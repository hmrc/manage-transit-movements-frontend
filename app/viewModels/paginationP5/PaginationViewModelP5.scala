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

package viewModels.paginationP5

import play.api.i18n.Messages

case class PaginationViewModelP5(
  results: MetaData,
  previous: Option[Previous],
  next: Option[Next],
  items: Items,
  pageNumber: Int,
  lrn: Option[String]
) {

  def searchResult()(implicit messages: Messages): String =
    lrn match {
      case Some(lrn) =>
        results.count match {
          case 1 => messages("numberOfMovements.singular.withLRN", "<b>1</b>", lrn)
          case x => messages("numberOfMovements.plural.withLRN", s"<b>$x</b>", lrn)
        }
      case None =>
        results.count match {
          case 1 => messages("numberOfMovements.singular", "<b>1</b>")
          case x => messages("numberOfMovements.plural", s"<b>$x</b>")
        }
    }

  def paginatedSearchResult()(implicit messages: Messages): String =
    lrn match {
      case Some(lrn) =>
        messages("pagination.results.search", s"<b>${results.from}</b>", s"<b>${results.to}</b>", s"<b>${results.count}</b>", lrn)
      case None =>
        messages("pagination.results", s"<b>${results.from}</b>", s"<b>${results.to}</b>", s"<b>${results.count}</b>")
    }
}

object PaginationViewModelP5 {

  def apply(
    totalNumberOfMovements: Int,
    currentPage: Int,
    numberOfMovementsPerPage: Int,
    href: String,
    additionalParams: Seq[(String, String)] = Seq.empty,
    lrn: Option[String] = None
  ): PaginationViewModelP5 = {

    val results: MetaData = MetaData(totalNumberOfMovements, numberOfMovementsPerPage, currentPage)

    val previous: Option[Previous] = if (results.currentPage > 1) {
      Some(Previous(href, results.currentPage, additionalParams))
    } else {
      None
    }

    val next: Option[Next] = if (results.currentPage < results.totalPages) {
      Some(Next(href, results.currentPage, additionalParams))
    } else {
      None
    }

    val items = Items(results, href, additionalParams)

    PaginationViewModelP5(results, previous, next, items, currentPage, lrn)
  }
}
