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

import models.departureP5.DepartureMessageType.DepartureNotification
import play.api.libs.json.{__, JsonValidationError, Reads}

case class LatestDepartureMessage(latestMessage: DepartureMessage, ie015MessageId: String)

object LatestDepartureMessage {

  implicit val reads: Reads[LatestDepartureMessage] =
    (__ \ "messages")
      .read[List[DepartureMessage]]
      .filter(
        JsonValidationError("expected a NonEmptyList but the list was empty")
      )(
        _.nonEmpty
      )
      .filter(
        JsonValidationError("could not find IE015 message")
      )(
        _.exists(_.messageType == DepartureNotification)
      )
      .map {
        list =>
          val sortedList: List[DepartureMessage] = list.sortBy(_.received).reverse

          val ie015Message = sortedList.filter(_.messageType == DepartureNotification).head

          LatestDepartureMessage(sortedList.head, ie015Message.messageId)
      }
}
