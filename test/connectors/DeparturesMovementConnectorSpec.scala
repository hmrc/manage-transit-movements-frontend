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
import models.{Departure, DepartureId, Departures, LocalReferenceNumber}
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import scala.concurrent.Future

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
            .willReturn(okJson(departuresResponseJson.toString()))
        )

        connector.getDepartures().futureValue mustBe Some(expectedResult)
      }

      "must return a None when an error response is returned from getDepartures" in {

        forAll(errorResponses) {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(s"/$startUrl/movements/departures"))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )
            connector.getDepartures().futureValue mustBe None
        }
      }
    }
  }
}
