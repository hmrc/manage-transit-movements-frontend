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

class PaginationViewModelSpec extends SpecBase {

  "PaginationViewModel" - {
    "apply" - {

      "Next" - {

        "must return some when current page is less than the total number of pages" in {

          PaginationViewModel(10, 2, 2, "testHref").next.isDefined mustBe true
        }

        "must return None when current page is not less than the total number of pages" in {

          PaginationViewModel(10, 5, 2, "testHref").next.isDefined mustBe false
        }
      }

      "Previous" - {

        "must return some when current page is greater than 1" in {
          PaginationViewModel(10, 2, 2, "testHref").previous.isDefined mustBe true
        }

        "must return none when current page is not greater than 1" in {
          PaginationViewModel(10, 1, 2, "testHref").previous.isDefined mustBe false
        }
      }
    }
  }
}