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

package viewModels.departure

import controllers.departure.routes
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.pagination.PaginationViewModel

case class ViewAllDepartureMovementsViewModel(
  heading: String,
  title: String,
  items: Seq[ViewDeparture],
  currentPage: Int,
  numberOfItemsPerPage: Int,
  override val searchParam: Option[String],
  totalNumberOfItems: Int
) extends PaginationViewModel[ViewDeparture] {

  override def href(page: Int): Call =
    routes.ViewAllDeparturesController.onPageLoad(Some(page), searchParam)
}

object ViewAllDepartureMovementsViewModel {

  def apply(
    movementsAndMessages: Seq[ViewDeparture],
    searchParam: Option[String],
    currentPage: Int,
    numberOfItemsPerPage: Int,
    totalNumberOfMovements: Int
  )(implicit messages: Messages): ViewAllDepartureMovementsViewModel = {
    val heading: String = searchParam match {
      case Some(value) =>
        messages("viewDepartureDeclarations.searchResult.heading", value)
      case None =>
        messages("viewDepartureDeclarations.heading")
    }

    val title: String = searchParam match {
      case Some(value) =>
        messages("viewDepartureDeclarations.searchResult.title", value)
      case None =>
        messages("viewDepartureDeclarations.title")
    }

    new ViewAllDepartureMovementsViewModel(
      heading,
      title,
      movementsAndMessages,
      currentPage,
      numberOfItemsPerPage,
      searchParam,
      totalNumberOfMovements
    )
  }

}
