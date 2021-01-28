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
import helper.WireMockServerHandler
import models.departure.{MessagesLocation, MessagesSummary, NoReleaseForTransitMessage}
import models.{Departure, DepartureId, Departures, LocalReferenceNumber}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.xml.NodeSeq

class DeparturesMovementConnectorSpec extends SpecBase with WireMockServerHandler with ScalaCheckPropertyChecks {

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
            "status"          -> "Submitted"
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
                "Submitted"
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
            "IE051" -> s"/movements/departures/${departureId.index}/messages/12"
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
              Some(s"/movements/departures/${departureId.index}/messages/12")
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
        val location     = s"/transits-movements-trader-at-departure/movements/departures/${departureId.index}/messages/1"
        val xml: NodeSeq = <CC051B>
              <HEAHEA>
                <DocNumHEA5>19GB00006010021477</DocNumHEA5>
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
        val expectedResult = Some(NoReleaseForTransitMessage("19GB00006010021477", Some("token"), 1000, "RefNumber"))

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
