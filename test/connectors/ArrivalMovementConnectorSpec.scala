/*
 * Copyright 2022 HM Revenue & Customs
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
import models._
import models.arrival.ArrivalStatus.GoodsReleased
import models.arrival.{MessagesLocation, MessagesSummary, XMLSubmissionNegativeAcknowledgementMessage}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}
import play.api.libs.ws.WSResponse

import scala.concurrent.Future
import scala.xml.NodeSeq

class ArrivalMovementConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  private lazy val connector: ArrivalMovementConnector =
    app.injector.instanceOf[ArrivalMovementConnector]
  private val startUrl = "transit-movements-trader-at-destination"

  lazy val app: Application = new GuiceApplicationBuilder()
    .configure(conf = "microservice.services.destination.port" -> server.port())
    .build()
  private val arrivalId                    = ArrivalId(1)
  private val localDateTime: LocalDateTime = LocalDateTime.now()

  private val arrivalsResponseJson = Json.obj(
    "retrievedArrivals" -> 1,
    "totalArrivals"     -> 2,
    "totalMatched"      -> 3,
    "arrivals" -> JsArray(
      Seq(
        Json.obj(
          "arrivalId"               -> 22,
          "created"                 -> localDateTime,
          "updated"                 -> localDateTime,
          "movementReferenceNumber" -> "mrn123",
          "status"                  -> "GoodsReleased"
        )
      )
    )
  )

  val errorResponses: Gen[Int] = Gen.chooseNum(400, 599)

  "arrivalMovementConnector" - {

    "getArrivals" - {
      "must return a successful future response" in {
        val expectedResult =
          Arrivals(
            1,
            2,
            Some(3),
            Seq(
              Arrival(ArrivalId(22), localDateTime, localDateTime, "mrn123", GoodsReleased)
            )
          )

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/movements/arrivals"))
            .withHeader("Channel", containing("web"))
            .willReturn(okJson(arrivalsResponseJson.toString()))
        )

        connector.getArrivals().futureValue mustBe Some(expectedResult)

      }

      "must return a None when getArrivals returns an error response" in {

        forAll(errorResponses) {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(s"/$startUrl/movements/arrivals"))
                .withHeader("Channel", containing("web"))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )
            connector.getArrivals().futureValue mustBe None
        }
      }
    }

    "getArrivalSearchResults" - {
      "must return a successful future response" in {
        val expectedResult =
          Arrivals(
            1,
            2,
            Some(3),
            Seq(
              Arrival(ArrivalId(22), localDateTime, localDateTime, "mrn123", GoodsReleased)
            )
          )

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/movements/arrivals?mrn=theMrn&pageSize=100"))
            .withHeader("Channel", containing("web"))
            .willReturn(okJson(arrivalsResponseJson.toString()))
        )

        connector.getArrivalSearchResults("theMrn", 100).futureValue mustBe Some(expectedResult)
      }

      "must return a None when arrivals API returns an error response" in {

        forAll(errorResponses) {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(s"/$startUrl/movements/arrivals?mrn=theMrn&pageSize=100"))
                .withHeader("Channel", containing("web"))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )
            connector.getArrivalSearchResults("theMrn", 100).futureValue mustBe None
        }
      }
    }

    "getPagedArrivals" - {
      "must return a successful future response" in {
        val expectedResult =
          Arrivals(
            1,
            2,
            Some(3),
            Seq(
              Arrival(ArrivalId(22), localDateTime, localDateTime, "mrn123", GoodsReleased)
            )
          )

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/movements/arrivals?page=42&pageSize=100"))
            .withHeader("Channel", containing("web"))
            .willReturn(okJson(arrivalsResponseJson.toString()))
        )

        connector.getPagedArrivals(42, 100).futureValue mustBe Some(expectedResult)
      }

      "must return a None when getPagedArrivals returns an error response" in {

        forAll(errorResponses) {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(s"/$startUrl/movements/arrivals?page=42&pageSize=100"))
                .withHeader("Channel", containing("web"))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )
            connector.getPagedArrivals(42, 100).futureValue mustBe None
        }
      }
    }

    "getPDF" - {
      "must return status Ok" in {

        val arrivalId = ArrivalId(0)

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/movements/arrivals/${arrivalId.index}/unloading-permission"))
            .withHeader("Channel", containing("web"))
            .willReturn(
              aResponse()
                .withStatus(200)
            )
        )

        val result: Future[WSResponse] = connector.getPDF(arrivalId, "bearerToken")

        result.futureValue.status mustBe 200
      }

      "must return other error status codes without exceptions" in {

        val genErrorResponse = Gen.oneOf(300, 500).sample.value
        val arrivalId        = ArrivalId(0)

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/movements/arrivals/${arrivalId.index}/unloading-permission"))
            .withHeader("Channel", containing("web"))
            .willReturn(
              aResponse()
                .withStatus(genErrorResponse)
            )
        )

        val result: Future[WSResponse] = connector.getPDF(arrivalId, "bearerToken")

        result.futureValue.status mustBe genErrorResponse
      }
    }

    "getSummary" - {

      "must be return summary of messages" in {
        val json = Json.obj(
          "arrivalId" -> arrivalId.value,
          "messages" -> Json.obj(
            "IE007" -> s"/movements/arrivals/${arrivalId.value}/messages/3",
            "IE917" -> s"/movements/arrivals/${arrivalId.value}/messages/5"
          )
        )

        val messageAction =
          MessagesSummary(arrivalId,
                          MessagesLocation(s"/movements/arrivals/${arrivalId.value}/messages/3",
                                           None,
                                           Some(s"/movements/arrivals/${arrivalId.value}/messages/5")
                          )
          )

        server.stubFor(
          get(urlEqualTo(s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/summary"))
            .willReturn(
              okJson(json.toString)
            )
        )
        connector.getSummary(arrivalId).futureValue mustBe Some(messageAction)
      }

      "must return 'None' when an error response is returned from getSummary" in {
        forAll(errorResponses) {
          errorResponse: Int =>
            server.stubFor(
              get(urlEqualTo(s"/$startUrl/movements/arrivals/1/messages/summary"))
                .withHeader("Channel", containing("web"))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )

            connector.getSummary(ArrivalId(1)).futureValue mustBe None
        }
      }
    }

    "getXMLSubmissionNegativeAcknowledgementMessage" - {
      "must return valid 'getXMLSubmissionNegativeAcknowledgementMessage'" in {
        val rejectionLocation = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/1"
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
          )
        )
        connector.getXMLSubmissionNegativeAcknowledgementMessage(rejectionLocation).futureValue mustBe expectedResult
      }

      "must return None for malformed xml'" in {
        val rejectionLocation = s"/transit-movements-trader-at-destination/movements/arrivals/${arrivalId.value}/messages/1"
        val rejectionXml: NodeSeq =
          <CC917A>
            <HEAHEA>
              <DocNumHEA5>19IT021300100075E9</DocNumHEA5>
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
}
