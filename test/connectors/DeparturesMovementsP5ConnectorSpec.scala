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
import models.{DepartureUserAnswerSummary, DeparturesSummary, DraftAvailability, LocalReferenceNumber}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

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

  private val summaryResponseJson =
    Json.obj(
      "eoriNumber" -> "1234567",
      "userAnswers" ->
        Json.arr(
          Json.obj(
            "lrn" -> "AB123",
            "_links" -> {
              "self" -> {
                "href" -> "/manage-transit-movements-departure-cache/user-answers/AB123"
              }
            },
            "createdAt"     -> createdAt.toString(),
            "lastUpdated"   -> "2023-01-27T08:43:17.064",
            "expiresInDays" -> 29,
            "_id"           -> "27e687a9-4544-4e22-937e-74e699d855f8"
          ),
          Json.obj(
            "lrn" -> "CD123",
            "_links" -> {
              "self" -> {
                "href" -> "/manage-transit-movements-departure-cache/user-answers/CD123 "
              }
            },
            "createdAt"     -> createdAt.toString(),
            "lastUpdated"   -> "2023-01-27T09:43:17.064",
            "expiresInDays" -> 28,
            "_id"           -> "27e687a9-4544-4e22-937e-74e699d855f8"
          )
        )
    )

  "DeparturesMovementConnector" - {

    "getDeparturesSummary" - {

      "must return DeparturesSummary when given successful response" in {

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/user-answers"))
            .willReturn(okJson(summaryResponseJson.toString()))
        )

        val expectedResult = DeparturesSummary(
          List(
            DepartureUserAnswerSummary(LocalReferenceNumber("AB123"), createdAt, 29),
            DepartureUserAnswerSummary(LocalReferenceNumber("CD123"), createdAt, 28)
          )
        )

        connector.getDeparturesSummary().futureValue.value mustBe expectedResult
      }

      "must return empty DeparturesSummary when not found" in {
        server.stubFor(
          get(urlEqualTo(s"/$startUrl/user-answers"))
            .willReturn(
              aResponse()
                .withStatus(404)
            )
        )

        val expectedResult = DeparturesSummary(List.empty)
        connector.getDeparturesSummary().futureValue.value mustBe expectedResult

      }
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

    "getDraftDeparturesAvailability" - {

      "must return NonEmpty when given successful response" in {
        server.stubFor(
          get(urlEqualTo(s"/$startUrl/user-answers?limit=1"))
            .willReturn(okJson(summaryResponseJson.toString()))
        )

        connector.getDraftDeparturesAvailability().futureValue mustBe DraftAvailability.NonEmpty
      }

      "must return Empty when given a not found response" in {
        server.stubFor(
          get(urlEqualTo(s"/$startUrl/user-answers?limit=1"))
            .willReturn(
              aResponse()
                .withStatus(404)
            )
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
  }

}
