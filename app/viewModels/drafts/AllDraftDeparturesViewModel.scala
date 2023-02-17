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

package viewModels.drafts

import models.DeparturesSummary
import play.api.i18n.Messages
import viewModels.drafts.AllDraftDeparturesViewModel.DraftDepartureRow
import viewModels.paginationP5.PaginationViewModelP5

case class AllDraftDeparturesViewModel(
  items: DeparturesSummary,
  pageSize: Int,
  lrn: Option[String],
  draftDepartureFrontendUrl: String,
  paginationViewModel: PaginationViewModelP5
) {

  val messageKeyPrefix      = "departure.drafts.dashboard"
  val tableMessageKeyPrefix = "departure.drafts.dashboard.table"

  val draftDepartures: Int = items.userAnswers.length

  def title(implicit messages: Messages): String                = messages(s"$messageKeyPrefix.title")
  def heading(implicit messages: Messages): String              = messages(s"$messageKeyPrefix.heading")
  def visuallyHiddenHeader(implicit messages: Messages): String = messages(s"$messageKeyPrefix.heading.hidden")

  def referenceNumber(implicit messages: Messages): String = messages(s"$tableMessageKeyPrefix.lrn")

  def lrnRedirectLocation(lrn: String): String            = s"$draftDepartureFrontendUrl/drafts/$lrn"
  def daysToComplete(implicit messages: Messages): String = messages(s"$tableMessageKeyPrefix.daysToComplete")

  def searchResult(implicit messages: Messages): Option[String] =
    lrn.map {
      lrn =>
        draftDepartures match {
          case 1 => messages("search.results.singular", "<b>1</b>", lrn)
          case x => messages("search.results.plural", s"<b>$x</b>", lrn)
        }
    }

  def dataRows: Seq[DraftDepartureRow] = items.userAnswers.map {
    dd => DraftDepartureRow(dd.lrn.toString, dd.expiresInDays)
  }

  def tooManyResults: Boolean = draftDepartures > pageSize

  def isSearch: Boolean = lrn.isDefined

  def resultsFound: Boolean = dataRows.nonEmpty

  def searchResultsFound: Boolean = resultsFound && isSearch

  def noResultsFound: Boolean = items.totalMovements == 0

  def noSearchResultsFound: Boolean = items.totalMatchingMovements == 0 && items.totalMovements > 0

  def pageNumber: Int = paginationViewModel.pageNumber

}

object AllDraftDeparturesViewModel {

  case class DraftDepartureRow(lrn: String, daysRemaining: Int)
}
