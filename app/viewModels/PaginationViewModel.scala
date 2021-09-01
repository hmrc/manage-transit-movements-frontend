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

import controllers.testOnly.routes
import play.api.libs.json.{JsObject, Json}

object PaginationViewModel {

  def apply(
             totalNumberOfMovements: Int,
             currentPage: Int,
             numberOfMovementsPerPage: Int
           ): JsObject = {

    val numberOfPagesFloat = totalNumberOfMovements.toFloat / numberOfMovementsPerPage

    val totalNumberOfPages   = if (numberOfPagesFloat.isWhole) {
      numberOfPagesFloat.toInt
    } else {
      numberOfPagesFloat.toInt + 1
    }

    def buildItem(pageNumber: Int, dotted: Boolean) = Json.obj(
      "text" -> pageNumber,
      "href" -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(pageNumber)).url}", // TODO move this to param
      "selected" -> Json.toJson(pageNumber == currentPage),
      "type" -> {if (dotted) "dots" else ""}
    )

    val items = if (totalNumberOfPages < 6) {
      (1 to totalNumberOfPages).map {
        buildItem(_, dotted = false)
      }
    } else if (currentPage == 1 | currentPage == 2) {

      val head = (1 to totalNumberOfPages).take(3)
      val tail = totalNumberOfPages
      val range = head ++ Seq(tail)

      range.map {
        pageNumber =>
          val dotted = (pageNumber == totalNumberOfPages)
          buildItem(pageNumber, dotted)
      }
    } else if (currentPage == totalNumberOfPages) {
      val range = Seq(1, totalNumberOfPages - 2, totalNumberOfPages - 1, totalNumberOfPages)

      range.map {
        pageNumber =>
          val dotted = pageNumber == 1
          buildItem(pageNumber, dotted)
      }
    } else {
      val range = Seq(1, currentPage - 1, currentPage, currentPage + 1, totalNumberOfPages)

      range.map {
        pageNumber =>
          val dotted = (pageNumber == 1 | pageNumber == totalNumberOfPages)
          buildItem(pageNumber, dotted)
      }
    }

    val from = if (currentPage == 1) currentPage else currentPage * numberOfMovementsPerPage
    val to   = if (currentPage == 1) currentPage * numberOfMovementsPerPage else (currentPage + 1) * numberOfMovementsPerPage

    Json.obj(
      "results" -> Json.obj(
        "from"  -> from,
        "to"    -> to,
        "count" -> totalNumberOfMovements
      ),
      "previous" -> Json.obj(
        "text" -> "Previous",
        "next" -> ""
      ),
      "next" -> Json.obj(
        "text" -> "Next",
        "href" -> ""
      ),
      "items" -> items
    )
  }
}


