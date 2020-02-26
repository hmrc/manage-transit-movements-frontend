/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.{LocalDate, LocalTime}

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import helper.WireMockServerHandler
import models.Movement
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DestinationConnectorSpec
    extends SpecBase
    with WireMockServerHandler
    with ScalaCheckPropertyChecks {

  private lazy val connector: DestinationConnector =
    app.injector.instanceOf[DestinationConnector]
  private val startUrl = "transit-movements-trader-at-destination"

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(conf = "microservice.services.destination.port" -> server.port())
    .build()

  private val localDate = LocalDate.now()
  private val localTime = LocalTime.now()

  private val responseJson: JsArray =
    Json.arr(
      Json.obj(
        "date" -> localDate,
        "time" -> localTime,
        "message" -> Json.obj(
          "procedure" -> "normal",
          "movementReferenceNumber" -> "test mrn",
          "notificationPlace" -> "test place",
          "notificationDate" -> "2020-02-20",
          "customsSubPlace" -> "test sub place",
          "trader" -> Json.obj(
            "eori" -> "test eori",
            "name" -> "test name",
            "streetAndNumber" -> "test street",
            "postCode" -> "test postcode",
            "city" -> "test city",
            "countryCode" -> "GB"
          ),
          "presentationOffice" -> "test presentation office",
          "enRouteEvents" -> Json.arr(
            Json.obj(
              "place" -> "test place",
              "countryCode" -> "test country code",
              "alreadyInNcts" -> true,
              "eventDetails" ->
                Json.obj(
                  "transportIdentity" -> "test transport identity",
                  "transportCountry" -> "test transport countru",
                  "containers" -> Json
                    .arr(Json.obj("containerNumber" -> "test container"))
                )
            )
          )
        )
      ),
      Json.obj(
        "date" -> localDate,
        "time" -> localTime,
        "message" -> Json.obj(
          "procedure" -> "normal",
          "movementReferenceNumber" -> "test mrn",
          "notificationPlace" -> "test place",
          "notificationDate" -> "2020-02-20",
          "customsSubPlace" -> "test sub place",
          "trader" -> Json.obj(
            "eori" -> "test eori",
            "name" -> "test name",
            "streetAndNumber" -> "test street",
            "postCode" -> "test postcode",
            "city" -> "test city",
            "countryCode" -> "GB"
          ),
          "presentationOffice" -> "test presentation office",
          "enRouteEvents" -> Json.arr(
            Json.obj(
              "place" -> "test place",
              "countryCode" -> "test country code",
              "alreadyInNcts" -> true,
              "eventDetails" ->
                Json.obj(
                  "transportIdentity" -> "test transport identity",
                  "transportCountry" -> "test transport countru",
                  "containers" -> Json
                    .arr(Json.obj("containerNumber" -> "test container"))
                )
            )
          )
        )
      )
    )

  val errorResponses: Gen[Int] = Gen.chooseNum(400, 599)

  "DestinationConnector" - {

    "getArrivalMovements" - {

      "must return a successful future response with a view arrival movement" in {

        val expectedResult = {
          Seq(
            Movement(
              localDate,
              localTime,
              "test mrn",
              "test name",
              "test presentation office",
              "normal"
            ),
            Movement(
              localDate,
              localTime,
              "test mrn",
              "test name",
              "test presentation office",
              "normal"
            )
          )
        }

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/arrivals-history"))
            .willReturn(okJson(responseJson.toString()))
        )

        connector.getArrivalMovements.futureValue mustBe expectedResult
      }

      "must return an exception when an error response is returned from getCountryList" in {

        checkErrorResponse(
          s"/$startUrl/arrivals-history",
          connector.getArrivalMovements
        )
      }
    }

    "getArrivalMovement" - {

      val expectedResult = Movement(
        localDate,
        localTime,
        "test mrn",
        "test name",
        "test presentation office",
        "normal"
      )

      server.stubFor(
        get(urlEqualTo(s"/$startUrl/arrivals-history"))
          .willReturn(okJson(responseJson.toString()))
      )

      connector.getArrivalMovements.futureValue mustBe expectedResult

    }
  }

  private def checkErrorResponse(url: String, result: Future[_]): Assertion =
    forAll(errorResponses) { errorResponse =>
      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(errorResponse)
          )
      )

      whenReady(result.failed) {
        _ mustBe an[Exception]
      }
    }
}
