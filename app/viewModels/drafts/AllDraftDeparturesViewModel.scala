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

import controllers.departureP5.drafts.routes
import models.Sort.*
import models.Sort.Field.{CreatedAt, LRN}
import models.{DepartureUserAnswerSummary, DeparturesSummary, LocalReferenceNumber, Sort}
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.drafts.AllDraftDeparturesViewModel.DraftDepartureRow
import viewModels.pagination.PaginationViewModel

case class AllDraftDeparturesViewModel(
  title: String,
  heading: String,
  departures: DeparturesSummary,
  currentPage: Int,
  numberOfItemsPerPage: Int,
  lrn: Option[String],
  sortParams: Sort = SortByCreatedAtDesc,
  override val additionalParams: Seq[(String, String)],
  href: Call
) extends PaginationViewModel[DepartureUserAnswerSummary] {

  override val items: Seq[DepartureUserAnswerSummary] = departures.userAnswers

  private val numberOfItems: Int = items.length

  private val tableMessageKeyPrefix                                   = "departure.drafts.dashboard.table"
  def visuallyHiddenHeader(implicit messages: Messages): String       = messages(s"$tableMessageKeyPrefix.heading.hidden")
  def referenceNumber(implicit messages: Messages): String            = messages(s"$tableMessageKeyPrefix.lrn")
  def daysToComplete(implicit messages: Messages): String             = messages(s"$tableMessageKeyPrefix.daysToComplete")
  def daysToCompleteHiddenHeader(implicit messages: Messages): String = messages(s"$tableMessageKeyPrefix.daysToComplete.header.hidden")

  def searchResult(implicit messages: Messages): Option[String] =
    lrn.map {
      value =>
        numberOfItems match {
          case 1 => messages("search.results.singular", "<b>1</b>", value)
          case x => messages("search.results.plural", s"<b>$x</b>", value)
        }
    }

  def dataRows: Seq[DraftDepartureRow] = items.map(DraftDepartureRow.apply)

  def tooManyResults: Boolean       = numberOfItems > numberOfItemsPerPage
  def isSearch: Boolean             = lrn.isDefined
  def resultsFound: Boolean         = numberOfItems > 0
  def searchResultsFound: Boolean   = resultsFound && isSearch
  def noResultsFound: Boolean       = departures.totalMovements == 0
  def noSearchResultsFound: Boolean = departures.totalMatchingMovements == 0 && !noResultsFound

  def sortLrn: String       = sortParams.ariaSort(LRN)
  def sortCreatedAt: String = sortParams.ariaSort(CreatedAt)

  def sortLRNHref: Call       = sortParams.href(LRN, lrn)
  def sortCreatedAtHref: Call = sortParams.href(CreatedAt, lrn)

  def sortHiddenTextLRN(implicit messages: Messages): String            = sortParams.hiddenText(LRN)
  def sortHiddenTextDaysToComplete(implicit messages: Messages): String = sortParams.hiddenText(CreatedAt)

  def deleteDraftUrl(draft: DraftDepartureRow): Call =
    routes.DeleteDraftDepartureYesNoController.onPageLoad(LocalReferenceNumber(draft.lrn), currentPage, numberOfItems, lrn)
}

object AllDraftDeparturesViewModel {

  case class DraftDepartureRow(lrn: String, daysRemaining: Int)

  object DraftDepartureRow {

    def apply(value: DepartureUserAnswerSummary): DraftDepartureRow =
      new DraftDepartureRow(value.lrn.toString, value.expiresInDays)
  }

  def apply(
    departures: DeparturesSummary,
    lrn: Option[String],
    currentPage: Int,
    numberOfItemsPerPage: Int,
    href: Call,
    sortParams: Sort,
    additionalParams: Seq[(String, String)]
  )(implicit messages: Messages): AllDraftDeparturesViewModel = {

    val messageKeyPrefix = "departure.drafts.dashboard"

    new AllDraftDeparturesViewModel(
      messages(s"$messageKeyPrefix.title"),
      messages(s"$messageKeyPrefix.heading"),
      departures,
      currentPage,
      numberOfItemsPerPage,
      lrn,
      sortParams,
      additionalParams,
      href
    )
  }
}
