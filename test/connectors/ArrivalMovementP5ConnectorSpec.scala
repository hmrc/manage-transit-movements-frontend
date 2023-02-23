/*
 * Copyright 2023 HM Revenue & Customs
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

import base.SpecBase
import cats.data.NonEmptyList
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo}
import generators.Generators
import helper.WireMockServerHandler
import models.arrivalP5._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ArrivalMovementP5ConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  private lazy val connector: ArrivalMovementP5Connector = app.injector.instanceOf[ArrivalMovementP5Connector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.common-transit-convention-trader.port" -> server.port())

  "ArrivalMovementP5Connector" - {

    "getAllMovements" - {

      val responseJson: JsValue = Json.parse(
        """
          |{
          |  "_links": {
          |    "self": {
          |      "href": "/customs/transits/movements/arrivals"
          |    }
          |  },
          |  "arrivals": [
          |    {
          |      "_links": {
          |        "self": {
          |          "href": "/customs/transits/movements/arrivals/63651574c3447b12"
          |        },
          |        "messages": {
          |          "href": "/customs/transits/movements/arrivals/63651574c3447b12/messages"
          |        }
          |      },
          |      "id": "63651574c3447b12",
          |      "movementReferenceNumber": "27WF9X1FQ9RCKN0TM3",
          |      "created": "2022-11-04T13:36:52.332Z",
          |      "updated": "2022-11-04T13:36:52.332Z",
          |      "enrollmentEORINumber": "9999912345",
          |      "movementEORINumber": "GB1234567890"
          |    },
          |    {
          |      "_links": {
          |        "self": {
          |          "href": "/customs/transits/movements/arrivals/6365135ba5e821ee"
          |        },
          |        "messages": {
          |          "href": "/customs/transits/movements/arrivals/6365135ba5e821ee/messages"
          |        }
          |      },
          |      "id": "6365135ba5e821ee",
          |      "movementReferenceNumber": "27WF9X1FQ9RCKN0TM3",
          |      "created": "2022-11-04T13:27:55.522Z",
          |      "updated": "2022-11-04T13:27:55.522Z",
          |      "enrollmentEORINumber": "9999912345",
          |      "movementEORINumber": "GB1234567890"
          |    }
          |  ]
          |}
          |""".stripMargin
      )

      "must return ArrivalMovements" in {

        server.stubFor(
          get(urlEqualTo(s"/movements/arrivals"))
            .willReturn(okJson(responseJson.toString()))
        )

        val expectedResult = ArrivalMovements(
          Seq(
            ArrivalMovement(
              "63651574c3447b12",
              "27WF9X1FQ9RCKN0TM3",
              LocalDateTime.parse("2022-11-04T13:36:52.332Z", DateTimeFormatter.ISO_DATE_TIME),
              "movements/arrivals/63651574c3447b12/messages"
            ),
            ArrivalMovement(
              "6365135ba5e821ee",
              "27WF9X1FQ9RCKN0TM3",
              LocalDateTime.parse("2022-11-04T13:27:55.522Z", DateTimeFormatter.ISO_DATE_TIME),
              "movements/arrivals/6365135ba5e821ee/messages"
            )
          )
        )

        connector.getAllMovements().futureValue mustBe Some(expectedResult)
      }

      "must return empty ArrivalMovements when 404 is returned" in {

        server.stubFor(
          get(urlEqualTo(s"/movements/arrivals"))
            .willReturn(aResponse().withStatus(404))
        )

        connector.getAllMovements().futureValue mustBe Some(ArrivalMovements(Seq.empty))
      }

      "must return None when an error is returned" in {
        val genError = Gen.chooseNum(400, 599).suchThat(_ != 404)

        forAll(genError) {
          error =>
            server.stubFor(
              get(urlEqualTo(s"/movements/arrivals"))
                .willReturn(aResponse().withStatus(error))
            )

            connector.getAllMovements().futureValue mustBe None
        }
      }
    }

    "getMessagesForMovement" - {

      val arrivalId = "63498209a2d89ad8"

      val responseJson =
        Json.parse("""
            |{
            |   "_links":{
            |      "self":{
            |         "href":"/customs/transits/movements/arrivals/1/messages"
            |      },
            |      "arrival":{
            |         "href":"/customs/transits/movements/arrivals/1"
            |      }
            |   },
            |   "messages":[
            |      {
            |         "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/arrivals/1/messages/2"
            |             },
            |             "arrival":{
            |                "href":"/customs/transits/movements/arrivals/1"
            |             }
            |         },
            |         "id":"634982098f02f00a",
            |         "arrivalId":"1",
            |         "received":"2022-11-10T12:32:51.459Z",
            |         "type":"IE007"
            |      },
            |      {
            |         "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/arrivals/1/messages/1"
            |             },
            |             "arrival":{
            |                "href":"/customs/transits/movements/arrivals/1"
            |             }
            |         },
            |         "id":"634982098f02f00a",
            |         "arrivalId":"1",
            |         "received":"2022-11-11T15:32:51.459Z",
            |         "type":"IE043"
            |      }
            |   ]
            |}
            |""".stripMargin)

      "must return MessagesForMovement" in {
        server.stubFor(
          get(urlEqualTo(s"/movements/arrivals/$arrivalId/messages"))
            .willReturn(okJson(responseJson.toString()))
        )

        val expectedResult = MessagesForMovement(
          NonEmptyList(
            Message(
              LocalDateTime.parse("2022-11-11T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.UnloadingPermission
            ),
            List(
              Message(
                LocalDateTime.parse("2022-11-10T12:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
                ArrivalMessageType.ArrivalNotification
              )
            )
          )
        )

        connector.getMessagesForMovement(s"movements/arrivals/$arrivalId/messages").futureValue mustBe expectedResult
      }
    }
  }

}
