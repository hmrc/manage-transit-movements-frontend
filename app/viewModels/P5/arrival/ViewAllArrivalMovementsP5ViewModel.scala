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

case class ViewAllArrivalMovementsP5ViewModel(
  dataRows: Seq[(String, Seq[ViewArrivalP5])],
  searchParam: Option[String]
) {

  def pageHeading(implicit messages: Messages): String =
    searchParam match {
      case Some(searchParam) =>
        messages("viewArrivalNotificationsP5.searchResult.heading", searchParam)
      case None =>
        messages("viewArrivalNotificationsP5.heading")
    }

  def pageTitle(implicit messages: Messages): String =
    searchParam match {
      case Some(searchParam) =>
        messages("viewArrivalNotificationsP5.searchResult.title", searchParam)
      case None =>
        messages("viewArrivalNotificationsP5.title")
    }

}

object ViewAllArrivalMovementsP5ViewModel {

  def apply(
    movementsAndMessages: Seq[ViewArrivalP5],
    searchParam: Option[String]
  )(implicit d: DummyImplicit): ViewAllArrivalMovementsP5ViewModel =
    new ViewAllArrivalMovementsP5ViewModel(
      ViewArrivalMovementsP5(movementsAndMessages).dataRows,
      searchParam
    )

}
