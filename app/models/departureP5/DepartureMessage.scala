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

import cats.Order
import models.MessageStatus
import play.api.libs.json.{__, Reads}

import java.time.LocalDateTime

case class DepartureMessage(messageId: String, received: LocalDateTime, messageType: DepartureMessageType, status: MessageStatus)

object DepartureMessage {

  implicit lazy val reads: Reads[DepartureMessage] = {
    import play.api.libs.functional.syntax.*
    (
      (__ \ "id").read[String] and
        (__ \ "received").read[LocalDateTime] and
        (__ \ "type").read[DepartureMessageType] and
        (__ \ "status").read[MessageStatus]
    )(DepartureMessage.apply)
  }

  implicit val order: Order[DepartureMessage] =
    Order.fromOrdering(Ordering.by[DepartureMessage, LocalDateTime](_.received).reverse)
}
