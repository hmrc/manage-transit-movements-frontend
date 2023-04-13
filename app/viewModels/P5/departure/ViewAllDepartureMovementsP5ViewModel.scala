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

package viewModels.P5.departure

import viewModels.pagination.PaginationViewModel

case class ViewAllDepartureMovementsP5ViewModel(
  dataRows: Seq[(String, Seq[ViewDepartureP5])],
  paginationViewModel: PaginationViewModel
)

object ViewAllDepartureMovementsP5ViewModel {

  def apply(
    movementsAndMessages: Seq[ViewDepartureP5],
    paginationViewModel: PaginationViewModel
  )(implicit d: DummyImplicit): ViewAllDepartureMovementsP5ViewModel =
    new ViewAllDepartureMovementsP5ViewModel(
      ViewDepartureMovementsP5(movementsAndMessages).dataRows,
      paginationViewModel
    )

}
