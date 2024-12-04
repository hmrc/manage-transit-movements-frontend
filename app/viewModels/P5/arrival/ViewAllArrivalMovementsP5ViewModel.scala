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

package viewModels.P5.arrival

import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.pagination.PaginationViewModel

case class ViewAllArrivalMovementsP5ViewModel(
  heading: String,
  title: String,
  items: Seq[(String, Seq[ViewArrivalP5])],
  currentPage: Int,
  numberOfItemsPerPage: Int,
  href: Call,
  override val additionalParams: Seq[(String, String)]
) extends PaginationViewModel[(String, Seq[ViewArrivalP5])]

object ViewAllArrivalMovementsP5ViewModel {

  def apply(
    movementsAndMessages: Seq[ViewArrivalP5],
    searchParam: Option[String],
    currentPage: Int,
    numberOfItemsPerPage: Int,
    href: Call,
    additionalParams: Seq[(String, String)]
  )(implicit messages: Messages): ViewAllArrivalMovementsP5ViewModel = {

    val heading: String = searchParam match {
      case Some(value) =>
        messages("viewArrivalNotificationsP5.searchResult.heading", value)
      case None =>
        messages("viewArrivalNotificationsP5.heading")
    }

    val title: String = searchParam match {
      case Some(value) =>
        messages("viewArrivalNotificationsP5.searchResult.title", value)
      case None =>
        messages("viewArrivalNotificationsP5.title")
    }

    new ViewAllArrivalMovementsP5ViewModel(
      heading,
      title,
      ViewArrivalMovementsP5(movementsAndMessages).dataRows,
      currentPage,
      numberOfItemsPerPage,
      href,
      additionalParams
    )
  }
}
