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

import models.departure.{DepartureMessageMetaData, DepartureStatus}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, __}

import java.time.LocalDateTime

case class Departure(departureId: DepartureId, updated: LocalDateTime, localReferenceNumber: LocalReferenceNumber, latestMessages: Seq[DepartureMessageMetaData]) {
  def currentStatus: DepartureStatus = ???
  def previousStatus: DepartureStatus = ???
}

object Departure {

  implicit val reads: Reads[Departure] = (
    (__ \ "departureId").read[DepartureId] and
      (__ \ "updated").read[LocalDateTime] and
      (__ \ "referenceNumber").read[LocalReferenceNumber] and
      (__ \ "latestMessages").read[Seq[DepartureMessageMetaData]]
  )(Departure.apply _)
}
