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

import com.github.tomakehurst.wiremock.client.WireMock.*
import generators.Generators
import itbase.{ItSpecBase, WireMockServerHandler}
import models.Sort.{SortByCreatedAtAsc, SortByCreatedAtDesc, SortByLRNAsc, SortByLRNDesc}
import models.departure.drafts.{Limit, Skip}
import models.{Availability, DepartureUserAnswerSummary, DeparturesSummary, LocalReferenceNumber, LockCheck}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import java.time.LocalDateTime

class DeparturesDraftsP5ConnectorSpec extends ItSpecBase with WireMockServerHandler with ScalaCheckPropertyChecks with Generators {

  private lazy val connector: DeparturesDraftsP5Connector = app.injector.instanceOf[DeparturesDraftsP5Connector]
  private val startUrl                                    = "manage-transit-movements-departure-cache"

  private val errorResponses4xx: Gen[Int] = Gen.chooseNum(400: Int, 499: Int)
  private val errorResponses5xx: Gen[Int] = Gen.chooseNum(500: Int, 599: Int)
  private val errorResponses: Gen[Int]    = Gen.oneOf(errorResponses4xx, errorResponses5xx)

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.manage-transit-movements-departure-cache.port" -> server.port())

  private val createdAt = LocalDateTime.now()

  "DeparturesMovementConnector" - {

    "sortDraftDepartures" - {

      val skip  = 1
      val limit = 100

      "must return Departure Summary sorted by LRN ascending when given successful response" in {

        val expectedResult = DeparturesSummary(
          0,
          0,
          List(
            DepartureUserAnswerSummary(LocalReferenceNumber("AB123"), createdAt, 29),
            DepartureUserAnswerSummary(LocalReferenceNumber("CD123"), createdAt, 28),
            DepartureUserAnswerSummary(LocalReferenceNumber("DE123"), createdAt, 27),
            DepartureUserAnswerSummary(LocalReferenceNumber("EF123"), createdAt, 26)
          )
        )

        val departuresUserAnswers: List[DepartureUserAnswerSummary] = expectedResult.userAnswers

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/user-answers?limit=$limit&skip=$skip&sortBy=lrn.asc&state=notSubmitted"))
            .willReturn(okJson(Json.prettyPrint(Json.toJson(expectedResult))))
        )

        val result =
          connector.sortDraftDepartures(sortParams = SortByLRNAsc, limit = Limit(limit), skip = Skip(skip))
        val resultsDeparturesUserAnswers: Seq[DepartureUserAnswerSummary] = result.futureValue.value.userAnswers

        resultsDeparturesUserAnswers.head `mustBe` departuresUserAnswers.head
        resultsDeparturesUserAnswers(1) `mustBe` departuresUserAnswers(1)
        resultsDeparturesUserAnswers(2) `mustBe` departuresUserAnswers(2)
        resultsDeparturesUserAnswers(3) `mustBe` departuresUserAnswers(3)
      }
      "must return Departure Summary sorted by LRN descending when given successful response" in {

        val expectedResult = DeparturesSummary(
          0,
          0,
          List(
            DepartureUserAnswerSummary(LocalReferenceNumber("EF123"), createdAt, 26),
            DepartureUserAnswerSummary(LocalReferenceNumber("DE123"), createdAt, 27),
            DepartureUserAnswerSummary(LocalReferenceNumber("CD123"), createdAt, 28),
            DepartureUserAnswerSummary(LocalReferenceNumber("AB123"), createdAt, 29)
          )
        )
        val departuresUserAnswers: List[DepartureUserAnswerSummary] = expectedResult.userAnswers

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/user-answers?limit=$limit&skip=$skip&sortBy=lrn.dsc&state=notSubmitted"))
            .willReturn(okJson(Json.prettyPrint(Json.toJson(expectedResult))))
        )

        val result =
          connector.sortDraftDepartures(SortByLRNDesc, limit = Limit(limit), skip = Skip(skip))
        val resultsDeparturesUserAnswers: Seq[DepartureUserAnswerSummary] = result.futureValue.value.userAnswers

        resultsDeparturesUserAnswers.head `mustBe` departuresUserAnswers.head
        resultsDeparturesUserAnswers(1) `mustBe` departuresUserAnswers(1)
        resultsDeparturesUserAnswers(2) `mustBe` departuresUserAnswers(2)
        resultsDeparturesUserAnswers(3) `mustBe` departuresUserAnswers(3)
      }
      "must return Departure Summary sorted by CreatedAT ascending when given successful response" in {

        val expectedResult = DeparturesSummary(
          0,
          0,
          List(
            DepartureUserAnswerSummary(LocalReferenceNumber("AB123"), createdAt, 29),
            DepartureUserAnswerSummary(LocalReferenceNumber("CD123"), createdAt, 28),
            DepartureUserAnswerSummary(LocalReferenceNumber("DE123"), createdAt, 27),
            DepartureUserAnswerSummary(LocalReferenceNumber("EF123"), createdAt, 26)
          )
        )

        val departuresUserAnswers: List[DepartureUserAnswerSummary] = expectedResult.userAnswers

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/user-answers?limit=$limit&skip=$skip&sortBy=createdAt.asc&state=notSubmitted"))
            .willReturn(okJson(Json.prettyPrint(Json.toJson(expectedResult))))
        )

        val result =
          connector.sortDraftDepartures(SortByCreatedAtAsc, limit = Limit(limit), skip = Skip(skip))
        val resultsDeparturesUserAnswers: Seq[DepartureUserAnswerSummary] = result.futureValue.value.userAnswers

