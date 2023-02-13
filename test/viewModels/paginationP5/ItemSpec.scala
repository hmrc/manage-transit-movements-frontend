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

class ItemSpec extends SpecBase {

  "Item" - {
    "apply" - {
      "must format href when not given params" in {

        Item(1, "testHref", 1, Seq.empty).href mustBe "testHref?page=1"
      }

      "must format href when given params" in {

        Item(1, "testHref", 1, Seq(("lrn", "123"))).href mustBe "testHref?page=1&lrn=123"
      }

      "selected" - {

        "must be true if current page is the same as page number" in {

          Item(1, "testHref", 1, Seq.empty).selected mustBe true
        }

        "must be false if current page is not the same as page number" in {

          Item(2, "testHref", 1, Seq.empty).selected mustBe false
        }
      }
    }
  }
}