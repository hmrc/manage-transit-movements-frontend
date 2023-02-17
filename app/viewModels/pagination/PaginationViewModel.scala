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

  def searchResult(implicit messages: Messages): String

  def paginatedSearchResult(implicit messages: Messages): String

  val pagination: Pagination = Pagination(Some(items), previous, next)

}
