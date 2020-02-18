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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, okJson, urlEqualTo}
import helper.WireMockServerHandler
import models.Movement
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import viewModels.ViewArrivalMovement

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



class DestinationConnectorSpec extends SpecBase with WireMockServerHandler with ScalaCheckPropertyChecks {

  private lazy val connector: DestinationConnector = app.injector.instanceOf[DestinationConnector]
  private val startUrl = "transit-movements-trader-at-destination"

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.destination.port" -> server.port()
    )
    .build()

  private val responseJson  = {
    """
      | {
      |     "01-01-2020":[
      |     {
      |       "updated":"test updated",
      |       "mrn":"test mrn",
      |       "traderName":"test name",
      |       "office":"test office",
      |       "procedure":"test procedure",
      |       "status":"test status",
      |       "actions":["test actions"]
      |      }
      |     ],
      |    "02-01-2020":[
      |     {
      |       "updated":"test updated",
      |       "mrn":"test mrn",
      |       "traderName":"test name",
      |       "office":"test office",
      |       "procedure":"test procedure",
      |       "status":"test status",
      |       "actions":["test actions"]
      |      }
      |    ]
      | }
      |""".stripMargin
  }

  val errorResponses: Gen[Int] = Gen.chooseNum(400, 599)


  "DestinationConnector" - {
    "must return a successful future response with a view arrival movement" in {

      val expectedResult = {
        ViewArrivalMovement(
          Map(
            "01-01-2020" -> {
              Seq(Movement("test updated", "test mrn", "test name", "test office", "test procedure", "test status", Seq("test actions")))
            },
            "02-01-2020" -> {
              Seq(Movement("test updated", "test mrn", "test name", "test office", "test procedure", "test status", Seq("test actions")))
            }
          )
        )
      }

      server.stubFor(
        get(urlEqualTo(s"/$startUrl/messages"))
          .willReturn(okJson(responseJson)
        )
      )

      connector.getArrivalMovements.futureValue mustBe expectedResult
    }

    "must return an exception when an error response is returned from getCountryList" in {

      checkErrorResponse(s"/$startUrl/messages", connector.getArrivalMovements)
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