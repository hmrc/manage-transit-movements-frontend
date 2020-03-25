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
import models.referenceData.Movement
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DestinationConnectorSpec extends SpecBase with WireMockServerHandler with ScalaCheckPropertyChecks {

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
        "date"                    -> localDate,
        "time"                    -> localTime,
        "movementReferenceNumber" -> "test mrn"
      ),
      Json.obj(
        "date"                    -> localDate,
        "time"                    -> localTime,
        "movementReferenceNumber" -> "test mrn"
      )
    )

  val errorResponses: Gen[Int] = Gen.chooseNum(400, 599)

  "DestinationConnector" - {

    "getMovements" - {

      "must return a successful future response with a view arrival movement" in {

        val expectedResult = {
          Seq(
            Movement(
              localDate,
              localTime,
              "test mrn"
            ),
            Movement(
              localDate,
              localTime,
              "test mrn"
            )
          )
        }

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/movements"))
            .willReturn(okJson(responseJson.toString()))
        )

        connector.getMovements.futureValue mustBe expectedResult
      }

      "must return an exception when an error response is returned from getCountryList" in {

        checkErrorResponse(
          s"/$startUrl/movements",
          connector.getMovements
        )
      }
    }
  }

  private def checkErrorResponse(url: String, result: Future[_]): Assertion =
    forAll(errorResponses) {
      errorResponse =>
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
