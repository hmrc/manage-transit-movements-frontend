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
import models.departureP5.{DepartureMessageType, _}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global

class DepartureMovementP5ConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  private lazy val connector: DepartureMovementP5Connector = app.injector.instanceOf[DepartureMovementP5Connector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.common-transit-convention-traders.port" -> server.port())

  "DepartureMovementP5Connector" - {

    "getAllMovements" - {

      val responseJson: JsValue = Json.parse(
        """
          {
            "_links": {
              "self": {
                "href": "/customs/transits/movements/departures"
              }
            },
            "departures": [
              {
                "_links": {
                  "self": {
                    "href": "/customs/transits/movements/departures/63651574c3447b12"
                  },
                  "messages": {
                    "href": "/customs/transits/movements/departures/63651574c3447b12/messages"
                  }
                },
                "id": "63651574c3447b12",
                "movementReferenceNumber": "27WF9X1FQ9RCKN0TM3",
                "created": "2022-11-04T13:36:52.332Z",
                "updated": "2022-11-04T13:36:52.332Z",
                "enrollmentEORINumber": "9999912345",
                "movementEORINumber": "GB1234567890"
              },
              {
                "_links": {
                  "self": {
                    "href": "/customs/transits/movements/departures/6365135ba5e821ee"
                  },
                  "messages": {
                    "href": "/customs/transits/movements/departures/6365135ba5e821ee/messages"
                  }
                },
                "id": "6365135ba5e821ee",
                "movementReferenceNumber": "27WF9X1FQ9RCKN0TM3",
                "created": "2022-11-04T13:27:55.522Z",
                "updated": "2022-11-04T13:27:55.522Z",
                "enrollmentEORINumber": "9999912345",
                "movementEORINumber": "GB1234567890"
              }
            ]
          }
          """
      )

      "must return DepartureMovements" in {

        server.stubFor(
          get(urlEqualTo(s"/movements/departures"))
            .willReturn(okJson(responseJson.toString()))
        )

        val expectedResult = DepartureMovements(
          Seq(
            DepartureMovement(
              "63651574c3447b12",
              Some("27WF9X1FQ9RCKN0TM3"),
              LocalDateTime.parse("2022-11-04T13:36:52.332Z", DateTimeFormatter.ISO_DATE_TIME),
              "movements/departures/63651574c3447b12/messages"
            ),
            DepartureMovement(
              "6365135ba5e821ee",
              Some("27WF9X1FQ9RCKN0TM3"),
              LocalDateTime.parse("2022-11-04T13:27:55.522Z", DateTimeFormatter.ISO_DATE_TIME),
              "movements/departures/6365135ba5e821ee/messages"
            )
          )
        )

        connector.getAllMovements().futureValue mustBe Some(expectedResult)
      }

      "must return empty DepartureMovements when 404 is returned" in {

        server.stubFor(
          get(urlEqualTo(s"/movements/departures"))
            .willReturn(aResponse().withStatus(404))
        )

        connector.getAllMovements().futureValue mustBe Some(DepartureMovements(Seq.empty))
      }

      "must return None when an error is returned" in {
        val genError = Gen.chooseNum(400, 599).suchThat(_ != 404)

        forAll(genError) {
          error =>
            server.stubFor(
              get(urlEqualTo(s"/movements/departures"))
                .willReturn(aResponse().withStatus(error))
            )

            connector.getAllMovements().futureValue mustBe None
        }
      }
    }

    "getMessagesForMovement" - {

      val departureId = "63498209a2d89ad8"

      val responseJson =
        Json.parse(s"""
            {
               "_links":{
                  "self":{
                     "href":"/customs/transits/movements/departures/1/messages"
                  },
                  "departure":{
                     "href":"/customs/transits/movements/departures/1"
                  }
               },
               "messages":[
                  {
                     "_links":{
                         "self":{
                            "href":"/customs/transits/movements/departures/1/messages/2"
                         },
                         "departure":{
                            "href":"/customs/transits/movements/departures/1"
                         }
                     },
                     "id":"634982098f02f00a",
                     "departureId":"$departureId",
                     "received":"2022-11-10T12:32:51.459Z",
                     "type":"IE015"
                  },
                  {
                     "_links":{
                         "self":{
                            "href":"/customs/transits/movements/departures/1/messages/1"
                         },
                         "departure":{
                            "href":"/customs/transits/movements/departures/1"
                         }
                     },
                     "id":"634982098f02f00a",
                     "departureId":"$departureId",
                     "received":"2022-11-11T15:32:51.459Z",
                     "type":"IE028"
                  }
               ]
            }
            """)

      "must return MessagesForMovement" in {
        server.stubFor(
          get(urlEqualTo(s"/movements/departures/$departureId/messages"))
            .willReturn(okJson(responseJson.toString()))
        )

        val expectedResult = MessagesForDepartureMovement(
          NonEmptyList(
            DepartureMessage(
              LocalDateTime.parse("2022-11-11T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.AllocatedMRN,
              "movements/departures/1/messages/1"
            ),
            List(
              DepartureMessage(
                LocalDateTime.parse("2022-11-10T12:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
                DepartureMessageType.DepartureNotification,
                "movements/departures/1/messages/2"
              )
            )
          )
        )

        connector.getMessagesForMovement(s"movements/departures/$departureId/messages").futureValue mustBe expectedResult
      }

    }

    "getMessageMetaData" - {

      "must return Messages" in {

        val responseJson: JsValue = Json.parse("""
            {
                "_links": {
                    "self": {
                        "href": "/customs/transits/movements/departures/6365135ba5e821ee/messages"
                    },
                    "departure": {
                        "href": "/customs/transits/movements/departures/6365135ba5e821ee"
                    }
                },
                "messages": [
                    {
                        "_links": {
                            "self": {
                                "href": "/customs/transits/movements/departures/6365135ba5e821ee/message/634982098f02f00b"
                            },
                            "departure": {
                                "href": "/customs/transits/movements/departures/6365135ba5e821ee"
                            }
                        },
                        "id": "634982098f02f00a",
                        "departureId": "6365135ba5e821ee",
                        "received": "2022-11-11T15:32:51.459Z",
                        "type": "IE015",
                        "status": "Success"
                    },
                    {
                        "_links": {
                            "self": {
                                "href": "/customs/transits/movements/departures/6365135ba5e821ee/message/634982098f02f00a"
                            },
                            "departure": {
                                "href": "/customs/transits/movements/departures/6365135ba5e821ee"
                            }
                        },
                        "id": "634982098f02f00a",
                        "departureId": "6365135ba5e821ee",
                        "received": "2022-11-10T15:32:51.459Z",
                        "type": "IE028",
                        "status": "Success"
                    }
                ]
            }
            """)

        val expectedResult = DepartureMessages(
          List(
            DepartureMessageMetaData(
              LocalDateTime.parse("2022-11-11T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.DepartureNotification,
              "movements/departures/6365135ba5e821ee/message/634982098f02f00b"
            ),
            DepartureMessageMetaData(
              LocalDateTime.parse("2022-11-10T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.AllocatedMRN,
              "movements/departures/6365135ba5e821ee/message/634982098f02f00a"
            )
          )
        )

        server.stubFor(
          get(urlEqualTo(s"/movements/departures/$departureIdP5/messages"))
            .willReturn(okJson(responseJson.toString()))
        )

        connector.getMessageMetaData(departureIdP5).futureValue mustBe expectedResult

      }
    }

    "getSpecificMessage" - {

      "must return an IE060 Message" in {

        val IE060 = Json.parse("""{
                                  "n1:CC060C":
                                  {
                                  "TransitOperation":
                                  { "LRN": "AB123",
                                          "MRN": "CD3232",
                                          "controlNotificationDateAndTime": "2014-06-09T16:15:04+01:00",
                                         "notificationType": "notification1"
                                     },
                                      "CustomsOfficeOfDeparture": {
                                          "referenceNumber": "22323323"
                                      },
                                      "TypeOfControls": [
                                         {
                                              "sequenceNumber": "1",
                                              "type": "type1",
                                              "text": "text1"
                                          },
                                          {
                                              "sequenceNumber": "2",
                                              "type": "type2"
                                          }
                                      ],
                                      "RequestedDocument": [
                                          {
                                              "sequenceNumber": "3",
                                              "documentType": "doc1",
                                              "description": "desc1"
                                          },
                                          {
                                              "sequenceNumber": "4",
                                              "documentType": "doc2"
                                          }
                                     ]
                                  }
                                }""")

        val responseJson: JsValue = Json.parse(s"""
            {
              "_links": {
                "self": {
                  "href": "/customs/transits/movements/departures/62f4ebbbf581d4aa/messages/62f4ebbb765ba8c2"
                },
                "departure": {
                  "href": "/customs/transits/movements/departures/62f4ebbbf581d4aa"
                }
              },
              "id": "62f4ebbb765ba8c2",
              "departureId": "62f4ebbbf581d4aa",
              "received": "2022-08-11T11:44:59.83705",
              "type": "IE060",
              "status": "Success",
              "body": ${IE060.toString()}
            }
            """)

        val expectedResult = IE060Data(
          IE060MessageData(
            TransitOperation(Some("CD3232"), Some("AB123"), LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
            CustomsOfficeOfDeparture("22323323"),
            Some(Seq(TypeOfControls("1", "type1", Some("text1")), TypeOfControls("2", "type2", None))),
            Some(Seq(RequestedDocument("3", "doc1", Some("desc1")), RequestedDocument("4", "doc2", None)))
          )
        )

        server.stubFor(
          get(urlEqualTo(s"/movements/departures/$departureIdP5/messages/62f4ebbb765ba8c2"))
            .willReturn(okJson(responseJson.toString()))
        )

        connector.getSpecificMessage[IE060Data](s"movements/departures/$departureIdP5/messages/62f4ebbb765ba8c2").futureValue mustBe expectedResult

      }

      "must return an IE056 Message" in {

        val IEO56 = Json.parse(
          """
            |{
            |  "n1:CC056C": {
            |    "TransitOperation": {
            |      "LRN": "AB123",
            |      "MRN": "CD3232"
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
        "href": "/customs/transits/movements/departures/62f4ebbbf581d4aa/messages/62f4ebbb765ba8c2"
      },
      "departure": {
        "href": "/customs/transits/movements/departures/62f4ebbbf581d4aa"
      }
    },
    "id": "62f4ebbb765ba8c2",
    "departureId": "62f4ebbbf581d4aa",
    "received": "2022-08-11T11:44:59.83705",
    "type": "IE060",
    "status": "Success",
    "body": ${IEO56.toString()}
  }
  """)

        val expectedResult: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("CD3232"), Some("AB123")),
            Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
          )
        )

        server.stubFor(
          get(urlEqualTo(s"/movements/departures/$departureIdP5/messages/62f4ebbb765ba8c2"))
            .willReturn(okJson(responseJson.toString()))
        )

        connector.getSpecificMessage[IE056Data](s"movements/departures/$departureIdP5/messages/62f4ebbb765ba8c2").futureValue mustBe expectedResult

      }
    }

    "getLRN" - {

      "must return LocalReferenceNumber" in {

        val ie015Body = Json.obj(
          "CC015" -> Json.obj(
            "TransitOperation" -> Json.obj(
              "LRN" -> "AB123"
            )
          )
        )

        val responseJson = Json.parse(
          s"""
             |{
             |  "_links": {
             |    "self": {
             |      "href": "/customs/transits/movements/departures/62f4ebbbf581d4aa/messages/62f4ebbb765ba8c2"
             |    },
             |    "departure": {
             |      "href": "/customs/transits/movements/departures/62f4ebbbf581d4aa"
             |    }
             |  },
             |  "id": "62f4ebbb765ba8c2",
             |  "departureId": "62f4ebbbf581d4aa",
             |  "received": "2022-08-11T11:44:59.83705",
             |  "type": "IE015",
             |  "status": "Success",
             |  "body": ${ie015Body.toString}
             |}
             |""".stripMargin
        )

        server.stubFor(
          get(urlEqualTo(s"/movements/departures/$departureId/messages/ab123"))
            .willReturn(okJson(responseJson.toString()))
        )

        val result = connector.getLRN(s"movements/departures/$departureId/messages/ab123").futureValue

        result mustBe LocalReferenceNumber("AB123")
      }

    }
  }

}
