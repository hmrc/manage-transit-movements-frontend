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

package viewModels.P5.departure

import controllers.departureP5.routes
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.pagination.PaginationViewModel

case class ViewAllDepartureMovementsP5ViewModel(
  heading: String,
  title: String,
  items: Seq[ViewDepartureP5],
  currentPage: Int,
  numberOfItemsPerPage: Int,
  override val searchParam: Option[String],
  totalNumberOfItems: Int
) extends PaginationViewModel[ViewDepartureP5] {

  override def href(page: Int): Call =
    routes.ViewAllDeparturesP5Controller.onPageLoad(Some(page), searchParam)
}

object ViewAllDepartureMovementsP5ViewModel {

  def apply(
    searchParam: Option[String],
    currentPage: Int,
    numberOfItemsPerPage: Int
  )(implicit messages: Messages): ViewAllDepartureMovementsP5ViewModel =
    apply(Nil, searchParam, currentPage, numberOfItemsPerPage, 0)

  def apply(
    movementsAndMessages: Seq[ViewDepartureP5],
    searchParam: Option[String],
    currentPage: Int,
    numberOfItemsPerPage: Int,
    totalNumberOfMovements: Int
  )(implicit messages: Messages): ViewAllDepartureMovementsP5ViewModel = {
    val heading: String = searchParam match {
      case Some(value) =>
        messages("viewDepartureDeclarationsP5.searchResult.heading", value)
      case None =>
        messages("viewDepartureDeclarationsP5.heading")
    }

    val title: String = searchParam match {
      case Some(value) =>
        messages("viewDepartureDeclarationsP5.searchResult.title", value)
      case None =>
        messages("viewDepartureDeclarationsP5.title")
    }

    new ViewAllDepartureMovementsP5ViewModel(
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
