/*
 * Copyright 2022 HM Revenue & Customs
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

package viewModels

import viewModels.pagination.PaginationViewModel

import java.time.LocalDate
import java.time.chrono.ChronoLocalDate
import java.time.format.DateTimeFormatter

case class ViewAllArrivalMovementsViewModel(
  dataRows: Seq[(String, Seq[ViewArrival])],
  paginationViewModel: PaginationViewModel
)

object ViewAllArrivalMovementsViewModel {

  implicit val localDateOrdering: Ordering[LocalDate] =
    Ordering.by(identity[ChronoLocalDate])

  def apply(
    movements: Seq[ViewArrival],
    paginationViewModel: PaginationViewModel
  )(implicit d: DummyImplicit): ViewAllArrivalMovementsViewModel =
    new ViewAllArrivalMovementsViewModel(format(movements), paginationViewModel)

  private def format(movements: Seq[ViewArrival]): Seq[(String, Seq[ViewArrival])] = {
    val groupMovements: Map[LocalDate, Seq[ViewArrival]] =
      movements.groupBy(_.updatedDate)
    val sortByDate: Seq[(LocalDate, Seq[ViewArrival])] =
      groupMovements.toSeq.sortBy(_._1).reverse

    sortByDate.map {
      result =>
        val dateFormatter: DateTimeFormatter =
          DateTimeFormatter.ofPattern("d MMMM yyyy")
        (result._1.format(dateFormatter), result._2.sortBy(_.updatedTime).reverse)
    }
  }

}
