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

case class PaginationViewModel(
  results: MetaData,
  previous: Option[Previous],
  next: Option[Next],
  items: Items,
  pageNumber: Int
)

object PaginationViewModel {

  def apply(
    totalNumberOfMovements: Int,
    currentPage: Int,
    numberOfMovementsPerPage: Int,
    href: String
  ): PaginationViewModel = {

    val results: MetaData = MetaData(totalNumberOfMovements, numberOfMovementsPerPage, currentPage)

    val previous: Option[Previous] = if (results.currentPage > 1) {
      Some(Previous(href, results.currentPage))
    } else {
      None
    }

    val next: Option[Next] = if (results.currentPage < results.totalPages) {
      Some(Next(href, results.currentPage))
    } else {
      None
    }

    val items = Items(results, href)

    PaginationViewModel(results, previous, next, items, currentPage)
  }
}
