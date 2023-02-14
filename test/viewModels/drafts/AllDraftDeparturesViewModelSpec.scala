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

package viewModels.drafts

import base.SpecBase
import generators.Generators
import models.Sort.{SortByCreatedAtAsc, SortByCreatedAtDesc, SortByLRNAsc, SortByLRNDesc}
import models.{DepartureUserAnswerSummary, DeparturesSummary}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class AllDraftDeparturesViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "AllDraftDeparturesViewModel" - {

    "must return correct data rows" in {

      val userAnswerSummary: Gen[List[DepartureUserAnswerSummary]] = Gen.listOfN(2, arbitrary[DepartureUserAnswerSummary])

      forAll(userAnswerSummary) {
        userAnswerSummary =>
          val draftDeparture = DeparturesSummary(userAnswerSummary)

          val viewModel = AllDraftDeparturesViewModel(draftDeparture, 1, None, frontendAppConfig.draftDepartureFrontendUrl)

          viewModel.dataRows.length mustBe draftDeparture.userAnswers.length

          viewModel.dataRows.head.lrn mustBe draftDeparture.userAnswers.head.lrn.toString
          viewModel.dataRows(1).lrn mustBe draftDeparture.userAnswers(1).lrn.toString

          viewModel.dataRows.head.daysRemaining mustBe draftDeparture.userAnswers.head.expiresInDays
          viewModel.dataRows(1).daysRemaining mustBe draftDeparture.userAnswers(1).expiresInDays
      }
    }

    "tooManyResult" - {

      val userAnswerSummary: List[DepartureUserAnswerSummary] = Gen.listOfN(2, arbitrary[DepartureUserAnswerSummary]).sample.value
      val departuresSummary: DeparturesSummary                = DeparturesSummary(userAnswerSummary)

      "must return true when departure size is greater than page size" in {

        val viewModel =
          AllDraftDeparturesViewModel(departuresSummary, departuresSummary.userAnswers.length - 1, None, frontendAppConfig.draftDepartureFrontendUrl)

        viewModel.tooManyResults mustBe true
      }

      "must return false when departure size is less than or equal to page size" in {

        val viewModel =
          AllDraftDeparturesViewModel(departuresSummary, departuresSummary.userAnswers.length + 1, None, frontendAppConfig.draftDepartureFrontendUrl)

        viewModel.tooManyResults mustBe false
      }
    }

    "isSearch" - {

      val userAnswerSummary: List[DepartureUserAnswerSummary] = Gen.listOfN(2, arbitrary[DepartureUserAnswerSummary]).sample.value
      val departuresSummary: DeparturesSummary                = DeparturesSummary(userAnswerSummary)

      "must return true when LRN is defined" in {

        val viewModel =
          AllDraftDeparturesViewModel(departuresSummary, departuresSummary.userAnswers.length, Some("AB123"), frontendAppConfig.draftDepartureFrontendUrl)

        viewModel.isSearch mustBe true
      }

      "must return false when LRN is not defined" in {

        val viewModel = AllDraftDeparturesViewModel(departuresSummary, departuresSummary.userAnswers.length, None, frontendAppConfig.draftDepartureFrontendUrl)

        viewModel.isSearch mustBe false
      }

    }

    "resultsFound" - {

      "must return true when data rows is not empty" in {

        val userAnswerSummary: List[DepartureUserAnswerSummary] = Gen.listOfN(2, arbitrary[DepartureUserAnswerSummary]).sample.value
        val departuresSummary: DeparturesSummary                = DeparturesSummary(userAnswerSummary)

        val viewModel = AllDraftDeparturesViewModel(departuresSummary, departuresSummary.userAnswers.length, None, frontendAppConfig.draftDepartureFrontendUrl)

        viewModel.resultsFound mustBe true
      }

      "must return false when data rows is empty" in {

        val departuresSummary: DeparturesSummary = DeparturesSummary(List.empty)

        val viewModel = AllDraftDeparturesViewModel(departuresSummary, departuresSummary.userAnswers.length, None, frontendAppConfig.draftDepartureFrontendUrl)

        viewModel.resultsFound mustBe false
      }
    }

    "searchResultsFound" - {

      "must return true when LRN is defined and rows is not empty" in {

        val userAnswerSummary: List[DepartureUserAnswerSummary] = Gen.listOfN(2, arbitrary[DepartureUserAnswerSummary]).sample.value
        val departuresSummary: DeparturesSummary                = DeparturesSummary(userAnswerSummary)

        val viewModel =
          AllDraftDeparturesViewModel(departuresSummary, departuresSummary.userAnswers.length, Some("AB123"), frontendAppConfig.draftDepartureFrontendUrl)

        viewModel.searchResultsFound mustBe true
      }

      "must return false when LRN is not defined" in {

        val userAnswerSummary: List[DepartureUserAnswerSummary] = Gen.listOfN(2, arbitrary[DepartureUserAnswerSummary]).sample.value
        val departuresSummary: DeparturesSummary                = DeparturesSummary(userAnswerSummary)

        val viewModel = AllDraftDeparturesViewModel(departuresSummary, departuresSummary.userAnswers.length, None, frontendAppConfig.draftDepartureFrontendUrl)

        viewModel.searchResultsFound mustBe false
      }

      "must return false when rows are empty" in {

        val departuresSummary: DeparturesSummary = DeparturesSummary(List.empty)

        val viewModel = AllDraftDeparturesViewModel(departuresSummary, departuresSummary.userAnswers.length, None, frontendAppConfig.draftDepartureFrontendUrl)

        viewModel.searchResultsFound mustBe false
      }
    }

    "sortParams" - {
      val departuresSummary = arbitrary[DeparturesSummary].sample.value
      "when sortParams is SortByLRNAsc" in {
        val sortParams = SortByLRNAsc
        val viewModel  = AllDraftDeparturesViewModel(departuresSummary, 1, None, frontendAppConfig.draftDepartureFrontendUrl, sortParams = Some(sortParams))
        viewModel.sortLrn mustBe "ascending"
        viewModel.sortCreatedAt mustBe "none"

      }

      "when sortParams is SortByLRNDesc" in {
        val sortParams = SortByLRNDesc
        val viewModel  = AllDraftDeparturesViewModel(departuresSummary, 1, None, frontendAppConfig.draftDepartureFrontendUrl, sortParams = Some(sortParams))
        viewModel.sortLrn mustBe "descending"
        viewModel.sortCreatedAt mustBe "none"

      }

      "when sortParams is SortByCreatedAtAsc" in {
        val sortParams = SortByCreatedAtAsc
        val viewModel  = AllDraftDeparturesViewModel(departuresSummary, 1, None, frontendAppConfig.draftDepartureFrontendUrl, sortParams = Some(sortParams))
        viewModel.sortCreatedAt mustBe "ascending"
        viewModel.sortLrn mustBe "none"

      }

      "when sortParams is SortByCreatedAtDesc" in {
        val sortParams = SortByCreatedAtDesc
        val viewModel  = AllDraftDeparturesViewModel(departuresSummary, 1, None, frontendAppConfig.draftDepartureFrontendUrl, sortParams = Some(sortParams))
        viewModel.sortCreatedAt mustBe "descending"
        viewModel.sortLrn mustBe "none"

      }

      "when sortParams is None" in {
        val viewModel = AllDraftDeparturesViewModel(departuresSummary, 1, None, frontendAppConfig.draftDepartureFrontendUrl)
        viewModel.sortCreatedAt mustBe "descending"
        viewModel.sortLrn mustBe "none"

      }
    }
  }
}
