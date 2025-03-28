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

import cats.data.NonEmptyList
import models.departureP5.DepartureMessageType.DepartureNotification
import models.nonEmptyListReads
import play.api.libs.json.{__, JsonValidationError, Reads}

case class DepartureMovementMessages(messages: NonEmptyList[DepartureMessage], ie015MessageId: String) {

  val latestMessage: DepartureMessage = messages.head

  def contains(messageType: DepartureMessageType): Boolean =
    messages.toList.map(_.messageType).contains(messageType)
}

object DepartureMovementMessages {

  implicit val reads: Reads[DepartureMovementMessages] =
    (__ \ "messages")
      .read[NonEmptyList[DepartureMessage]]
      .map(_.sorted)
      .map {
        messages =>
          (
            messages,
            messages.find {
              case DepartureMessage(_, _, DepartureNotification, _) => true
              case _                                                => false
            }
          )
      }
      .collect {
        JsonValidationError("could not find IE015 message")
      } {
        case (messages, Some(message)) =>
          DepartureMovementMessages(
            messages = messages,
            ie015MessageId = message.messageId
          )
      }

}
