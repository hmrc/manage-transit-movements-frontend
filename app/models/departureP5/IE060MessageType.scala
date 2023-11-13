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

import play.api.libs.json.{JsError, JsString, JsSuccess, Reads, Writes}

sealed trait IE060MessageType {
  val messageType: String
}

object IE060MessageType {

  case object GoodsUnderControl extends IE060MessageType {
    override val messageType = "0"
  }

  case object GoodsUnderControlRequestedDocuments extends IE060MessageType {
    override val messageType = "1"
  }

  case object IntentionToControl extends IE060MessageType {
    override val messageType = "2"
  }

  implicit val reads: Reads[IE060MessageType] = Reads {
    case JsString(GoodsUnderControl.messageType)                   => JsSuccess(GoodsUnderControl)
    case JsString(GoodsUnderControlRequestedDocuments.messageType) => JsSuccess(GoodsUnderControlRequestedDocuments)
    case JsString(IntentionToControl.messageType)                  => JsSuccess(IntentionToControl)
    case _                                                         => JsError("Failed to read IE060MessageType")
  }

  implicit val writes: Writes[IE060MessageType] = Writes {
    messageType => JsString(messageType.messageType)
  }
}
