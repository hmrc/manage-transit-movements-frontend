/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import java.time.LocalDateTime
import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import generators.Generators
import helper.WireMockServerHandler
import models.departure._
import models.{Departure, DepartureId, Departures, ErrorPointer, ErrorType, FunctionalError, LocalReferenceNumber, XMLSubmissionNegativeAcknowledgementMessage}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import utils.Format
import models.departure.DepartureStatus.DepartureSubmitted

import scala.concurrent.Future
import scala.xml.NodeSeq

class DeparturesMovementConnectorSpec extends SpecBase with WireMockServerHandler with ScalaCheckPropertyChecks with Generators {

  private lazy val connector: DeparturesMovementConnector =
    app.injector.instanceOf[DeparturesMovementConnector]
  private val startUrl = "transits-movements-trader-at-departure"

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(conf = "microservice.services.departure.port" -> server.port())
    .build()

  private val localDateTime: LocalDateTime = LocalDateTime.now()

  private val departuresResponseJson =
    Json.obj(
      "departures" ->
        Json.arr(
          Json.obj(
            "departureId"     -> 22,
            "updated"         -> localDateTime,
            "referenceNumber" -> "lrn",
            "status"          -> DepartureSubmitted.toString
          )
        )
    )

  val errorResponses: Gen[Int] = Gen.chooseNum(400, 599)

