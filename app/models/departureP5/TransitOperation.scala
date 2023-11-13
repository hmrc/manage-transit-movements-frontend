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

import models.RejectionType
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Json, OFormat, Reads}

import java.time.{LocalDate, LocalDateTime}

case class TransitOperation(MRN: Option[String], LRN: Option[String], controlNotificationDateAndTime: LocalDateTime, notificationType: String)
case class TransitOperationIE009(MRN: Option[String])
case class TransitOperationIE035(MRN: String, declarationAcceptanceDate: LocalDate)
case class TransitOperationIE055(MRN: String, declarationAcceptanceDate: LocalDate)
case class TransitOperationIE015(additionalDeclarationType: Prelodged)
case class TransitOperationIE056(MRN: Option[String], LRN: Option[String], businessRejectionType: RejectionType)
case class TransitOperationIE060(MRN: Option[String], LRN: Option[String], controlNotificationDateAndTime: LocalDateTime, notificationType: String)
case class TransitOperationIE051(MRN: String, declarationSubmissionDateAndTime: LocalDateTime, noReleaseMotivationCode: String, noReleaseMotivationText: String)

object TransitOperation {
  implicit val formats: OFormat[TransitOperation] = Json.format[TransitOperation]
}

object TransitOperationIE009 {
  implicit val formats: OFormat[TransitOperationIE009] = Json.format[TransitOperationIE009]
}

object TransitOperationIE035 {
  implicit val formats: OFormat[TransitOperationIE035] = Json.format[TransitOperationIE035]
}

object TransitOperationIE055 {

  implicit val reads: Reads[TransitOperationIE055] = (
    (__ \ "MRN").read[String] and
      (__ \ "declarationAcceptanceDate").read[LocalDate]
  )(TransitOperationIE055.apply _)
}

object TransitOperationIE051 {

  implicit val reads: Reads[TransitOperationIE051] = ((__ \ "MRN").read[String] and
    (__ \ "declarationSubmissionDateAndTime").read[LocalDateTime] and
    (__ \ "noReleaseMotivationCode").read[String] and
    (__ \ "noReleaseMotivationText").read[String])(TransitOperationIE051.apply _)
}

object TransitOperationIE056 {

  implicit val reads: Reads[TransitOperationIE056] = (
    (__ \ "MRN").readNullable[String] and
      (__ \ "LRN").readNullable[String] and
      (__ \ "businessRejectionType").read[RejectionType]
  )(TransitOperationIE056.apply _)
}

object TransitOperationIE060 {
  implicit val formats: OFormat[TransitOperationIE060] = Json.format[TransitOperationIE060]
}

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
}

object TransitOperationIE015 {
  implicit val formats: OFormat[TransitOperationIE015] = Json.format[TransitOperationIE015]
}
