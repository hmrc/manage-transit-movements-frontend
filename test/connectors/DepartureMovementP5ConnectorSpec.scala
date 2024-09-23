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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo}
import generators.Generators
import helper.WireMockServerHandler
import models.Availability
import models.departureP5._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global

class DepartureMovementP5ConnectorSpec extends SpecBase with WireMockServerHandler with Generators with ScalaCheckPropertyChecks {

  private lazy val connector: DepartureMovementP5Connector = app.injector.instanceOf[DepartureMovementP5Connector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.common-transit-convention-traders.port" -> server.port())

  private val genError = Gen.chooseNum(400: Int, 599: Int).suchThat(_ != 404)

  val ie056 = Json.parse(
    """
      |{
      |  "n1:CC056C": {
      |    "TransitOperation": {
      |      "LRN": "AB123",
      |      "MRN": "CD3232",
      |      "businessRejectionType": "015"
      |    },
      |    "CustomsOfficeOfDeparture": {
      |     "referenceNumber": "22323323"
      |     },
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
        "type": "IE056",
        "status": "Success",
        "body": ${ie056.toString()}
      }
      """)

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
            "totalCount": 2,
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
                "localReferenceNumber": "AB123",
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
                "localReferenceNumber": "CD123",
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
          departureMovements = Seq(
            DepartureMovement(
              "63651574c3447b12",
              Some("27WF9X1FQ9RCKN0TM3"),
              "AB123",
              LocalDateTime.parse("2022-11-04T13:36:52.332Z", DateTimeFormatter.ISO_DATE_TIME)
            ),
            DepartureMovement(
              "6365135ba5e821ee",
              Some("27WF9X1FQ9RCKN0TM3"),
              "CD123",
              LocalDateTime.parse("2022-11-04T13:27:55.522Z", DateTimeFormatter.ISO_DATE_TIME)
            )
          ),
          totalCount = 2
        )

