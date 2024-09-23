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

import models.arrivalP5.ArrivalMessageType.ArrivalNotification
import play.api.libs.json.{__, JsonValidationError, Reads}

case class LatestArrivalMessage(latestMessage: ArrivalMessage, ie007Id: String)

object LatestArrivalMessage {

  implicit val reads: Reads[LatestArrivalMessage] =
    (__ \ "messages")
      .read[List[ArrivalMessage]]
      .filter(
        JsonValidationError("expected a NonEmptyList but the list was empty")
      )(
        _.nonEmpty
      )
      .filter(
        JsonValidationError("could not find IE007 message")
      )(
        _.exists(_.messageType == ArrivalNotification)
      )
      .map {
        list =>
          val sortedList: List[ArrivalMessage] = list.sortBy(_.received).reverse

          val ie007Message = sortedList.filter(_.messageType == ArrivalNotification).head

          LatestArrivalMessage(sortedList.head, ie007Message.messageId)
      }

}
