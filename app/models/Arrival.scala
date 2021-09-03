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

import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}

case class Arrival(
  arrivalId: ArrivalId,
  created: LocalDateTime,
  updated: LocalDateTime,
  status: String,
  movementReferenceNumber: String
)

object Arrival {

  implicit val reads: Reads[Arrival] = (
    (__ \ "arrivalId").read[ArrivalId] and
      (__ \ "created").read[LocalDateTime] and
      (__ \ "updated").read[LocalDateTime] and
      (__ \ "status").read[String] and
      (__ \ "movementReferenceNumber").read[String]
  )(Arrival.apply _)
}