        connector.getAllMovements().futureValue `mustBe` Some(expectedResult)
      }

      "must return empty DepartureMovements when 404 is returned" in {

        server.stubFor(
          get(urlEqualTo(s"/movements/departures"))
            .willReturn(aResponse().withStatus(404))
        )

        connector.getAllMovements().futureValue `mustBe` Some(DepartureMovements(Seq.empty, 0))
      }

      "must return None when an error is returned" in {
        forAll(genError) {
          error =>
            server.stubFor(
              get(urlEqualTo(s"/movements/departures"))
                .willReturn(aResponse().withStatus(error))
            )

            connector.getAllMovements().futureValue `mustBe` None
        }
      }
    }

    "getAllMovementsForSearchQuery" - {

      val responseJson = Json.parse("""
          |{
          |  "_links": {
          |    "self": {
          |      "href": "/customs/transits/movements/departures"
          |    }
          |  },
          |  "totalCount": 1,
          |  "departures": [
          |    {
          |      "_links": {
          |        "self": {
          |          "href": "/customs/transits/movements/departures/63651574c3447b12"
          |        },
          |        "messages": {
          |          "href": "/customs/transits/movements/departures/63651574c3447b12/messages"
          |        }
          |      },
          |      "id": "63651574c3447b12",
          |      "localReferenceNumber": "LRN12345",
          |      "created": "2022-11-04T13:36:52.332Z",
          |      "updated": "2022-11-04T13:36:52.332Z",
          |      "enrollmentEORINumber": "9999912345",
          |      "movementEORINumber": "GB1234567890"
          |    }
          |  ]
          |}
          |""".stripMargin)

      "when search param provided" - {
        "must add values to request url" in {
          val searchParam = "LRN123"
          server.stubFor(
            get(urlEqualTo(s"/movements/departures?page=1&count=20&localReferenceNumber=$searchParam"))
              .willReturn(okJson(responseJson.toString()))
          )

          val expectedResult = DepartureMovements(
            departureMovements = Seq(
              DepartureMovement(
                "63651574c3447b12",
                None,
                "LRN12345",
                LocalDateTime.parse("2022-11-04T13:36:52.332Z", DateTimeFormatter.ISO_DATE_TIME)
              )
            ),
            totalCount = 1
          )

          connector.getAllMovementsForSearchQuery(1, 20, Some(searchParam)).futureValue `mustBe` Some(expectedResult)
        }
      }

      "when search param not provided" - {
        "must add values to request url" in {
          server.stubFor(
            get(urlEqualTo("/movements/departures?page=1&count=20"))
              .willReturn(okJson(responseJson.toString()))
          )

          val expectedResult = DepartureMovements(
            departureMovements = Seq(
              DepartureMovement(
                "63651574c3447b12",
                None,
                "LRN12345",
                LocalDateTime.parse("2022-11-04T13:36:52.332Z", DateTimeFormatter.ISO_DATE_TIME)
              )
            ),
            totalCount = 1
          )

          connector.getAllMovementsForSearchQuery(1, 20, None).futureValue `mustBe` Some(expectedResult)
        }
      }
    }

    "getAvailability" - {
      "must return NonEmpty" - {
        "when departure returned" in {
          val responseJson = Json.parse("""
              |{
              |  "_links": {
              |    "self": {
              |      "href": "/customs/transits/movements/departures"
              |    }
              |  },
              |  "totalCount": 1,
              |  "departures": [
              |    {
              |      "_links": {
              |        "self": {
              |          "href": "/customs/transits/movements/departures/63651574c3447b12"
              |        },
              |        "messages": {
              |          "href": "/customs/transits/movements/departures/63651574c3447b12/messages"
              |        }
              |      },
              |      "id": "63651574c3447b12",
              |      "movementReferenceNumber": "27WF9X1FQ9RCKN0TM3",
              |      "localReferenceNumber": "AB123",
              |      "created": "2022-11-04T13:36:52.332Z",
              |      "updated": "2022-11-04T13:36:52.332Z",
              |      "enrollmentEORINumber": "9999912345",
              |      "movementEORINumber": "GB1234567890"
              |    }
              |  ]
              |}
              |""".stripMargin)

          server.stubFor(
            get(urlEqualTo("/movements/departures?count=1"))
              .willReturn(okJson(responseJson.toString()))
          )

          connector.getAvailability().futureValue `mustBe` Availability.NonEmpty
        }
      }

      "must return Empty" - {
        "when no departures returned" in {
          val responseJson = Json.parse("""
              |{
              |  "_links": {
              |    "self": {
              |      "href": "/customs/transits/movements/departures"
              |    }
              |  },
              |  "totalCount": 0,
              |  "departures": []
              |}
              |""".stripMargin)

          server.stubFor(
            get(urlEqualTo("/movements/departures?count=1"))
              .willReturn(okJson(responseJson.toString()))
          )

          connector.getAvailability().futureValue `mustBe` Availability.Empty
        }
      }

      "must return Unavailable" - {
        "when there is an error" in {
          forAll(genError) {
            error =>
              server.stubFor(
                get(urlEqualTo("/movements/departures?count=1"))
                  .willReturn(aResponse().withStatus(error))
              )

              connector.getAvailability().futureValue `mustBe` Availability.Unavailable
          }
        }
      }
    }

    "getDepartureReferenceNumbers" - {

      "must return departure reference numbers when MRN is defined" in {

        val responseJson = Json.parse(
          """
            |{
            |   "id": "6365135ba5e821ee",
            |   "movementReferenceNumber": "ABC123",
            |   "localReferenceNumber": "DEF456",
            |   "created": "2022-11-10T15:32:51.459Z",
            |   "updated": "2022-11-10T15:32:51.459Z",
            |   "enrollmentEORINumber": "GB1234567890",
            |   "movementEORINumber": "GB1234567890"
            |}
            |""".stripMargin
        )

        server.stubFor(
          get(urlEqualTo(s"/movements/departures/$departureIdP5"))
            .willReturn(okJson(responseJson.toString()))
        )

        val expectedResult = DepartureReferenceNumbers("DEF456", Some("ABC123"))

        connector.getDepartureReferenceNumbers(departureIdP5).futureValue `mustBe` expectedResult
      }

      "must return departure reference numbers when MRN is not defined" in {

        val responseJson = Json.parse(
          """
            |{
            |   "id": "6365135ba5e821ee",
            |   "localReferenceNumber": "DEF456",
            |   "created": "2022-11-10T15:32:51.459Z",
            |   "updated": "2022-11-10T15:32:51.459Z",
            |   "enrollmentEORINumber": "GB1234567890",
            |   "movementEORINumber": "GB1234567890"
            |}
            |""".stripMargin
        )

        server.stubFor(
          get(urlEqualTo(s"/movements/departures/$departureIdP5"))
            .willReturn(okJson(responseJson.toString()))
        )

        val expectedResult = DepartureReferenceNumbers("DEF456", None)

        connector.getDepartureReferenceNumbers(departureIdP5).futureValue `mustBe` expectedResult
      }
    }

    "getLatestMessageForMovement" - {
      val messageId = "634982098f02f00a"

      "must return latest message" - {
        "when arrival returned" in {
          val responseJson: JsValue = Json.parse(s"""
               |{
               |  "_links": {
               |    "self": {
               |      "href": "/customs/transits/movements/departures/$departureIdP5/messages"
               |    },
               |    "departure": {
               |      "href": "/customs/transits/movements/departures/$departureIdP5"
               |    }
               |  },
               |  "totalCount": 1,
               |  "messages": [
               |    {
               |      "_links": {
               |        "self": {
               |          "href": "/customs/transits/movements/departures/$departureIdP5/messages/$messageId"
               |        },
               |        "arrival": {
               |          "href": "/customs/transits/movements/departures/$departureIdP5"
               |        }
               |      },
               |      "id": "$messageId",
               |      "departureId": "$departureIdP5",
               |      "received": "2022-11-10T15:32:51.459Z",
               |      "type": "IE015",
               |      "status": "Success"
               |    }
               |  ]
               |}
               |""".stripMargin)

          server.stubFor(
            get(urlEqualTo(s"/movements/departures/$departureIdP5/messages?count=500"))
              .willReturn(okJson(responseJson.toString()))
          )

          connector.getLatestMessageForMovement(departureIdP5).futureValue mustBe
            LatestDepartureMessage(
              latestMessage = DepartureMessage(
                messageId = messageId,
                received = LocalDateTime.of(2022, 11, 10, 15, 32, 51, 459000000),
                messageType = DepartureMessageType.DepartureNotification
              ),
              ie015MessageId = messageId
            )
        }
      }
    }

  }

}
