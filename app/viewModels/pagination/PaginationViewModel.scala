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
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}

trait PaginationViewModel {

  val results: MetaData
  val previous: Option[PaginationLink]
  val next: Option[PaginationLink]
  val items: Seq[PaginationItem]
  val pageNumber: Int

  def searchResult(implicit messages: Messages): String =
    results.count match {
      case 1 => messages("numberOfMovements.singular", "<b>1</b>")
      case x => messages("numberOfMovements.plural", s"<b>$x</b>")
    }

  def paginatedSearchResult(implicit messages: Messages): String =
    messages("pagination.results", s"<b>${results.from}</b>", s"<b>${results.to}</b>", s"<b>${results.count}</b>")

  val pagination: Pagination = Pagination(Some(items), previous, next)
}

object PaginationViewModel {

  def apply[T <: PaginationViewModel](
    totalNumberOfMovements: Int,
    currentPage: Int,
    numberOfMovementsPerPage: Int,
    href: String,
    additionalParams: Seq[(String, String)]
  )(
    f: (MetaData, Option[PaginationLink], Option[PaginationLink], Seq[PaginationItem]) => T
  ): T = {

    val results: MetaData = MetaData(totalNumberOfMovements, numberOfMovementsPerPage, currentPage)

    def hrefWithParams(page: Int): String = additionalParams.foldLeft(s"$href?page=$page") {
      case (href, (key, value)) =>
        href + s"&$key=$value"
    }

    val previous: Option[PaginationLink] = if (currentPage > 1) {
      Some(PaginationLink(hrefWithParams(currentPage - 1)))
    } else {
      None
    }

    val next: Option[PaginationLink] = if (currentPage < results.totalPages) {
      Some(PaginationLink(hrefWithParams(currentPage + 1)))
    } else {
      None
    }

    val items = (1 to results.totalPages).foldLeft[Seq[PaginationItem]](Nil) {
      (acc, page) =>
        if (page == 1 || (page >= currentPage - 1 && page <= currentPage + 1) || page == results.totalPages) {
          acc :+ PaginationItem(
            href = hrefWithParams(page),
            number = Some(page.toString),
            current = Some(page == currentPage)
          )
        } else if (acc.lastOption.flatMap(_.ellipsis).contains(true)) {
          acc
        } else {
          acc :+ PaginationItem(ellipsis = Some(true))
        }
    }

    f(results, previous, next, items)
  }
}
