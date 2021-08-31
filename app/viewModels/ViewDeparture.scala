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
import models.{Departure, LocalReferenceNumber}
import play.api.libs.json.{JsObject, Json, OWrites}

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime}

final case class ViewDeparture(updatedDate: LocalDate,
                               updatedTime: LocalTime,
                               localReferenceNumber: LocalReferenceNumber,
                               status: String,
                               actions: Seq[ViewMovementAction]
)

object ViewDeparture {

  def apply(departure: Departure, config: FrontendAppConfig): ViewDeparture = {
    val departureStatus = DepartureStatusViewModel(departure, config)
    ViewDeparture(
      updatedDate = departure.updated.toLocalDate,
      updatedTime = departure.updated.toLocalTime,
      localReferenceNumber = departure.localReferenceNumber,
      status = departureStatus.status,
      actions = departureStatus.actions
    )
  }

  implicit val writes: OWrites[ViewDeparture] =
    new OWrites[ViewDeparture] {

      override def writes(o: ViewDeparture): JsObject = Json.obj(
        "updated" -> o.updatedTime
          .format(DateTimeFormatter.ofPattern("h:mma"))
          .toLowerCase,
        "referenceNumber" -> o.localReferenceNumber,
        "status"          -> o.status,
        "actions"         -> o.actions
      )
    }
}
