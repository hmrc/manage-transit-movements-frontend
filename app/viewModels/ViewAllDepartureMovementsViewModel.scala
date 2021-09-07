/*
 * Copyright 2021 HM Revenue & Customs
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

import config.FrontendAppConfig
import controllers.routes
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, OWrites}
import viewModels.pagination.PaginationViewModel

import java.time.LocalDate
import java.time.chrono.ChronoLocalDate
import java.time.format.DateTimeFormatter

case class ViewAllDepartureMovementsViewModel(
                                             dataRows: Seq[(String, Seq[ViewDeparture])],
                                             paginationViewModel: PaginationViewModel
                                           ) {

  val singularOrPlural = if (paginationViewModel.results.count == 1) { "numberOfMovements.singular" }
  else { "numberOfMovements.plural" }

}

object ViewAllDepartureMovementsViewModel {

  implicit val localDateOrdering: Ordering[LocalDate] =
    Ordering.by(identity[ChronoLocalDate])

  def apply(
             movements: Seq[ViewDeparture],
             paginationViewModel: PaginationViewModel
           )(implicit d: DummyImplicit): ViewAllDepartureMovementsViewModel =
    ViewAllDepartureMovementsViewModel(format(movements), paginationViewModel)

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

  implicit def writes(implicit frontendAppConfig: FrontendAppConfig): OWrites[ViewAllDepartureMovementsViewModel] = (
    (__ \ "dataRows").write[Seq[(String, Seq[ViewDeparture])]] and
      (__ \ "declareDepartureNotificationUrl").write[String] and
      (__ \ "homePageUrl").write[String] and
      (__ \ "singularOrPlural").write[String] and
      __.write[PaginationViewModel]
    )(
    o =>
      (
        o.dataRows,
        frontendAppConfig.declareDepartureStartWithLRNUrl,
        routes.WhatDoYouWantToDoController.onPageLoad().url,
        o.singularOrPlural,
        o.paginationViewModel
      )
  )

}
