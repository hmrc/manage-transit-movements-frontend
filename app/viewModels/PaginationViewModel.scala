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
             numberOfMovementsPerPage: Int,
             href: String
           ): JsObject = {

    val numberOfPagesFloat = totalNumberOfMovements.toFloat / numberOfMovementsPerPage

    val totalNumberOfPages   = if (numberOfPagesFloat.isWhole) {
        numberOfPagesFloat.toInt
    } else {
        numberOfPagesFloat.toInt + 1
    }

    val validateCurrentPage = if (currentPage <  1 | currentPage > totalNumberOfPages) 1 else currentPage

    def buildItem(pageNumber: Int, dottedLeft: Boolean, dottedRight: Boolean): JsObject = Json.obj(
      "pageNumber" -> pageNumber,
      "href"        -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(pageNumber)).url}", // TODO move this to param
      "selected"    -> Json.toJson(pageNumber == validateCurrentPage),
      "dottedLeft"  -> dottedLeft,
      "dottedRight" -> dottedRight
    )

    val items =
      if(totalNumberOfPages < 6) {
        val range = (1 to totalNumberOfPages).take(5)

        range.map(buildItem(_, false,false ))
      }
    else if (validateCurrentPage == 1 | validateCurrentPage == 2) {

      val head = (1 to totalNumberOfPages).take(3)
      val tail = totalNumberOfPages
      val range = head ++ Seq(tail)

      range.map(buildItem(_, false, dottedRight = true ))

    } else if (validateCurrentPage == totalNumberOfPages | validateCurrentPage == totalNumberOfPages - 1) {

      val range = Seq(1 ,totalNumberOfPages - 2, totalNumberOfPages - 1, totalNumberOfPages)

      range.map(buildItem(_, dottedLeft = true, dottedRight = false))

    } else {

      val range = Seq(1 ,validateCurrentPage - 1, validateCurrentPage, validateCurrentPage + 1, totalNumberOfPages)

      range.map(buildItem(_, dottedLeft = true, dottedRight = true))
    }

    val from = validateCurrentPage match {
      case 1    => 1
      case 2    => numberOfMovementsPerPage +1
      case page => numberOfMovementsPerPage * (page - 1) +1

    }

    val to = if (validateCurrentPage == 1) {
      numberOfMovementsPerPage
    } else {
      val roundedNumberOfMovementsPerPage = numberOfMovementsPerPage * validateCurrentPage

      if (roundedNumberOfMovementsPerPage > totalNumberOfMovements) {
        totalNumberOfMovements
      } else {
        roundedNumberOfMovementsPerPage
      }
    }

    Json.obj(
      "results" -> Json.obj(
        "from"  -> from,
        "to"    -> to,
        "count" -> totalNumberOfMovements,
        "currentPage" -> validateCurrentPage,
        "totalPages" -> totalNumberOfPages
      ),
      "previous" -> Json.obj(
        "text" -> "Previous",
        "href" -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(currentPage - 1)).url}" // TODO move to param
      ),
      "next" -> Json.obj(
        "text" -> "Next",
        "href" -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(currentPage + 1)).url}" // TODO move to param
      ),
      "items" -> items
    )
  }
}


