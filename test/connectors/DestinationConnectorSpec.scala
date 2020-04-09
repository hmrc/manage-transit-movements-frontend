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

import java.time.LocalDateTime

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import helper.WireMockServerHandler
import models.{Arrival, ArrivalDateTime, Arrivals}
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.concurrent.Future

class DestinationConnectorSpec extends SpecBase with WireMockServerHandler with ScalaCheckPropertyChecks {

  private lazy val connector: DestinationConnector =
    app.injector.instanceOf[DestinationConnector]
  private val startUrl = "transit-movements-trader-at-destination"

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(conf = "microservice.services.destination.port" -> server.port())
    .build()

  private val localDateTime = LocalDateTime.now()

  private val arrivalsResponseJson =
    Json.obj(
      "arrivals" ->
        Json.arr(
          Json.obj(
            "created" -> {
              Json.obj(
                "dateTime" -> localDateTime
              )
            },
            "updated" -> {
              Json.obj(
                "dateTime" -> localDateTime
              )
            },
            "state"                   -> "Submitted",
            "movementReferenceNumber" -> "test mrn"
          )
        )
    )

  val errorResponses: Gen[Int] = Gen.chooseNum(400, 599)

  "DestinationConnector" - {

    "getArrivals" - {
      "must return a successful future response" in {
        val expectedResult = {
          Arrivals(
            Seq(
              Arrival(
                ArrivalDateTime(localDateTime),
                ArrivalDateTime(localDateTime),
                "Submitted",
                "test mrn"
              )
            )
          )
        }

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/movements/arrivals"))
            .willReturn(okJson(arrivalsResponseJson.toString()))
        )

        connector.getArrivals.futureValue mustBe expectedResult
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
