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
import com.github.tomakehurst.wiremock.client.WireMock._
import generators.Generators
import helper.WireMockServerHandler
import models.departure.drafts.Limit
import models.{DepartureUserAnswerSummary, DeparturesSummary, DraftAvailability, LocalReferenceNumber}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.OK

import java.time.LocalDateTime

class DeparturesMovementsP5ConnectorSpec extends SpecBase with WireMockServerHandler with ScalaCheckPropertyChecks with Generators {

  private lazy val connector: DeparturesMovementsP5Connector = app.injector.instanceOf[DeparturesMovementsP5Connector]
  private val startUrl                                       = "manage-transit-movements-departure-cache"

  private val errorResponses: Gen[Int] = Gen.chooseNum(400, 599).suchThat(_ != 404)

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.drafts-repository.port" -> server.port())

  private val createdAt = LocalDateTime.now()

  "DeparturesMovementConnector" - {

    "getDeparturesSummary" - {

      "must return DeparturesSummary when given successful response" in {

        val expectedResult = DeparturesSummary(
          0,
          0,
          List(
            DepartureUserAnswerSummary(LocalReferenceNumber("AB123"), createdAt, 29),
            DepartureUserAnswerSummary(LocalReferenceNumber("CD123"), createdAt, 28)
          )
        )

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/user-answers"))
            .willReturn(okJson(Json.prettyPrint(Json.toJson(expectedResult))))
        )

        connector.getDeparturesSummary().futureValue.value mustBe expectedResult
      }

      "must return DeparturesSummary when given successful response with empty user answers" in {

        val expectedResult = DeparturesSummary(0, 0, List.empty)

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/user-answers"))
            .willReturn(okJson(Json.prettyPrint(Json.toJson(expectedResult))))
        )

        connector.getDeparturesSummary().futureValue.value mustBe expectedResult
      }

      "must return none on failure" in {
        errorResponses.map {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(s"/$startUrl/user-answers"))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )

            val expectedResult = None
            connector.getDeparturesSummary().futureValue mustBe expectedResult
        }
      }
    }

    "lrnFuzzySearch" - {

      val maxSearchResults = 100
      val partialLRN       = "123"

      "must return DeparturesSummary when given successful response" in {

        val expectedResult = DeparturesSummary(
          0,
          0,
          List(
            DepartureUserAnswerSummary(LocalReferenceNumber("AB123"), createdAt, 29),
            DepartureUserAnswerSummary(LocalReferenceNumber("CD123"), createdAt, 28)
          )
        )

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/user-answers?lrn=$partialLRN&limit=$maxSearchResults"))
            .willReturn(okJson(Json.prettyPrint(Json.toJson(expectedResult))))
        )

        connector.lrnFuzzySearch(partialLRN, Limit(maxSearchResults)).futureValue.value mustBe expectedResult
      }

      "must return none on failure" in {
        errorResponses.map {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(s"/$startUrl/user-answers?lrn=$partialLRN&limit=$maxSearchResults"))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )

            val expectedResult = None
            connector.lrnFuzzySearch(partialLRN, Limit(maxSearchResults)).futureValue mustBe expectedResult

        }
      }
    }

    "deleteDraftDeparture" - {
      val lrn = "1234"
      "must return 200 on a successful deletion" in {
        server.stubFor(
          delete(urlEqualTo(s"/$startUrl/user-answers/$lrn"))
            .willReturn(
              aResponse()
                .withStatus(200)
            )
        )

        connector.deleteDraftDeparture(lrn).futureValue.status mustBe 200
      }

      "must return 500 on a failed deletion" in {
        server.stubFor(
          delete(urlEqualTo(s"/$startUrl/user-answers/$lrn"))
            .willReturn(
              aResponse()
                .withStatus(500)
            )
        )

        connector.deleteDraftDeparture(lrn).futureValue.status mustBe 500
      }
    }

    "getDraftDeparturesAvailability" - {

      "must return NonEmpty when given successful response" in {
        val expectedResult = DeparturesSummary(
          0,
          0,
          List(
            DepartureUserAnswerSummary(LocalReferenceNumber("AB123"), createdAt, 29),
            DepartureUserAnswerSummary(LocalReferenceNumber("CD123"), createdAt, 28)
          )
        )

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/user-answers?limit=1"))
            .willReturn(okJson(Json.prettyPrint(Json.toJson(expectedResult))))
        )

        connector.getDraftDeparturesAvailability().futureValue mustBe DraftAvailability.NonEmpty
      }

      "must return Empty when given a not found response" in {

        val expectedResult = DeparturesSummary(0, 0, List.empty)

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/user-answers?limit=1"))
            .willReturn(okJson(Json.prettyPrint(Json.toJson(expectedResult))))
        )

        connector.getDraftDeparturesAvailability().futureValue mustBe DraftAvailability.Empty
      }

      "must return unavailable when given an error response" in {

        errorResponses.map {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(s"/$startUrl/user-answers?limit=1"))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )

            connector.getDraftDeparturesAvailability().futureValue mustBe DraftAvailability.Unavailable
        }
      }
    }

    "checkLock" - {

      val url = s"/manage-transit-movements-departure-cache/user-answers/$lrn/lock"

      "must return true when status is Ok" in {
        server.stubFor(get(urlEqualTo(url)) willReturn aResponse().withStatus(OK))

        val result: Boolean = connector.checkLock(lrn.value).futureValue

        result mustBe true
      }

      "return false for other responses" in {

        val errorResponses: Gen[Int] = Gen
          .chooseNum(400: Int, 599: Int)

        forAll(errorResponses) {
          error =>
            server.stubFor(
              get(urlEqualTo(url))
                .willReturn(aResponse().withStatus(error))
            )

            val result: Boolean = connector.checkLock(lrn.value).futureValue

            result mustBe false
        }
      }
    }

  }

}
