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

package viewModels.paginationP5

import base.SpecBase
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.PaginationItem

class PaginationViewModelP5Spec extends SpecBase {

  "PaginationViewModelP5" - {
    "apply" - {

      "Next" - {

        "must return some when current page is less than the total number of pages" in {

          PaginationViewModelP5(10, 2, 2, "testHref").next.isDefined mustBe true
        }

        "must return None when current page is not less than the total number of pages" in {

          PaginationViewModelP5(10, 5, 2, "testHref").next.isDefined mustBe false
        }
      }

      "Previous" - {

        "must return some when current page is greater than 1" in {
          PaginationViewModelP5(10, 2, 2, "testHref").previous.isDefined mustBe true
        }

        "must return none when current page is not greater than 1" in {
          PaginationViewModelP5(10, 1, 2, "testHref").previous.isDefined mustBe false
        }
      }

      "Items" - {

        "must return [1] 2 … 100 when on page 1 of 100" in {
          val result = PaginationViewModelP5(1000, 1, 10, "href").items

          result mustBe Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(true)),
            PaginationItem(s"href?page=2", Some("2"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(false))
          )
        }

        "must return 1 [2] 3 … 100 when on page 2 of 100" in {
          val result = PaginationViewModelP5(1000, 2, 10, "href").items

          result mustBe Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem(s"href?page=2", Some("2"), current = Some(true)),
            PaginationItem(s"href?page=3", Some("3"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(false))
          )
        }

        "must return 1 2 [3] 4 … 100 when on page 3 of 100" in {
          val result = PaginationViewModelP5(1000, 3, 10, "href").items

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
          val result = PaginationViewModelP5(1000, 4, 10, "href").items

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
          val result = PaginationViewModelP5(1000, 98, 10, "href").items

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
          val result = PaginationViewModelP5(1000, 99, 10, "href").items

          result mustBe Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=98", Some("98"), current = Some(false)),
            PaginationItem(s"href?page=99", Some("99"), current = Some(true)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(false))
          )
        }

        "must return 1 … 99 [100] when on page 100 of 100" in {
          val result = PaginationViewModelP5(1000, 100, 10, "href").items

          result mustBe Seq(
            PaginationItem(s"href?page=1", Some("1"), current = Some(false)),
            PaginationItem("", ellipsis = Some(true)),
            PaginationItem(s"href?page=99", Some("99"), current = Some(false)),
            PaginationItem(s"href?page=100", Some("100"), current = Some(true))
          )
        }
      }
    }
  }
}
