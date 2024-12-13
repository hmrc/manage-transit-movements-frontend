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
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc.Call
import play.api.test.Helpers.GET
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{PaginationItem, PaginationLink}

class PaginationViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private def intGen: Gen[Int] = Gen.choose(2, 10: Int)

  private class FakeViewModel(
    override val items: Seq[String],
    override val currentPage: Int,
    override val numberOfItemsPerPage: Int,
    override val searchParam: Option[String]
  ) extends PaginationViewModel[String] {

    override def href(page: Int): Call = {
      val url = searchParam match {
        case Some(value) => s"href?page=$page&foo=$value"
        case None        => s"href?page=$page"
      }
      Call(GET, url)
    }
    override val heading: String = "Example page heading"
  }

  "PaginationViewModel" - {
    "apply" - {

      "next" - {

        "must return some when current page is less than the total number of pages" in {
          val items     = Seq.fill(10: Int)("value")
          val viewModel = new FakeViewModel(items, 2, 2, Some("bar"))
          val expectedResult = PaginationLink(
            href = "href?page=3&foo=bar",
            text = None,
            labelText = None,
            attributes = Map("aria-label" -> "Next page of example page heading")
          )
          val result = viewModel.pagination.next
          result.value mustBe expectedResult
        }

        "must return None when current page is not less than the total number of pages" in {
          val items     = Seq.fill(10: Int)("value")
          val viewModel = new FakeViewModel(items, 5, 2, Some("bar"))
          val result    = viewModel.pagination.next
          result mustBe None
        }
      }

      "previous" - {

        "must return some when current page is greater than 1" in {
          val items     = Seq.fill(10: Int)("value")
          val viewModel = new FakeViewModel(items, 2, 2, Some("bar"))
          val expectedResult = PaginationLink(
            href = "href?page=1&foo=bar",
            text = None,
            labelText = None,
            attributes = Map("aria-label" -> "Previous page of example page heading")
          )
          val result = viewModel.pagination.previous
          result.value mustBe expectedResult
        }

        "must return none when current page is not greater than 1" in {
          val items     = Seq.fill(10: Int)("value")
          val viewModel = new FakeViewModel(items, 1, 2, Some("bar"))
          val result    = viewModel.pagination.previous
          result mustBe None
        }
      }

      "items" - {

        "must return [1] 2 … 10 when on page 1 of 10" in {
          val items     = Seq.fill(100: Int)("value")
          val viewModel = new FakeViewModel(items, 1, 10, None)
          val expectedResult = Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(true)),
            PaginationItem(s"href?page=2", Some("2"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=10", Some("10"), current = Some(false))
          )
          val result = viewModel.pagination.items
          result.value mustBe expectedResult
        }

        "must return 1 [2] 3 … 10 when on page 2 of 10" in {
          val items     = Seq.fill(100: Int)("value")
          val viewModel = new FakeViewModel(items, 2, 10, None)
          val expectedResult = Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem(s"href?page=2", Some("2"), current = Some(true)),
            PaginationItem(s"href?page=3", Some("3"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=10", Some("10"), current = Some(false))
          )
          val result = viewModel.pagination.items
          result.value mustBe expectedResult
        }

        "must return 1 2 [3] 4 … 10 when on page 3 of 10" in {
          val items     = Seq.fill(100: Int)("value")
          val viewModel = new FakeViewModel(items, 3, 10, None)
          val expectedResult = Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem(s"href?page=2", Some("2"), current = Some(false)),
            PaginationItem(s"href?page=3", Some("3"), current = Some(true)),
            PaginationItem(s"href?page=4", Some("4"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=10", Some("10"), current = Some(false))
          )
          val result = viewModel.pagination.items
          result.value mustBe expectedResult
        }

        "must return 1 … 3 [4] 5 … 10 when on page 4 of 10" in {
          val items     = Seq.fill(100: Int)("value")
          val viewModel = new FakeViewModel(items, 4, 10, None)
          val expectedResult = Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=3", Some("3"), current = Some(false)),
            PaginationItem(s"href?page=4", Some("4"), current = Some(true)),
            PaginationItem(s"href?page=5", Some("5"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=10", Some("10"), current = Some(false))
          )
          val result = viewModel.pagination.items
          result.value mustBe expectedResult
        }

        "must return 1 … 7 [8] 9 10 when on page 8 of 10" in {
          val items     = Seq.fill(100: Int)("value")
          val viewModel = new FakeViewModel(items, 8, 10, None)
          val expectedResult = Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=7", Some("7"), current = Some(false)),
            PaginationItem(s"href?page=8", Some("8"), current = Some(true)),
            PaginationItem(s"href?page=9", Some("9"), current = Some(false)),
            PaginationItem(s"href?page=10", Some("10"), current = Some(false))
          )
          val result = viewModel.pagination.items
          result.value mustBe expectedResult
        }

        "must return 1 … 8 [9] 10 when on page 9 of 10" in {
          val items     = Seq.fill(100: Int)("value")
          val viewModel = new FakeViewModel(items, 9, 10, None)
          val expectedResult = Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=8", Some("8"), current = Some(false)),
            PaginationItem(s"href?page=9", Some("9"), current = Some(true)),
            PaginationItem(s"href?page=10", Some("10"), current = Some(false))
          )
          val result = viewModel.pagination.items
          result.value mustBe expectedResult
        }

        "must return 1 … 9 [10] when on page 10 of 10" in {
          val items     = Seq.fill(100: Int)("value")
          val viewModel = new FakeViewModel(items, 10, 10, None)
          val expectedResult = Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=9", Some("9"), current = Some(false)),
            PaginationItem(s"href?page=10", Some("10"), current = Some(true))
          )
          val result = viewModel.pagination.items
          result.value mustBe expectedResult
        }

        "must return 1 [2] 3 when on page 2 of 3" in {
          val items     = Seq.fill(30: Int)("value")
          val viewModel = new FakeViewModel(items, 2, 10, None)
          val expectedResult = Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem(s"href?page=2", Some("2"), current = Some(true)),
            PaginationItem(s"href?page=3", Some("3"), current = Some(false))
          )
          val result = viewModel.pagination.items
          result.value mustBe expectedResult
        }
      }

      "searchResult" - {
        "must show how many results found" - {
          "when 1 result found" in {
            val items     = Seq("value")
            val viewModel = new FakeViewModel(items, 1, 10, None)
            val result    = viewModel.searchResult
            result mustBe "Showing <b>1</b> result"
          }

          "when 1 search result found" in {
            forAll(nonEmptyString) {
              search =>
                val items     = Seq("value")
                val viewModel = new FakeViewModel(items, 1, 10, Some(search))
                val result    = viewModel.searchResult
                result mustBe s"Showing <b>1</b> result matching $search"
            }
          }

          "when multiple results found" in {
            forAll(intGen) {
              count =>
                val items     = Seq.fill(count)("value")
                val viewModel = new FakeViewModel(items, 1, 10, None)
                val result    = viewModel.searchResult
                result mustBe s"Showing <b>$count</b> results"
            }
          }

          "when multiple search results found" in {
            forAll(intGen, nonEmptyString) {
              (count, search) =>
                val items     = Seq.fill(count)("value")
                val viewModel = new FakeViewModel(items, 1, 10, Some(search))
                val result    = viewModel.searchResult
                result mustBe s"Showing <b>$count</b> results matching $search"
            }
          }
        }
      }

      "paginatedSearchResult" - {
        "must show how many results found" in {
          val items     = Seq.fill(30: Int)("value")
          val viewModel = new FakeViewModel(items, 2, 10, None)
          val result    = viewModel.paginatedSearchResult
          result mustBe s"Showing <b>11</b> to <b>20</b> of <b>30</b> results"
        }

        "must show how many search results found" in {
          forAll(nonEmptyString) {
            search =>
              val items     = Seq.fill(30: Int)("value")
              val viewModel = new FakeViewModel(items, 2, 10, Some(search))
              val result    = viewModel.paginatedSearchResult
              result mustBe s"Showing <b>11</b> to <b>20</b> of <b>30</b> results matching $search"
          }
        }
      }
    }
  }
}
