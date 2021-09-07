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

package viewModels

import base.SpecBase
import controllers.testOnly.routes
import play.api.libs.json.Json

class PaginationViewModelSpec extends SpecBase {

  "PaginationViewModel" - {
    "Must return paginated list without dots when the number of pages is less than 6" in {
      val expectedResult = Json.obj(
        "results" -> Json.obj(
          "from"        -> 1,
          "to"          -> 50,
          "count"       -> 60,
          "currentPage" -> 1,
          "totalPages"  -> 2
        ),
        "previous" -> Json.obj(
          "text" -> "Previous",
          "href" -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(0)).url}"
        ),
        "next" -> Json.obj(
          "text" -> "Next",
          "href" -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(2)).url}"
        ),
        "items" -> Json.arr(
          Json.obj(
            "pageNumber"  -> 1,
            "href"        -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(1)).url}",
            "selected"    -> Json.toJson(true),
            "dottedLeft"  -> false,
            "dottedRight" -> false
          ),
          Json.obj(
            "pageNumber"  -> 2,
            "href"        -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(2)).url}",
            "selected"    -> Json.toJson(false),
            "dottedLeft"  -> false,
            "dottedRight" -> false
          )
        )
      )
      PaginationViewModel(60, 1, 50, routes.ViewAllArrivalsController.onPageLoad) mustBe expectedResult
    }

    "Must return paginated list with left and right dots when the current page is not the first 2 or last 2 pages" in {
      val expectedResult = Json.obj(
        "results" -> Json.obj(
          "from"        -> 151,
          "to"          -> 200,
          "count"       -> 260,
          "currentPage" -> 4,
          "totalPages"  -> 6
        ),
        "previous" -> Json.obj(
          "text" -> "Previous",
          "href" -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(3)).url}"
        ),
        "next" -> Json.obj(
          "text" -> "Next",
          "href" -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(5)).url}"
        ),
        "items" -> Json.arr(
          Json.obj(
            "pageNumber"  -> 1,
            "href"        -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(1)).url}",
            "selected"    -> Json.toJson(false),
            "dottedLeft"  -> true,
            "dottedRight" -> true
          ),
          Json.obj(
            "pageNumber"  -> 3,
            "href"        -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(3)).url}",
            "selected"    -> Json.toJson(false),
            "dottedLeft"  -> true,
            "dottedRight" -> true
          ),
          Json.obj(
            "pageNumber"  -> 4,
            "href"        -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(4)).url}",
            "selected"    -> Json.toJson(true),
            "dottedLeft"  -> true,
            "dottedRight" -> true
          ),
          Json.obj(
            "pageNumber"  -> 5,
            "href"        -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(5)).url}",
            "selected"    -> Json.toJson(false),
            "dottedLeft"  -> true,
            "dottedRight" -> true
          ),
          Json.obj(
            "pageNumber"  -> 6,
            "href"        -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(6)).url}",
            "selected"    -> Json.toJson(false),
            "dottedLeft"  -> true,
            "dottedRight" -> true
          )
        )
      )
      PaginationViewModel(260, 4, 50, routes.ViewAllArrivalsController.onPageLoad) mustBe expectedResult
    }

    "Must return paginated list with left dots when the current page is one of the last 2 pages" in {
      val expectedResult = Json.obj(
        "results" -> Json.obj(
          "from"        -> 251,
          "to"          -> 260,
          "count"       -> 260,
          "currentPage" -> 6,
          "totalPages"  -> 6
        ),
        "previous" -> Json.obj(
          "text" -> "Previous",
          "href" -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(5)).url}"
        ),
        "next" -> Json.obj(
          "text" -> "Next",
          "href" -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(7)).url}"
        ),
        "items" -> Json.arr(
          Json.obj(
            "pageNumber"  -> 1,
            "href"        -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(1)).url}",
            "selected"    -> Json.toJson(false),
            "dottedLeft"  -> true,
            "dottedRight" -> false
          ),
          Json.obj(
            "pageNumber"  -> 4,
            "href"        -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(4)).url}",
            "selected"    -> Json.toJson(false),
            "dottedLeft"  -> true,
            "dottedRight" -> false
          ),
          Json.obj(
            "pageNumber"  -> 5,
            "href"        -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(5)).url}",
            "selected"    -> Json.toJson(false),
            "dottedLeft"  -> true,
            "dottedRight" -> false
          ),
          Json.obj(
            "pageNumber"  -> 6,
            "href"        -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(6)).url}",
            "selected"    -> Json.toJson(true),
            "dottedLeft"  -> true,
            "dottedRight" -> false
          )
        )
      )
      PaginationViewModel(260, 6, 50, routes.ViewAllArrivalsController.onPageLoad) mustBe expectedResult
    }

    "Must return paginated list with right dots when the current page is one of the first 2 pages and number of p[ages is greater than 5" in {
      val expectedResult = Json.obj(
        "results" -> Json.obj(
          "from"        -> 1,
          "to"          -> 50,
          "count"       -> 260,
          "currentPage" -> 1,
          "totalPages"  -> 6
        ),
        "previous" -> Json.obj(
          "text" -> "Previous",
          "href" -> s"${routes.ViewAllDeparturesController.onPageLoad(Some(0)).url}"
        ),
        "next" -> Json.obj(
          "text" -> "Next",
          "href" -> s"${routes.ViewAllDeparturesController.onPageLoad(Some(2)).url}"
        ),
        "items" -> Json.arr(
          Json.obj(
            "pageNumber"  -> 1,
            "href"        -> s"${routes.ViewAllDeparturesController.onPageLoad(Some(1)).url}",
            "selected"    -> Json.toJson(true),
            "dottedLeft"  -> false,
            "dottedRight" -> true
          ),
          Json.obj(
            "pageNumber"  -> 2,
            "href"        -> s"${routes.ViewAllDeparturesController.onPageLoad(Some(2)).url}",
            "selected"    -> Json.toJson(false),
            "dottedLeft"  -> false,
            "dottedRight" -> true
          ),
          Json.obj(
            "pageNumber"  -> 3,
            "href"        -> s"${routes.ViewAllDeparturesController.onPageLoad(Some(3)).url}",
            "selected"    -> Json.toJson(false),
            "dottedLeft"  -> false,
            "dottedRight" -> true
          ),
          Json.obj(
            "pageNumber"  -> 6,
            "href"        -> s"${routes.ViewAllDeparturesController.onPageLoad(Some(6)).url}",
            "selected"    -> Json.toJson(false),
            "dottedLeft"  -> false,
            "dottedRight" -> true
          )
        )
      )
      PaginationViewModel(260, 1, 50, routes.ViewAllDeparturesController.onPageLoad) mustBe expectedResult
    }

  }
}
