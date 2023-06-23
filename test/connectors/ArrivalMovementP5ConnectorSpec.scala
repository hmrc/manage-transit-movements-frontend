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
import models.departureP5.FunctionalError
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global

class ArrivalMovementP5ConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  private lazy val connector: ArrivalMovementP5Connector = app.injector.instanceOf[ArrivalMovementP5Connector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.common-transit-convention-traders.port" -> server.port())

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
            |         "id":"343ffafafaaf",
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
            |         "id":"343ffafafaaf",
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

        val expectedResult = MessagesForArrivalMovement(
          NonEmptyList(
            ArrivalMessage(
              messageId,
              LocalDateTime.parse("2022-11-11T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.UnloadingPermission
            ),
            List(
              ArrivalMessage(
                messageId,
                LocalDateTime.parse("2022-11-10T12:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
                ArrivalMessageType.ArrivalNotification
              )
            )
          )
        )

        connector.getMessagesForMovement(s"movements/arrivals/$arrivalId/messages").futureValue mustBe expectedResult
      }
    }

    "getMessageMetaData" - {

      "must return Messages" in {

        val responseJson: JsValue = Json.parse("""
            {
                "_links": {
                    "self": {
                        "href": "/customs/transits/movements/arrivals/6365135ba5e821ee/messages"
                    },
                    "departure": {
                        "href": "/customs/transits/movements/arrivals/6365135ba5e821ee"
                    }
                },
                "messages": [
                    {
                        "_links": {
                            "self": {
                                "href": "/customs/transits/movements/arrivals/6365135ba5e821ee/message/634982098f02f00b"
                            },
                            "departure": {
                                "href": "/customs/transits/movements/arrivals/6365135ba5e821ee"
                            }
                        },
                        "id": "634982098f02f00a",
                        "departureId": "6365135ba5e821ee",
                        "received": "2022-11-11T15:32:51.459Z",
                        "type": "IE007",
                        "status": "Success"
                    },
                    {
                        "_links": {
                            "self": {
                                "href": "/customs/transits/movements/arrivals/6365135ba5e821ee/message/634982098f02f00a"
                            },
                            "departure": {
                                "href": "/customs/transits/movements/arrivals/6365135ba5e821ee"
                            }
                        },
                        "id": "634982098f02f00a",
                        "departureId": "6365135ba5e821ee",
                        "received": "2022-11-10T15:32:51.459Z",
                        "type": "IE057",
                        "status": "Success"
                    }
                ]
            }
            """)

        val expectedResult = ArrivalMessages(
          List(
            ArrivalMessageMetaData(
              LocalDateTime.parse("2022-11-11T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.ArrivalNotification,
              "movements/arrivals/6365135ba5e821ee/message/634982098f02f00b"
            ),
            ArrivalMessageMetaData(
              LocalDateTime.parse("2022-11-10T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.RejectionFromOfficeOfDestination,
              "movements/arrivals/6365135ba5e821ee/message/634982098f02f00a"
            )
          )
        )

        server.stubFor(
          get(urlEqualTo(s"/movements/arrivals/$arrivalIdP5/messages"))
            .willReturn(okJson(responseJson.toString()))
        )

        connector.getMessageMetaData(arrivalIdP5).futureValue mustBe expectedResult

      }
    }

    "getSpecificMessage" - {

      "must return an IE057 Message" in {

        val IEO57 = Json.parse(
          """
            |{
            |  "n1:CC057C": {
            |    "TransitOperation": {
            |      "MRN": "CD3232"
            |    },
            |    "CustomsOfficeOfDestinationActual": {
            |      "referenceNumber": "1234"
            |    },
            |    "FunctionalError": [
            |      {
            |        "errorPointer": "1",
            |        "errorCode": "12",
            |        "errorReason": "Codelist violation"
            |      },
            |      {
            |        "errorPointer": "2",
            |        "errorCode": "14",
            |        "errorReason": "Rule violation"
            |      }
            |    ]
            |  }
            |}
            |""".stripMargin
        )

        val responseJson: JsValue = Json.parse(s"""
          {
            "_links": {
              "self": {
                "href": "/customs/transits/movements/arrivals/62f4ebbbf581d4aa/messages/62f4ebbb765ba8c2"
              },
              "departure": {
                "href": "/customs/transits/movements/arrivals/62f4ebbbf581d4aa"
              }
            },
            "id": "62f4ebbb765ba8c2",
            "arrivalId": "62f4ebbbf581d4aa",
            "received": "2022-08-11T11:44:59.83705",
            "type": "IE057",
            "status": "Success",
            "body": ${IEO57.toString()}
          }
          """)

        val expectedResult: IE057Data = IE057Data(
          IE057MessageData(
            TransitOperationIE057("CD3232"),
            CustomsOfficeOfDestinationActual("1234"),
            Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
          )
        )

        server.stubFor(
          get(urlEqualTo(s"/movements/arrivals/$arrivalIdP5/messages/62f4ebbb765ba8c2"))
            .willReturn(okJson(responseJson.toString()))
        )

        connector.getSpecificMessage[IE057Data](s"movements/arrivals/$arrivalIdP5/messages/62f4ebbb765ba8c2").futureValue mustBe expectedResult

      }
    }
  }

}