  "DeparturesMovementConnector" - {

    "getDepartures" - {
      "must return a successful future response" in {
        val expectedResult = {
          Departures(
            Seq(
              Departure(
                DepartureId(22),
                localDateTime,
                LocalReferenceNumber("lrn"),
                DepartureSubmitted
              )
            )
          )
        }

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/movements/departures"))
            .withHeader("Channel", containing("web"))
            .willReturn(okJson(departuresResponseJson.toString()))
        )

        connector.getDepartures().futureValue mustBe Some(expectedResult)
      }

      "must return a None when an error response is returned from getDepartures" in {

        forAll(errorResponses) {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(s"/$startUrl/movements/departures"))
                .withHeader("Channel", containing("web"))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )
            connector.getDepartures().futureValue mustBe None
        }
      }
    }

    "getSummary" - {

      "must be return summary of messages" in {
        val json = Json.obj(
          "departureId" -> departureId.index,
          "messages" -> Json.obj(
            "IE015" -> s"/movements/departures/${departureId.index}/messages/3",
            "IE055" -> s"/movements/departures/${departureId.index}/messages/5",
            "IE016" -> s"/movements/departures/${departureId.index}/messages/7",
            "IE009" -> s"/movements/departures/${departureId.index}/messages/9",
            "IE014" -> s"/movements/departures/${departureId.index}/messages/11",
            "IE051" -> s"/movements/departures/${departureId.index}/messages/12",
            "IE060" -> s"/movements/departures/${departureId.index}/messages/13"
          )
        )

        val messageAction =
          MessagesSummary(
            departureId,
            MessagesLocation(
              s"/movements/departures/${departureId.index}/messages/3",
              Some(s"/movements/departures/${departureId.index}/messages/5"),
              Some(s"/movements/departures/${departureId.index}/messages/7"),
              Some(s"/movements/departures/${departureId.index}/messages/9"),
              Some(s"/movements/departures/${departureId.index}/messages/11"),
              Some(s"/movements/departures/${departureId.index}/messages/12"),
              Some(s"/movements/departures/${departureId.index}/messages/13")
            )
          )

        server.stubFor(
          get(urlEqualTo(s"/transits-movements-trader-at-departure/movements/departures/${departureId.index}/messages/summary"))
            .withHeader("Channel", containing("web"))
            .willReturn(
              okJson(json.toString)
            )
        )
        connector.getSummary(departureId).futureValue mustBe Some(messageAction)
      }

      "must return 'None' when an error response is returned from getSummary" in {
        forAll(errorResponses) {
          errorResponseCode: Int =>
            stubGetResponse(errorResponseCode, "/transits-movements-trader-at-departure/movements/departures/1/messages/summary")

            connector.getSummary(departureId).futureValue mustBe None
        }
      }
    }

    "getNoReleaseForTransitMessage" - {
      "must return valid 'no release for transit message'" in {
        val location         = s"/transits-movements-trader-at-departure/movements/departures/${departureId.index}/messages/1"
        val noReleaseMessage = arbitrary[NoReleaseForTransitMessage].sample.value.copy(resultsOfControl = None)
        val xml: NodeSeq     = <CC051B>
          <HEAHEA>
          <DocNumHEA5>{noReleaseMessage.mrn}</DocNumHEA5>
          {noReleaseMessage.noReleaseMotivation.fold(NodeSeq.Empty) {
            noReleaseMotivation =>
              <NoRelMotHEA272>{noReleaseMotivation}</NoRelMotHEA272>}}
          <TotNumOfIteHEA305>{noReleaseMessage.totalNumberOfItems}</TotNumOfIteHEA305>
        </HEAHEA>
          <CUSOFFDEPEPT><RefNumEPT1>{noReleaseMessage.officeOfDepartureRefNumber}</RefNumEPT1></CUSOFFDEPEPT>
          <CONRESERS>
            <ConResCodERS16>{noReleaseMessage.controlResult.code}</ConResCodERS16>
            <ConDatERS14>{Format.dateFormatted(noReleaseMessage.controlResult.datLimERS69)}</ConDatERS14>
          </CONRESERS>
        </CC051B>

        val json = Json.obj("message" -> xml.toString())

        server.stubFor(
          get(urlEqualTo(location))
            .withHeader("Channel", containing("web"))
            .willReturn(
              okJson(json.toString)
            )
        )
        val expectedResult = Some(noReleaseMessage)

        connector.getNoReleaseForTransitMessage(location).futureValue mustBe expectedResult
      }

      "must return None for malformed input'" in {
        val location     = s"/transits-movements-trader-at-departure/movements/departures/${departureId.index}/messages/1"
        val xml: NodeSeq = <CC051B>
          <HEAHEA>
            <NoRelMotHEA272>token</NoRelMotHEA272>
            <TotNumOfIteHEA305>1000</TotNumOfIteHEA305>
          </HEAHEA>
          <CUSOFFDEPEPT><RefNumEPT1>RefNumber</RefNumEPT1></CUSOFFDEPEPT>
        </CC051B>

        val json = Json.obj("message" -> xml.toString())

        server.stubFor(
          get(urlEqualTo(location))
            .withHeader("Channel", containing("web"))
            .willReturn(
              okJson(json.toString)
            )
        )

        connector.getNoReleaseForTransitMessage(location).futureValue mustBe None
      }

      "must return None when an error response is returned from getGuaranteeNotValidMessage" in {
        val location: String = "/transits-movements-trader-at-departure/movements/departures/1/messages/1"
        forAll(errorResponses) {
          errorResponseCode =>
            stubGetResponse(errorResponseCode, location)

            connector.getNoReleaseForTransitMessage(location).futureValue mustBe None
        }
      }
    }

    "getControlDecisionMessage" - {
      "must return valid controlDecision" in {

        forAll(arbitrary[ControlDecision]) {
          controlDecision =>
            val location     = s"/transits-movements-trader-at-departure/movements/departures/${departureId.index}/messages/1"
            val xml: NodeSeq = ControlDecisionSpec.toXml(controlDecision)

            val json = Json.obj("message" -> xml.toString())

            server.stubFor(
              get(urlEqualTo(location))
                .withHeader("Channel", containing("web"))
                .willReturn(
                  okJson(json.toString)
                )
            )
            val result = connector.getControlDecisionMessage(location).futureValue.value

            result mustBe controlDecision
        }
      }

      "must return None for malformed input'" in {
        val location     = s"/transits-movements-trader-at-departure/movements/departures/${departureId.index}/messages/1"
        val xml: NodeSeq = <CC060A></CC060A>

        val json = Json.obj("message" -> xml.toString())

        server.stubFor(
          get(urlEqualTo(location))
            .withHeader("Channel", containing("web"))
            .willReturn(
              okJson(json.toString)
            )
        )

        connector.getControlDecisionMessage(location).futureValue mustBe None
      }

      "must return None when an error response is returned from getGuaranteeNotValidMessage" in {
        val location: String = "/transits-movements-trader-at-departure/movements/departures/1/messages/1"
        forAll(errorResponses) {
          errorResponseCode =>
            stubGetResponse(errorResponseCode, location)

            connector.getControlDecisionMessage(location).futureValue mustBe None
        }
      }
    }

    "getTadPdf" - {
      "must return status Ok" in {

        val departureId = DepartureId(0)

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/movements/departures/${departureId.index}/accompanying-document"))
            .withHeader("User-Agent", equalTo("manage-transit-movements-frontend"))
            .withHeader("Channel", equalTo("web"))
            .willReturn(
              aResponse()
                .withStatus(200)
            )
        )

        val result: Future[WSResponse] = connector.getPDF(departureId)

        result.futureValue.status mustBe 200
      }

      "must return other error status codes without exceptions" in {

        val genErrorResponse = Gen.oneOf(300, 500).sample.value
        val departureId      = DepartureId(0)

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/movements/departures/${departureId.index}/accompanying-document"))
            .withHeader("User-Agent", equalTo("manage-transit-movements-frontend"))
            .withHeader("Channel", equalTo("web"))
            .willReturn(
              aResponse()
                .withStatus(genErrorResponse)
            )
        )

        val result: Future[WSResponse] = connector.getPDF(departureId)

        result.futureValue.status mustBe genErrorResponse
      }
    }

    "getXMLSubmissionNegativeAcknowledgementMessage" - {
      "must return valid 'getXMLSubmissionNegativeAcknowledgementMessage'" in {
        val rejectionLocation = s"/transits-movements-trader-at-departure/movements/arrivals/${departureId.index}/messages/1"
        val genRejectionError = arbitrary[ErrorType].sample.value
        val rejectionXml: NodeSeq =
          <CC917A>
            <HEAHEA>
              <DocNumHEA5>19IT021300100075E9</DocNumHEA5>
            </HEAHEA>
            <FUNERRER1>
              <ErrTypER11>{genRejectionError.code}</ErrTypER11>
              <ErrPoiER12>Message type</ErrPoiER12>
              <OriAttValER14>GB007A</OriAttValER14>
            </FUNERRER1>
          </CC917A>

        val json = Json.obj("message" -> rejectionXml.toString())

        server.stubFor(
          get(urlEqualTo(rejectionLocation))
            .willReturn(
              okJson(json.toString)
            )
        )
        val expectedResult = Some(
          XMLSubmissionNegativeAcknowledgementMessage(
            Some("19IT021300100075E9"),
            None,
            FunctionalError(genRejectionError, ErrorPointer("Message type"), None, Some("GB007A"))
          ))
        connector.getXMLSubmissionNegativeAcknowledgementMessage(rejectionLocation).futureValue mustBe expectedResult
      }

      "must return None for malformed xml'" in {
        val rejectionLocation = s"/transit-movements-trader-at-destination/movements/arrivals/${departureId.index}/messages/1"
        val rejectionXml: NodeSeq =
          <CC917A>
            <HEAHEA><DocNumHEA5>19IT021300100075E9</DocNumHEA5>
            </HEAHEA>
          </CC917A>

        val json = Json.obj("message" -> rejectionXml.toString())

        server.stubFor(
          get(urlEqualTo(rejectionLocation))
            .willReturn(
              okJson(json.toString)
            )
        )

        connector.getXMLSubmissionNegativeAcknowledgementMessage(rejectionLocation).futureValue mustBe None
      }

    }

  }

  private def stubGetResponse(errorResponseCode: Int, serviceUrl: String) =
    server.stubFor(
      get(urlEqualTo(serviceUrl))
        .withHeader("Channel", containing("web"))
        .willReturn(
          aResponse()
            .withStatus(errorResponseCode)
        ))
}
