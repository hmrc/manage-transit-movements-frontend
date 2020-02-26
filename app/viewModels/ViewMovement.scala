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

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime}

import models.Movement
import play.api.libs.json.{JsObject, Json, OWrites}

case class ViewMovement(
                        date: LocalDate,
                        time: LocalTime,
                        movementReferenceNumber: String,
                        traderName: String,
                        presentationOfficeId: String,
                        presentationOfficeName: String,
                        procedure: String
                       )

object ViewMovement {
  implicit val writes: OWrites[ViewMovement] = new OWrites[ViewMovement] {

    override def writes(o: ViewMovement): JsObject = Json.obj(
      "updated"     -> o.time.format(DateTimeFormatter.ofPattern("h:mma")).toLowerCase,
      "mrn"        -> o.movementReferenceNumber,
      "traderName" -> o.traderName,
      "office"     -> s"${o.presentationOfficeName} (${o.presentationOfficeId})",
      "procedure"  -> o.procedure,
      "actions"    -> Seq("history"),             // TODO: This will be decided based on the message type
      "status"     -> "Arrival notification sent" // TODO: In future we will pull this status from the backend
    )
  }
}
