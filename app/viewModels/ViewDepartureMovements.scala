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

case class ViewDepartureMovements(dataRows: Map[String, Seq[ViewDeparture]])

object ViewDepartureMovements {

  implicit val localDateOrdering: Ordering[LocalDate] =
    Ordering.by(identity[ChronoLocalDate])

  def apply(departures: Seq[ViewDeparture]): ViewDepartureMovements =
    ViewDepartureMovements(format(departures))

  private def format(departures: Seq[ViewDeparture]): Map[String, Seq[ViewDeparture]] = {
    val groupDepartures: Map[LocalDate, Seq[ViewDeparture]] =
      departures.groupBy(_.createdDate)
    val sortByDate: Seq[(LocalDate, Seq[ViewDeparture])] =
      groupDepartures.toSeq.sortBy(_._1).reverse

    sortByDate.map {
      result =>
        val dateFormater: DateTimeFormatter =
          DateTimeFormatter.ofPattern("d MMMM yyyy")
        (result._1.format(dateFormater), result._2.sortBy(_.createdTime))
    }.toMap

  }

  implicit def writes(implicit frontendAppConfig: FrontendAppConfig): OWrites[ViewDepartureMovements] =
    new OWrites[ViewDepartureMovements] {
      override def writes(o: ViewDepartureMovements): JsObject = Json.obj(
        "dataRows"                        -> o.dataRows,
        "declareDepartureNotificationUrl" -> frontendAppConfig.declareDepartureStartWithLRNUrl,
        "homePageUrl"                     -> routes.IndexController.onPageLoad().url
      )
    }
}
