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

package models

import play.api.libs.json._

sealed trait ArrivalRejectionType {
  val code: String
}

object ArrivalRejectionType {

  case object ArrivalNotificationRejection extends ArrivalRejectionType {
    override val code = "007"
  }

  case object UnloadingRemarkRejection extends ArrivalRejectionType {
    override val code = "044"
  }

  case class Other(code: String) extends ArrivalRejectionType

  implicit val reads: Reads[ArrivalRejectionType] = Reads {
    case JsString(ArrivalNotificationRejection.code) => JsSuccess(ArrivalNotificationRejection)
    case JsString(UnloadingRemarkRejection.code)     => JsSuccess(UnloadingRemarkRejection)
    case JsString(code)                              => JsSuccess(Other(code))
    case _                                           => JsError("Failed to read arrival rejection type")
  }

  implicit val writes: Writes[ArrivalRejectionType] = Writes {
    rejectionType => JsString(rejectionType.code)
  }

}
