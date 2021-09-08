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

package models

import java.time.LocalDateTime

import models.departure.DepartureStatus
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Json, Reads}

case class Departure(departureId: DepartureId, updated: LocalDateTime, localReferenceNumber: LocalReferenceNumber, status: DepartureStatus)

object Departure {

  implicit val reads: Reads[Departure] = (
    (__ \ "departureId").read[DepartureId] and
      (__ \ "updated").read[LocalDateTime] and
      (__ \ "referenceNumber").read[LocalReferenceNumber] and
      (__ \ "status").read[DepartureStatus]
  )(Departure.apply _)
}

case class Departures(
  retrievedDepartures: Int,
  totalDepartures: Int,
  totalMatched: Option[Int],
  departures: Seq[Departure]
)

object Departures {
  implicit val reads: Reads[Departures] = Json.reads[Departures]
}
