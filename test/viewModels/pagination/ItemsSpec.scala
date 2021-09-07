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

class ItemsSpec extends SpecBase {


  "Items" - {
    "apply" - {
      "must return a sequence of items without dots ranging from 1 to the total number of pages " +
        "when the total number of pages is less than 6 " +
        "e.g. 1, 2, 3, 4, 5" in {

        val expectedResult1 = Items(
          Seq(
            Item(1, "testHref?page=1", selected = true),
            Item(2, "testHref?page=2", selected = false)
          ),
          firstItemDotted = false,
          lastItemDotted = false
        )

        val expectedResult2 = Items(
          Seq(
            Item(1, "testHref?page=1", selected = false),
            Item(2, "testHref?page=2", selected = true),
            Item(3, "testHref?page=3", selected = false)
          ),
          firstItemDotted = false,
          lastItemDotted = false
        )

        val result1 = Items(MetaData(10, 5, 1), "testHref")
        val result2 = Items(MetaData(15, 5, 2), "testHref")

        result1 mustBe expectedResult1
        result2 mustBe expectedResult2
      }

      "must return a sequence of items with last item dotted ranging from 1 to the total number of pages " +
        "ending with the total number of pages when the current page is 1, 2 or 3 " +
        "e.g. 1, 2, 3, 4 ... 10" in {

        val expectedResult1 = Items(
          Seq(
            Item(1, "testHref?page=1", selected = true),
            Item(2, "testHref?page=2", selected = false),
            Item(3, "testHref?page=3", selected = false),
            Item(4, "testHref?page=4", selected = false),
            Item(6, "testHref?page=6", selected = false)
          ),
          firstItemDotted = false,
          lastItemDotted = true
        )

        val expectedResult2 = Items(
          Seq(
            Item(1, "testHref?page=1", selected = false),
            Item(2, "testHref?page=2", selected = true),
            Item(3, "testHref?page=3", selected = false),
            Item(4, "testHref?page=4", selected = false),
            Item(10, "testHref?page=10", selected = false)
          ),
          firstItemDotted = false,
          lastItemDotted = true
        )

        val result1 = Items(MetaData(12, 2, 1), "testHref")
        val result2 = Items(MetaData(20, 2, 2), "testHref")

        result1 mustBe expectedResult1
        result2 mustBe expectedResult2
      }

      "must return a sequence of items with first item dotted ranging from the last page to the previous 2 pages " +
        "beginning with the first page when the current page is the last page or the second from last page " +
        "e.g. 1 ... 7, 8, 9, 10" in {


        val expectedResult1 = Items(
          Seq(
            Item(1, "testHref?page=1", selected = false),
            Item(3, "testHref?page=3", selected = false),
            Item(4, "testHref?page=4", selected = false),
            Item(5, "testHref?page=5", selected = false),
            Item(6, "testHref?page=6", selected = true),
          ),
          firstItemDotted = true,
          lastItemDotted = false
        )

        val expectedResult2 = Items(
          Seq(
            Item(1, "testHref?page=1", selected = false),
            Item(7, "testHref?page=7", selected = false),
            Item(8, "testHref?page=8", selected = false),
            Item(9, "testHref?page=9", selected = true),
            Item(10, "testHref?page=10", selected = false),
          ),
          firstItemDotted = true,
          lastItemDotted = false
        )

        val result1 = Items(MetaData(12, 2, 6), "testHref")
        val result2 = Items(MetaData(20, 2, 9), "testHref")

        result1 mustBe expectedResult1
        result2 mustBe expectedResult2
      }

      "must return a sequence of items with first and last item dotted ranging from the first page to the current page minus 1 " +
        "the current page, the current page plus 1 and the last page " +
        "e.g. 1 ... 4, 5, 6 ... 10" in {

        val expectedResult = Items(
          Seq(
            Item(1, "testHref?page=1", selected = false),
            Item(5, "testHref?page=5", selected = false),
            Item(6, "testHref?page=6", selected = true),
            Item(7, "testHref?page=7", selected = false),
            Item(10, "testHref?page=10", selected = false),
          ),
          firstItemDotted = true,
          lastItemDotted = true
        )

        val result = Items(MetaData(20, 2, 6), "testHref")

        result mustBe expectedResult
      }
    }
  }
}