        resultsDeparturesUserAnswers.head `mustBe` departuresUserAnswers.head
        resultsDeparturesUserAnswers(1) `mustBe` departuresUserAnswers(1)
        resultsDeparturesUserAnswers(2) `mustBe` departuresUserAnswers(2)
        resultsDeparturesUserAnswers(3) `mustBe` departuresUserAnswers(3)
      }
      "must return Departure Summary sorted by createdAt descending when given successful response" in {

        val expectedResult = DeparturesSummary(
          0,
          0,
          List(
            DepartureUserAnswerSummary(LocalReferenceNumber("EF123"), createdAt, 26),
            DepartureUserAnswerSummary(LocalReferenceNumber("DE123"), createdAt, 27),
            DepartureUserAnswerSummary(LocalReferenceNumber("CD123"), createdAt, 28),
            DepartureUserAnswerSummary(LocalReferenceNumber("AB123"), createdAt, 29)
          )
        )
        val departuresUserAnswers: List[DepartureUserAnswerSummary] = expectedResult.userAnswers

        server.stubFor(
          get(urlEqualTo(s"/$startUrl/user-answers?limit=$limit&skip=$skip&sortBy=createdAt.dsc&state=notSubmitted"))
            .willReturn(okJson(Json.prettyPrint(Json.toJson(expectedResult))))
        )

        val result =
          connector.sortDraftDepartures(SortByCreatedAtDesc, limit = Limit(limit), skip = Skip(skip))
        val resultsDeparturesUserAnswers: Seq[DepartureUserAnswerSummary] = result.futureValue.value.userAnswers

        resultsDeparturesUserAnswers.head `mustBe` departuresUserAnswers.head
        resultsDeparturesUserAnswers(1) `mustBe` departuresUserAnswers(1)
        resultsDeparturesUserAnswers(2) `mustBe` departuresUserAnswers(2)
        resultsDeparturesUserAnswers(3) `mustBe` departuresUserAnswers(3)
      }

      "must return none for 4xx/5xx" in {
        forAll(errorResponses) {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(s"/$startUrl/user-answers?limit=$limit&skip=$skip&sortBy=lrn.asc&state=notSubmitted"))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )

            val expectedResult = None
            connector.sortDraftDepartures(SortByLRNAsc, limit = Limit(limit), skip = Skip(skip)).futureValue `mustBe` expectedResult
        }
      }
    }

    "lrnFuzzySearch" - {
      val maxSearchResults = 100
      val partialLRN       = "123"
      val url              = s"/$startUrl/user-answers?lrn=$partialLRN&limit=$maxSearchResults&state=notSubmitted"

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
          get(urlEqualTo(url))
            .willReturn(okJson(Json.prettyPrint(Json.toJson(expectedResult))))
        )

        connector.lrnFuzzySearch(partialLRN, Limit(maxSearchResults)).futureValue.value `mustBe` expectedResult
      }

      "must return none for 4xx/5xx response" in {
        forAll(errorResponses) {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(url))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )

            val expectedResult = None
            connector.lrnFuzzySearch(partialLRN, Limit(maxSearchResults)).futureValue `mustBe` expectedResult
        }
      }
    }

    "deleteDraftDeparture" - {
      val lrn = "1234"
      val url = s"/$startUrl/user-answers/$lrn"

      "must return 200 on a successful deletion" in {
        server.stubFor(
          delete(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(200)
            )
        )

        connector.deleteDraftDeparture(lrn).futureValue.status `mustBe` 200
      }

      "must return 500 on a failed deletion" in {
        server.stubFor(
          delete(urlEqualTo(url))
            .willReturn(
              aResponse()
                .withStatus(500)
            )
        )

        connector.deleteDraftDeparture(lrn).futureValue.status `mustBe` 500
      }
    }

    "getDraftDeparturesAvailability" - {
      val url = s"/$startUrl/user-answers?limit=1&state=notSubmitted"

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
          get(urlEqualTo(url))
            .willReturn(okJson(Json.prettyPrint(Json.toJson(expectedResult))))
        )

        connector.getDraftDeparturesAvailability().futureValue `mustBe` Availability.NonEmpty
      }

      "must return Empty when given a not found response" in {

        val expectedResult = DeparturesSummary(0, 0, List.empty)

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(okJson(Json.prettyPrint(Json.toJson(expectedResult))))
        )

        connector.getDraftDeparturesAvailability().futureValue `mustBe` Availability.Empty
      }

      "must return unavailable for 4xx/5xx response" in {
        forAll(errorResponses) {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(url))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )

            connector.getDraftDeparturesAvailability().futureValue `mustBe` Availability.Unavailable
        }
      }
    }

    "checkLock" - {
      val url = s"/manage-transit-movements-departure-cache/user-answers/$lrn/lock"

      "must return Unlocked when status is Ok (200)" in {
        server.stubFor(get(urlEqualTo(url)) `willReturn` aResponse().withStatus(OK))

        val result: LockCheck = connector.checkLock(lrn.value).futureValue

        result `mustBe` LockCheck.Unlocked
      }

      "must return Locked when status is Locked (423)" in {
        server.stubFor(get(urlEqualTo(url)) `willReturn` aResponse().withStatus(LOCKED))

        val result: LockCheck = connector.checkLock(lrn.value).futureValue

        result `mustBe` LockCheck.Locked
      }

      "return LockCheckFailure for other 4xx/5xx responses" in {

        forAll(Gen.choose(400: Int, 599: Int).retryUntil(_ != LOCKED)) {
          errorStatus =>
            server.stubFor(get(urlEqualTo(url)) `willReturn` aResponse().withStatus(errorStatus))

            val result: LockCheck = connector.checkLock(lrn.value).futureValue

            result `mustBe` LockCheck.LockCheckFailure
        }
      }
    }
  }

}
