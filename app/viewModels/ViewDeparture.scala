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

import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.time.format.DateTimeFormatter

import models.{Departure, LocalReferenceNumber}
import play.api.libs.json.{JsObject, Json, OWrites}

final case class ViewDeparture(createdDate: LocalDate,
                               createdTime: LocalTime,
                               localReferenceNumber: LocalReferenceNumber,
                               officeOfDeparture: String,
                               status: String)

object ViewDeparture {

  def apply(departure: Departure): ViewDeparture =
    ViewDeparture(
      createdDate          = departure.created.toLocalDate,
      createdTime          = departure.created.toLocalTime,
      localReferenceNumber = departure.localReferenceNumber,
      officeOfDeparture    = departure.officeOfDeparture,
      status               = departure.status
    )

  implicit val writes: OWrites[ViewDeparture] =
    new OWrites[ViewDeparture] {
      override def writes(o: ViewDeparture): JsObject = Json.obj(
        "createdDate" -> o.createdDate,
        "createdTime" -> o.createdTime
          .format(DateTimeFormatter.ofPattern("h:mma"))
          .toLowerCase,
        "localReferenceNumber" -> o.localReferenceNumber,
        "officeOfDeparture"    -> o.officeOfDeparture,
        "status"               -> o.status
      )
    }
}
