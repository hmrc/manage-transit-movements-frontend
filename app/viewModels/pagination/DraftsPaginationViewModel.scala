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

import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination._

// TODO - this is identical to MovementsPaginationViewModel. Consider removing.
case class DraftsPaginationViewModel(
  results: MetaData,
  previous: Option[PaginationLink],
  next: Option[PaginationLink],
  items: Seq[PaginationItem],
  pageNumber: Int
) extends PaginationViewModel

object DraftsPaginationViewModel {

  def apply(
    totalNumberOfMovements: Int,
    currentPage: Int,
    numberOfMovementsPerPage: Int,
    href: String,
    additionalParams: Seq[(String, String)] = Seq.empty
  ): DraftsPaginationViewModel =
    PaginationViewModel(
      totalNumberOfMovements,
      currentPage,
      numberOfMovementsPerPage,
      href,
      additionalParams
    ) {
      new DraftsPaginationViewModel(_, _, _, _, currentPage)
    }
}
