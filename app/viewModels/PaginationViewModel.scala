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

    def buildItem(pageNumber: Int, dottedLeft: Boolean, dottedRight: Boolean): JsObject = Json.obj(
      "pageNumber" -> pageNumber,
      "href"        -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(pageNumber)).url}", // TODO move this to param
      "selected"    -> Json.toJson(pageNumber == currentPage),
      "dottedLeft"  -> dottedLeft,
      "dottedRight" -> dottedRight
    )

    val items =
      if(totalNumberOfPages <6) {
        val range = (1 to totalNumberOfPages).take(5)

        range.map(buildItem(_, false,false ))
      }
    else if (currentPage == 1 | currentPage == 2) {

      val head = (1 to totalNumberOfPages).take(3)
      val tail = totalNumberOfPages
      val range = head ++ Seq(tail)

      range.map(buildItem(_, false, dottedRight = true ))

    } else if (currentPage == totalNumberOfPages | currentPage == totalNumberOfPages - 1) {

      val range = Seq(1 ,totalNumberOfPages - 2, totalNumberOfPages - 1, totalNumberOfPages)

      range.map(buildItem(_, dottedLeft = true, dottedRight = false))

    } else {

      val range = Seq(1 ,currentPage - 1, currentPage, currentPage + 1, totalNumberOfPages)

      range.map(buildItem(_, dottedLeft = true, dottedRight = true))

    }

    val from = currentPage match {
      case 1    => 1
      case 2    => numberOfMovementsPerPage +1
      case page => numberOfMovementsPerPage * (page - 1) +1

    }

    val to = if (currentPage == 1) {
      numberOfMovementsPerPage
    } else {
      val roundedNumberOfMovementsPerPage = numberOfMovementsPerPage * currentPage

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
        "count" -> totalNumberOfMovements
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


