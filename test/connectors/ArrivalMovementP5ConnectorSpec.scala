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
      .configure(conf = "microservice.services.common-transit-convention-traders.port" -> server.port())

  private val genError = Gen.chooseNum(400: Int, 599: Int).suchThat(_ != 404)

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
          |  "totalCount": 2,
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
          arrivalMovements = Seq(
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
          ),
          totalCount = 2
        )

        connector.getAllMovements().futureValue mustBe Some(expectedResult)
      }

      "must return empty ArrivalMovements when 404 is returned" in {

        server.stubFor(
          get(urlEqualTo(s"/movements/arrivals"))
            .willReturn(aResponse().withStatus(404))
        )

        connector.getAllMovements().futureValue mustBe Some(ArrivalMovements(Seq.empty, 0))
      }

      "must return None when an error is returned" in {
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

    "getAllMovementsForSearchQuery" - {

      val responseJson = Json.parse("""
          |{
          |  "_links": {
          |    "self": {
          |      "href": "/customs/transits/movements/arrivals"
          |    }
          |  },
          |  "totalCount": 1,
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
          |      "movementReferenceNumber": "MRN12345",
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
          val searchParam = "MRN123"
          server.stubFor(
            get(urlEqualTo(s"/movements/arrivals?page=1&count=20&movementReferenceNumber=$searchParam"))
              .willReturn(okJson(responseJson.toString()))
          )

          val expectedResult = ArrivalMovements(
            arrivalMovements = Seq(
              ArrivalMovement(
                "63651574c3447b12",
                "MRN12345",
                LocalDateTime.parse("2022-11-04T13:36:52.332Z", DateTimeFormatter.ISO_DATE_TIME),
                "movements/arrivals/63651574c3447b12/messages"
              )
            ),
            totalCount = 1
          )

          connector.getAllMovementsForSearchQuery(1, 20, Some(searchParam)).futureValue mustBe Some(expectedResult)
        }
      }

      "when search param not provided" - {
        "must add values to request url" in {
          server.stubFor(
            get(urlEqualTo("/movements/arrivals?page=1&count=20"))
              .willReturn(okJson(responseJson.toString()))
          )

          val expectedResult = ArrivalMovements(
            arrivalMovements = Seq(
              ArrivalMovement(
                "63651574c3447b12",
                "MRN12345",
                LocalDateTime.parse("2022-11-04T13:36:52.332Z", DateTimeFormatter.ISO_DATE_TIME),
                "movements/arrivals/63651574c3447b12/messages"
              )
            ),
            totalCount = 1
          )

          connector.getAllMovementsForSearchQuery(1, 20, None).futureValue mustBe Some(expectedResult)
        }
      }
    }

    "getAvailability" - {
      "must return NonEmpty" - {
        "when arrival returned" in {
          val responseJson: JsValue = Json.parse("""
              |{
              |  "_links": {
              |    "self": {
              |      "href": "/customs/transits/movements/arrivals"
              |    }
              |  },
              |  "totalCount": 2,
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
              |""".stripMargin)

          server.stubFor(
            get(urlEqualTo("/movements/arrivals?count=1"))
              .willReturn(okJson(responseJson.toString()))
          )

          connector.getAvailability().futureValue mustBe Availability.NonEmpty
        }
      }

      "must return Empty" - {
        "when no arrivals returned" in {
          val responseJson = Json.parse("""
              |{
              |  "_links": {
              |    "self": {
              |      "href": "/customs/transits/movements/arrivals"
              |    }
              |  },
              |  "totalCount": 0,
              |  "arrivals": []
              |}
              |""".stripMargin)

          server.stubFor(
            get(urlEqualTo("/movements/arrivals?count=1"))
              .willReturn(okJson(responseJson.toString()))
          )

          connector.getAvailability().futureValue mustBe Availability.Empty
        }
      }

      "must return Unavailable" - {
        "when there is an error" in {
          forAll(genError) {
            error =>
              server.stubFor(
                get(urlEqualTo("/movements/arrivals?count=1"))
                  .willReturn(aResponse().withStatus(error))
              )

              connector.getAvailability().futureValue mustBe Availability.Unavailable
          }
        }
      }
    }
  }

}
