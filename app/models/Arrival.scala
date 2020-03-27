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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads, Writes}

case class Arrival(created: ArrivalDateTime, updated: ArrivalDateTime, state: String, movementReferenceNumber: String)

object Arrival {
  implicit val reads: Reads[Arrival] = (
    (__ \ "created").read[ArrivalDateTime] and
      (__ \ "updated").read[ArrivalDateTime] and
      (__ \ "state").read[String] and
      (__ \ "movementReferenceNumber").read[String]
  )(Arrival.apply _)

  implicit val writes: Writes[Arrival] = (
    (__ \ "created").write[ArrivalDateTime] and
      (__ \ "updated").write[ArrivalDateTime] and
      (__ \ "state").write[String] and
      (__ \ "movementReferenceNumber").write[String]
  )(unlift(Arrival.unapply))
}
