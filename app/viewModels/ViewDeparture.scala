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
import models.{Departure, LocalReferenceNumber, RichLocalDateTime}
import play.api.libs.json.{Json, OWrites}

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate, LocalTime}

final case class ViewDeparture(updatedDate: LocalDate,
                               updatedTime: LocalTime,
                               localReferenceNumber: LocalReferenceNumber,
                               status: String,
                               actions: Seq[ViewMovementAction]
) extends ViewMovement {

  override val referenceNumber: String = localReferenceNumber.value
}

object ViewDeparture {

  def apply(departure: Departure)(implicit config: FrontendAppConfig, clock: Clock): ViewDeparture = {

    val departureStatus = DepartureStatusViewModel(departure)

    val systemTime = departure.updated.toSystemDefaultTime

    ViewDeparture(
      updatedDate = systemTime.toLocalDate,
      updatedTime = systemTime.toLocalTime,
      localReferenceNumber = departure.localReferenceNumber,
      status = departureStatus.status,
      actions = departureStatus.actions
    )
  }

  implicit val writes: OWrites[ViewDeparture] =
    (o: ViewDeparture) =>
      Json.obj(
        "updated" -> o.updatedTime
          .format(DateTimeFormatter.ofPattern("h:mma"))
          .toLowerCase,
        "referenceNumber" -> o.localReferenceNumber,
        "status"          -> o.status,
        "actions"         -> o.actions
      )
}
