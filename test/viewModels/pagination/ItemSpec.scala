/*
 * Copyright 2021 HM Revenue & Customs
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

class ItemSpec extends SpecBase {

  "Item" - {
    "apply" - {
      "must format href" in {

        Item(1, "testHref", 1).href mustBe "testHref?page=1"
      }

      "selected" - {

        "must be true if current page is the same as page number" in {

          Item(1, "testHref", 1).selected mustBe true
        }

        "must be false if current page is not the same as page number" in {

          Item(2, "testHref", 1).selected mustBe false
        }
      }
    }
  }
}