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
      |[
      | {
      |   "date":"2020-02-20",
      |   "time":"07:30:08.759",
      |   "message": {
      |     "procedure":"normal",
      |     "movementReferenceNumber":"test mrn",
      |     "notificationPlace":"test place",
      |     "notificationDate":"2020-02-20",
      |     "customsSubPlace":"test sub place",
      |     "trader": {
      |       "eori":"test eori",
      |       "name":"test name",
      |       "streetAndNumber":"test street",
      |       "postCode":"test postcode",
      |       "city":"test city",
      |       "countryCode":"GB"
      |    },
      |    "presentationOffice":"test presentation office",
      |    "enRouteEvents":[
      |     {
      |       "place":"test place",
      |       "countryCode":"test country code",
      |       "alreadyInNcts":true,
      |       "eventDetails":
      |         {
      |           "transportIdentity":"test transport identity",
      |           "transportCountry":"test transport countru",
      |           "containers":[{"containerNumber":"test container"}]
      |           }
      |         }
      |       ]
      |     }
      |  },
      |  {
      |   "date":"2020-02-20",
      |   "time":"07:30:08.759",
      |   "message": {
      |     "procedure":"normal",
      |     "movementReferenceNumber":"test mrn",
      |     "notificationPlace":"test place",
      |     "notificationDate":"2020-02-20",
      |     "customsSubPlace":"test sub place",
      |     "trader": {
      |       "eori":"test eori",
      |       "name":"test name",
      |       "streetAndNumber":"test street",
      |       "postCode":"test postcode",
      |       "city":"test city",
      |       "countryCode":"GB"
      |    },
      |    "presentationOffice":"test presentation office",
      |    "enRouteEvents":[
      |     {
      |       "place":"test place",
      |       "countryCode":"test country code",
      |       "alreadyInNcts":true,
      |       "eventDetails":
      |         {
      |           "transportIdentity":"test transport identity",
      |           "transportCountry":"test transport countru",
      |           "containers":[{"containerNumber":"test container"}]
      |           }
      |         }
      |       ]
      |     }
      |   }
      |]
      |""".stripMargin
  }

  val errorResponses: Gen[Int] = Gen.chooseNum(400, 599)


  "DestinationConnector" - {
    "must return a successful future response with a view arrival movement" in {

      val expectedResult = {
        Seq(
          ViewArrivalMovement(
            "2020-02-20",
            "07:30:08.759",
            Movement("test mrn", "test name", "test presentation office", "normal")
          ),
          ViewArrivalMovement(
            "2020-02-20",
            "07:30:08.759",
            Movement("test mrn", "test name", "test presentation office", "normal")
          )
        )
      }


      server.stubFor(
        get(urlEqualTo(s"/$startUrl/arrivals-history"))
          .willReturn(okJson(responseJson)
        )
      )

      connector.getArrivalMovements.futureValue mustBe expectedResult
    }

    "must return an exception when an error response is returned from getCountryList" in {

      checkErrorResponse(s"/$startUrl/arrivals-history", connector.getArrivalMovements)
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