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

class MetaDataSpec extends SpecBase {

  "MetaData" - {
    "apply" - {
      "totalNumberOfPages" - {

        "must return total number of movements divided by number of movements and add 1 if its not whole" in {

          MetaData(3, 2, 1).totalPages mustBe 2
          MetaData(5, 2, 1).totalPages mustBe 3
          MetaData(10, 3, 1).totalPages mustBe 4
        }

        "must return total number of movements divided by number of movements and not add 1 if its whole" in {

          MetaData(2, 1, 1).totalPages mustBe 2
          MetaData(6, 2, 1).totalPages mustBe 3
          MetaData(8, 2, 1).totalPages mustBe 4
        }
      }

      "to" - {

        "must return the number of movements per page if current page is 1" in {

          MetaData(20, 10, 1).to mustBe 10
          MetaData(40, 20, 1).to mustBe 20
          MetaData(100, 50, 1).to mustBe 50
        }

        "must return the total number of movements" +
          " when number of movement times by the current page number is greater than the total number of movements" in {

            MetaData(11, 10, 2).to mustBe 11
            MetaData(21, 10, 3).to mustBe 21
            MetaData(101, 50, 3).to mustBe 101
          }
      }

      "from" - {

        "must return 1 when current page is 1" in {

          MetaData(2, 1, 1).from mustBe 1
        }

        "must return number of movements per page + 1 when current page is 2" in {

          MetaData(30, 10, 2).from mustBe 11
          MetaData(5, 2, 2).from mustBe 3
          MetaData(150, 50, 2).from mustBe 51
        }

        "must return (number of movements per page times by (current page minus 1)) plus 1 when on any other page" in {

          MetaData(30, 10, 3).from mustBe 21
          MetaData(10, 2, 4).from mustBe 7
          MetaData(400, 50, 5).from mustBe 201
        }
      }
    }
  }
}
