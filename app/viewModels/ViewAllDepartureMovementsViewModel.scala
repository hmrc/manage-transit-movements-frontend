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

case class ViewAllDepartureMovementsViewModel(
  dataRows: Seq[(String, Seq[ViewDeparture])],
  paginationViewModel: PaginationViewModel
)

object ViewAllDepartureMovementsViewModel {

  implicit val localDateOrdering: Ordering[LocalDate] =
    Ordering.by(identity[ChronoLocalDate])

  def apply(
    movements: Seq[ViewDeparture],
    paginationViewModel: PaginationViewModel
  )(implicit d: DummyImplicit): ViewAllDepartureMovementsViewModel =
    new ViewAllDepartureMovementsViewModel(format(movements), paginationViewModel)

  private def format(movements: Seq[ViewDeparture]): Seq[(String, Seq[ViewDeparture])] = {
    val groupMovements: Map[LocalDate, Seq[ViewDeparture]] =
      movements.groupBy(_.updatedDate)
    val sortByDate: Seq[(LocalDate, Seq[ViewDeparture])] =
      groupMovements.toSeq.sortBy(_._1).reverse

    sortByDate.map {
      result =>
        val dateFormatter: DateTimeFormatter =
          DateTimeFormatter.ofPattern("d MMMM yyyy")
        (result._1.format(dateFormatter), result._2.sortBy(_.updatedTime).reverse)
    }
  }

}
