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

sealed trait RejectionType {
  val code: String
}

object RejectionType {

  case object AmendmentRejection extends RejectionType {
    override val code = "013"
  }

  case object InvalidationRejection extends RejectionType {
    override val code = "014"
  }

  case object DeclarationRejection extends RejectionType {
    override val code = "015"
  }

  case object ReleaseRequestRejection extends RejectionType {
    override val code = "054"
  }

  case object RejectionOfInformation extends RejectionType {
    override val code = "141"
  }

  case object PresentationNotificationRejection extends RejectionType {
    override val code = "170"
  }

  case class Other(code: String) extends RejectionType

  val values = Seq(
    AmendmentRejection,
    InvalidationRejection,
    DeclarationRejection,
    ReleaseRequestRejection,
    RejectionOfInformation,
    PresentationNotificationRejection
  )

  implicit val reads: Reads[RejectionType] = Reads {
    case JsString(AmendmentRejection.code)                => JsSuccess(AmendmentRejection)
    case JsString(InvalidationRejection.code)             => JsSuccess(InvalidationRejection)
    case JsString(DeclarationRejection.code)              => JsSuccess(DeclarationRejection)
    case JsString(ReleaseRequestRejection.code)           => JsSuccess(ReleaseRequestRejection)
    case JsString(RejectionOfInformation.code)            => JsSuccess(RejectionOfInformation)
    case JsString(PresentationNotificationRejection.code) => JsSuccess(PresentationNotificationRejection)
    case JsString(code)                                   => JsSuccess(Other(code))
  }

  implicit val writes: Writes[RejectionType] = Writes {
    rejectionType => JsString(rejectionType.code)
  }

}
