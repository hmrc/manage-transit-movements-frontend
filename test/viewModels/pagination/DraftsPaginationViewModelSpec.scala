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

package viewModels.pagination

import base.SpecBase
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.PaginationItem

class DraftsPaginationViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private def intGen: Gen[Int] = Gen.choose(2, 10: Int)

  "DraftsPaginationViewModel" - {
    "apply" - {

      "next" - {

        "must return some when current page is less than the total number of pages" in {
          DraftsPaginationViewModel(10, 2, 2, "testHref").next.isDefined mustBe true
        }

        "must return None when current page is not less than the total number of pages" in {
          DraftsPaginationViewModel(10, 5, 2, "testHref").next.isDefined mustBe false
        }
      }

      "previous" - {

        "must return some when current page is greater than 1" in {
          DraftsPaginationViewModel(10, 2, 2, "testHref").previous.isDefined mustBe true
        }

        "must return none when current page is not greater than 1" in {
          DraftsPaginationViewModel(10, 1, 2, "testHref").previous.isDefined mustBe false
        }
      }

      "items" - {

        "must return [1] 2 … 100 when on page 1 of 100" in {
          val result = DraftsPaginationViewModel(1000, 1, 10, "href").items

          result mustBe Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(true)),
            PaginationItem(s"href?page=2", Some("2"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(false))
          )
        }

        "must return 1 [2] 3 … 100 when on page 2 of 100" in {
          val result = DraftsPaginationViewModel(1000, 2, 10, "href").items

          result mustBe Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem(s"href?page=2", Some("2"), current = Some(true)),
            PaginationItem(s"href?page=3", Some("3"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(false))
          )
        }

        "must return 1 2 [3] 4 … 100 when on page 3 of 100" in {
          val result = DraftsPaginationViewModel(1000, 3, 10, "href").items

          result mustBe Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem(s"href?page=2", Some("2"), current = Some(false)),
            PaginationItem(s"href?page=3", Some("3"), current = Some(true)),
            PaginationItem(s"href?page=4", Some("4"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(false))
          )
        }

        "must return 1 … 3 [4] 5 … 100 when on page 4 of 100" in {
          val result = DraftsPaginationViewModel(1000, 4, 10, "href").items

          result mustBe Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=3", Some("3"), current = Some(false)),
            PaginationItem(s"href?page=4", Some("4"), current = Some(true)),
            PaginationItem(s"href?page=5", Some("5"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(false))
          )
        }

        "must return 1 … 97 [98] 99 100 when on page 98 of 100" in {
          val result = DraftsPaginationViewModel(1000, 98, 10, "href").items

          result mustBe Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=97", Some("97"), current = Some(false)),
            PaginationItem(s"href?page=98", Some("98"), current = Some(true)),
            PaginationItem(s"href?page=99", Some("99"), current = Some(false)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(false))
          )
        }

        "must return 1 … 98 [99] 100 when on page 99 of 100" in {
          val result = DraftsPaginationViewModel(1000, 99, 10, "href").items

          result mustBe Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=98", Some("98"), current = Some(false)),
            PaginationItem(s"href?page=99", Some("99"), current = Some(true)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(false))
          )
        }

        "must return 1 … 99 [100] when on page 100 of 100" in {
          val result = DraftsPaginationViewModel(1000, 100, 10, "href").items

          result mustBe Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=99", Some("99"), current = Some(false)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(true))
          )
        }

        "must return 1 [2] 3 when on page 2 of 3" in {
          val result = DraftsPaginationViewModel(30, 2, 10, "href").items

          result mustBe Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem(s"href?page=2", Some("2"), current = Some(true)),
            PaginationItem(s"href?page=3", Some("3"), current = Some(false))
          )
        }
      }

      "searchResult" - {
        "must show how many results found" - {
          "when lrn is defined" - {
            "and 1 result found" in {
              forAll(arbitrary[DraftsPaginationViewModel], nonEmptyString) {
                (viewModel, lrn) =>
                  val result = viewModel
                    .copy(results = viewModel.results.copy(count = 1))
                    .copy(lrn = Some(lrn))
                    .searchResult

                  result mustBe s"Showing <b>1</b> result matching $lrn"
              }
            }

            "and multiple results found" in {
              forAll(arbitrary[DraftsPaginationViewModel], intGen, nonEmptyString) {
                (viewModel, count, lrn) =>
                  val result = viewModel
                    .copy(results = viewModel.results.copy(count = count))
                    .copy(lrn = Some(lrn))
                    .searchResult

                  result mustBe s"Showing <b>$count</b> results matching $lrn"
              }
            }
          }

          "when lrn is undefined" - {
            "and 1 result found" in {
              forAll(arbitrary[DraftsPaginationViewModel]) {
                viewModel =>
                  val result = viewModel
                    .copy(results = viewModel.results.copy(count = 1))
                    .copy(lrn = None)
                    .searchResult

                  result mustBe "Showing <b>1</b> result"
              }
            }

            "and multiple results found" in {
              forAll(arbitrary[DraftsPaginationViewModel], intGen) {
                (viewModel, count) =>
                  val result = viewModel
                    .copy(results = viewModel.results.copy(count = count))
                    .copy(lrn = None)
                    .searchResult

                  result mustBe s"Showing <b>$count</b> results"
              }
            }
          }
        }
      }

      "paginatedSearchResult" - {
        "must show how many results found" - {
          "when lrn is defined" in {
            forAll(arbitrary[DraftsPaginationViewModel], intGen, intGen, intGen, nonEmptyString) {
              (viewModel, from, to, count, lrn) =>
                val result = viewModel
                  .copy(results = viewModel.results.copy(from = from, to = to, count = count))
                  .copy(lrn = Some(lrn))
                  .paginatedSearchResult

                result mustBe s"Showing <b>$from</b> to <b>$to</b> of <b>$count</b> results matching $lrn"
            }
          }

          "when lrn is undefined" in {
            forAll(arbitrary[DraftsPaginationViewModel], intGen, intGen, intGen) {
              (viewModel, from, to, count) =>
                val result = viewModel
                  .copy(results = viewModel.results.copy(from = from, to = to, count = count))
                  .copy(lrn = None)
                  .paginatedSearchResult

                result mustBe s"Showing <b>$from</b> to <b>$to</b> of <b>$count</b> results"
            }
          }
        }
      }
    }
  }
}
