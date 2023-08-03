/*
 * Copyright 2023 HM Revenue & Customs
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

package models.departureP5

import models.{LocalReferenceNumber, Movement}
import play.api.libs.json.{__, Reads}

import java.time.LocalDateTime

case class DepartureMovement(
  departureId: String,
  movementReferenceNumber: Option[String],
  localReferenceNumber: LocalReferenceNumber,
  updated: LocalDateTime,
  messagesLocation: String
) extends Movement

object DepartureMovement {

  implicit lazy val reads: Reads[DepartureMovement] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "id").read[String] and
        (__ \ "movementReferenceNumber").readNullable[String] and
        (__ \ "localReferenceNumber").read[LocalReferenceNumber] and
        (__ \ "updated").read[LocalDateTime] and
        (__ \ "_links" \ "messages" \ "href")
          .read[String]
          .map(_.replace("/customs/transits/", ""))
    )(DepartureMovement.apply _)
  }
}
