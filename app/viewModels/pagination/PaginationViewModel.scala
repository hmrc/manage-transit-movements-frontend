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

package viewModels.pagination

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}

trait PaginationViewModel[T] {

  val items: Seq[T]
  val totalNumberOfItems: Int // this is across all pages
  val currentPage: Int
  val numberOfItemsPerPage: Int
  val heading: String
  val searchParam: Option[String] = None

  def results: MetaData = MetaData(totalNumberOfItems, numberOfItemsPerPage, currentPage)

  def noResultsFound(implicit messages: Messages): String = messages("search.noResultsFound")

  private def currentPageInvalid: Boolean = totalNumberOfItems < ((currentPage - 1) * numberOfItemsPerPage)

  def paginated: Boolean = results.totalPages > 1

  def status(implicit messages: Messages): String =
    if (totalNumberOfItems == 0) {
      noResultsFound
    } else if (currentPageInvalid) {
      messages("numberOfMovements.plural", "<b>0</b>")
    } else if (paginated) {
      searchParam match {
        case Some(value) => messages("pagination.results.search", s"<b>${results.from}</b>", s"<b>${results.to}</b>", s"<b>${results.count}</b>", value)
        case None        => messages("pagination.results", s"<b>${results.from}</b>", s"<b>${results.to}</b>", s"<b>${results.count}</b>")
      }
    } else {
      (searchParam, results.count) match {
        case (Some(value), 1) => messages("numberOfMovements.singular.withSearchParam", "<b>1</b>", value)
        case (Some(value), x) => messages("numberOfMovements.plural.withSearchParam", s"<b>$x</b>", value)
        case (None, 1)        => messages("numberOfMovements.singular", "<b>1</b>")
        case (None, x)        => messages("numberOfMovements.plural", s"<b>$x</b>")
      }
    }

  def href(page: Int): Call

  def pagination(implicit messages: Messages): Pagination = {
    def attributes(key: String): Map[String, String] =
      Map("aria-label" -> messages(key, heading.toLowerCase))

    val previous: Option[PaginationLink] = Option.when(currentPage > 1) {
      PaginationLink(
        href = href(currentPage - 1).url,
        attributes = attributes("pagination.previous.hidden")
      )
    }

    val next: Option[PaginationLink] = Option.when(currentPage < results.totalPages) {
      PaginationLink(
        href = href(currentPage + 1).url,
        attributes = attributes("pagination.next.hidden")
      )
    }

    val items = (1 to results.totalPages).foldLeft[Seq[PaginationItem]](Nil) {
      (acc, page) =>
        if (page == 1 || (page >= currentPage - 1 && page <= currentPage + 1) || page == results.totalPages) {
          acc :+ PaginationItem(
            href = href(page).url,
            number = Some(page.toString),
            current = Some(page == currentPage)
          )
        } else if (acc.lastOption.flatMap(_.ellipsis).contains(true)) {
          acc
        } else {
          acc :+ PaginationItem(ellipsis = Some(true))
        }
    }

    Pagination(Some(items), previous, next)
  }
}
