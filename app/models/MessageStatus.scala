/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import play.api.libs.json.*

sealed trait MessageStatus

object MessageStatus {

  case object Received extends MessageStatus
  case object Pending extends MessageStatus
  case object Processing extends MessageStatus
  case object Success extends MessageStatus
  case object Failed extends MessageStatus

  implicit val reads: Reads[MessageStatus] = Reads {
    case JsString("Received")   => JsSuccess(Received)
    case JsString("Pending")    => JsSuccess(Pending)
    case JsString("Processing") => JsSuccess(Processing)
    case JsString("Success")    => JsSuccess(Success)
    case JsString("Failed")     => JsSuccess(Failed)
    case x                      => JsError(s"Could not parse $x as a MessageStatus")
  }
}
