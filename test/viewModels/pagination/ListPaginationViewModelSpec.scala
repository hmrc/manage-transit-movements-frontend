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

class ListPaginationViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private def intGen: Gen[Int] = Gen.choose(2, 10: Int)

  "ListPaginationViewModel" - {
    "apply" - {

      "next" - {

        "must return some when current page is less than the total number of pages" in {
          ListPaginationViewModel(10, 2, 2, "testHref").next.isDefined `mustBe` true
        }

        "must pass aria label to next page attribute" in {
          ListPaginationViewModel(10, 2, 2, "testHref", Seq.empty, Some("Heading")).next.get.attributes("aria-label") `mustBe` "Next page of heading"
        }

        "must not pass aria label to next page if template does not exist" in {
          ListPaginationViewModel(10, 2, 2, "testHref", Seq.empty, None).next.get.attributes.get("aria-label") `mustBe` None
        }

        "must pass aria label to previous page attribute" in {
          ListPaginationViewModel(10, 2, 2, "testHref", Seq.empty, Some("Heading")).previous.get.attributes("aria-label") `mustBe` "Previous page of heading"
        }

        "must not pass aria label to previous page if template does not exist" in {
          ListPaginationViewModel(10, 2, 2, "testHref", Seq.empty, None).previous.get.attributes.get("aria-label") `mustBe` None
        }

        "must return None when current page is not less than the total number of pages" in {
          ListPaginationViewModel(10, 5, 2, "testHref").next.isDefined `mustBe` false
        }
      }

      "previous" - {

        "must return some when current page is greater than 1" in {
          ListPaginationViewModel(10, 2, 2, "testHref").previous.isDefined `mustBe` true
        }

        "must return none when current page is not greater than 1" in {
          ListPaginationViewModel(10, 1, 2, "testHref").previous.isDefined `mustBe` false
        }
      }

      "items" - {

        "must return [1] 2 … 100 when on page 1 of 100" in {
          val result = ListPaginationViewModel(1000, 1, 10, "href").items

          result `mustBe` Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(true)),
            PaginationItem(s"href?page=2", Some("2"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(false))
          )
        }

        "must return 1 [2] 3 … 100 when on page 2 of 100" in {
          val result = ListPaginationViewModel(1000, 2, 10, "href").items

          result `mustBe` Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem(s"href?page=2", Some("2"), current = Some(true)),
            PaginationItem(s"href?page=3", Some("3"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(false))
          )
        }

        "must return 1 2 [3] 4 … 100 when on page 3 of 100" in {
          val result = ListPaginationViewModel(1000, 3, 10, "href").items

          result `mustBe` Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem(s"href?page=2", Some("2"), current = Some(false)),
            PaginationItem(s"href?page=3", Some("3"), current = Some(true)),
            PaginationItem(s"href?page=4", Some("4"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(false))
          )
        }

        "must return 1 … 3 [4] 5 … 100 when on page 4 of 100" in {
          val result = ListPaginationViewModel(1000, 4, 10, "href").items

          result `mustBe` Seq(
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
          val result = ListPaginationViewModel(1000, 98, 10, "href").items

          result `mustBe` Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=97", Some("97"), current = Some(false)),
            PaginationItem(s"href?page=98", Some("98"), current = Some(true)),
            PaginationItem(s"href?page=99", Some("99"), current = Some(false)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(false))
          )
        }

        "must return 1 … 98 [99] 100 when on page 99 of 100" in {
          val result = ListPaginationViewModel(1000, 99, 10, "href").items

          result `mustBe` Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=98", Some("98"), current = Some(false)),
            PaginationItem(s"href?page=99", Some("99"), current = Some(true)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(false))
          )
        }

        "must return 1 … 99 [100] when on page 100 of 100" in {
          val result = ListPaginationViewModel(1000, 100, 10, "href").items

          result `mustBe` Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=99", Some("99"), current = Some(false)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(true))
          )
        }

        "must return 1 [2] 3 when on page 2 of 3" in {
          val result = ListPaginationViewModel(30, 2, 10, "href").items

          result `mustBe` Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem(s"href?page=2", Some("2"), current = Some(true)),
            PaginationItem(s"href?page=3", Some("3"), current = Some(false))
          )
        }
      }

      "searchResult" - {
        "must show how many results found" - {
          "when 1 result found" in {
            forAll(arbitrary[ListPaginationViewModel]) {
              viewModel =>
                val result = viewModel
                  .copy(results = viewModel.results.copy(count = 1))
                  .searchResult(None)

                result `mustBe` "Showing <b>1</b> result"
            }
          }

          "when multiple results found" in {
            forAll(arbitrary[ListPaginationViewModel], intGen) {
              (viewModel, count) =>
                val result = viewModel
                  .copy(results = viewModel.results.copy(count = count))
                  .searchResult(None)

                result `mustBe` s"Showing <b>$count</b> results"
            }
          }
        }
      }

      "paginatedSearchResult" - {
        "must show how many results found" in {
          forAll(arbitrary[ListPaginationViewModel], intGen, intGen, intGen) {
            (viewModel, from, to, count) =>
              val result = viewModel
                .copy(results = viewModel.results.copy(from = from, to = to, count = count))
                .paginatedSearchResult(None)

              result `mustBe` s"Showing <b>$from</b> to <b>$to</b> of <b>$count</b> results"
          }
        }
      }
    }
  }

}
