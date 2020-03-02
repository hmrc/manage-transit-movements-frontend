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

package models.referenceData

import java.time.{LocalDate, LocalTime}

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Movement(date: LocalDate,
                    time: LocalTime,
                    movementReferenceNumber: String,
                    traderName: String,
                    presentationOfficeId: String,
                    procedure: String)

object Movement {

  implicit val writes: OWrites[Movement] = new OWrites[Movement] {
    override def writes(o: Movement): JsObject = Json.obj(
      "updated"     -> o.time,
      "mrn"        -> o.movementReferenceNumber,
      "traderName" -> o.traderName,
      "office"     -> o.presentationOfficeId,
      "procedure"  -> o.procedure
    )
  }

  implicit val reads: Reads[Movement] = (
    (__ \ "date").read[LocalDate] and
      (__ \ "time").read[LocalTime] and
      (__ \ "message" \ "movementReferenceNumber").read[String] and
      (__ \ "message" \ "trader" \ "name").read[String] and
      (__ \ "message" \ "presentationOffice").read[String] and
      (__ \ "message" \ "procedure").read[String]
  )(Movement.apply _)
}
