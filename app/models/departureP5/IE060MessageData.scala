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

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class IE060MessageData(
  TransitOperation: TransitOperation,
  CustomsOfficeOfDeparture: CustomsOfficeOfDeparture,
  TypeOfControls: Option[Seq[TypeOfControls]],
  RequestedDocument: Option[Seq[RequestedDocument]]
)

case class TransitOperation(MRN: String, LRN: String, controlNotificationDateAndTime: LocalDateTime, notificationType: String)

object TransitOperation {
  implicit val formats: OFormat[TransitOperation] = Json.format[TransitOperation]
}

case class TypeOfControls(
  sequenceNumber: String,
  `type`: String,
  text: Option[String]
)

object TypeOfControls {
  implicit val formats: OFormat[TypeOfControls] = Json.format[TypeOfControls]
}

case class RequestedDocument(
  sequenceNumber: String,
  documentType: String,
  description: Option[String]
)

object RequestedDocument {
  implicit val formats: OFormat[RequestedDocument] = Json.format[RequestedDocument]
}

object IE060MessageData {
  implicit val formats: OFormat[IE060MessageData] = Json.format[IE060MessageData]
}
