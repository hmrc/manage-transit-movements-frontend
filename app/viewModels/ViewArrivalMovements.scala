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

import config.FrontendAppConfig
import controllers.routes
import play.api.libs.json.{Json, OWrites}

import java.time.LocalDate
import java.time.chrono.ChronoLocalDate
import java.time.format.DateTimeFormatter

case class ViewArrivalMovements(
  dataRows: Seq[(String, Seq[ViewArrival])]
)

object ViewArrivalMovements {

  implicit val localDateOrdering: Ordering[LocalDate] =
    Ordering.by(identity[ChronoLocalDate])

  def apply(
    movements: Seq[ViewArrival]
  )(implicit d: DummyImplicit): ViewArrivalMovements =
    ViewArrivalMovements(format(movements))

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

  implicit def writes(implicit
    frontendAppConfig: FrontendAppConfig
  ): OWrites[ViewArrivalMovements] =
    (o: ViewArrivalMovements) =>
      Json.obj(
        "dataRows"                      -> o.dataRows,
        "declareArrivalNotificationUrl" -> frontendAppConfig.declareArrivalNotificationStartUrl,
        "homePageUrl"                   -> routes.WhatDoYouWantToDoController.onPageLoad().url
      )
}
