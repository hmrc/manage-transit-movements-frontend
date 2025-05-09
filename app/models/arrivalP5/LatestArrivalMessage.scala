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

package models.arrivalP5

import cats.data.NonEmptyList
import models.arrivalP5.ArrivalMessageType.ArrivalNotification
import models.nonEmptyListReads
import play.api.libs.json.{__, JsonValidationError, Reads}

case class LatestArrivalMessage(latestMessage: ArrivalMessage, ie007Id: String)

object LatestArrivalMessage {

  implicit val reads: Reads[LatestArrivalMessage] =
    (__ \ "messages")
      .read[NonEmptyList[ArrivalMessage]]
      .map(_.sorted)
      .map {
        messages =>
          (
            messages,
            messages.find {
              case ArrivalMessage(_, _, ArrivalNotification, _) => true
              case _                                            => false
            }
          )
      }
      .collect {
        JsonValidationError("could not find IE007 message")
      } {
        case (messages, Some(message)) =>
          LatestArrivalMessage(
            latestMessage = messages.head,
            ie007Id = message.messageId
          )
      }

}
