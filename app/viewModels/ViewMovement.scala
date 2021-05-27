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

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime}

import config.FrontendAppConfig
import models.Arrival
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json, OWrites}

final case class ViewMovement(date: LocalDate, time: LocalTime, movementReferenceNumber: String, status: String, action: Seq[ViewMovementAction])

object ViewMovement {

  def apply(arrival: Arrival)(implicit messages: Messages, frontendAppConfig: FrontendAppConfig): ViewMovement = {

    val movementStatus: MovementStatus = MovementStatus(arrival)

    ViewMovement(
      arrival.updated.toLocalDate,
      arrival.updated.toLocalTime,
      arrival.movementReferenceNumber,
      movementStatus.status,
      movementStatus.actions
    )
  }

  implicit val writes: OWrites[ViewMovement] =
    new OWrites[ViewMovement] {

      override def writes(o: ViewMovement): JsObject = Json.obj(
        "updated" -> o.time
          .format(DateTimeFormatter.ofPattern("h:mma"))
          .toLowerCase,
        "mrn"     -> o.movementReferenceNumber,
        "status"  -> o.status,
        "actions" -> o.action
      )
    }
}
