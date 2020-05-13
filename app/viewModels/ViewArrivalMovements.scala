/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDate
import java.time.chrono.ChronoLocalDate
import java.time.format.DateTimeFormatter

import config.FrontendAppConfig
import controllers.routes
import play.api.libs.json.{JsObject, Json, OWrites}

case class ViewArrivalMovements(
  dataRows: Map[String, Seq[ViewMovement]]
)

object ViewArrivalMovements {

  implicit val localDateOrdering: Ordering[LocalDate] =
    Ordering.by(identity[ChronoLocalDate])

  def apply(
    movements: Seq[ViewMovement]
  ): ViewArrivalMovements =
    ViewArrivalMovements(format(movements))

  private def format(
    movements: Seq[ViewMovement]
  ): Map[String, Seq[ViewMovement]] = {
    val groupMovements: Map[LocalDate, Seq[ViewMovement]] =
      movements.groupBy(_.date)
    val sortByDate: Seq[(LocalDate, Seq[ViewMovement])] =
      groupMovements.toSeq.sortBy(_._1).reverse

    sortByDate.map {
      result =>
        val dateFormatter: DateTimeFormatter =
          DateTimeFormatter.ofPattern("d MMMM yyyy")
        (result._1.format(dateFormatter), result._2.sortBy(_.time))
    }.toMap
  }

  implicit def writes(
    implicit frontendAppConfig: FrontendAppConfig
  ): OWrites[ViewArrivalMovements] =
    new OWrites[ViewArrivalMovements] {
      override def writes(o: ViewArrivalMovements): JsObject =
        Json.obj(
          "dataRows"                      -> o.dataRows,
          "declareArrivalNotificationUrl" -> frontendAppConfig.declareArrivalNotificationStartUrl,
          "homePageUrl"                   -> routes.IndexController.onPageLoad().url
        )
    }
}
