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

import models.departureP5.IE060MessageType.GoodsUnderControlRequestedDocuments
import play.api.libs.json.{Json, OFormat}

case class IE060MessageData(
  TransitOperation: TransitOperationIE060,
  CustomsOfficeOfDeparture: CustomsOfficeOfDeparture,
  TypeOfControls: Option[Seq[TypeOfControls]],
  RequestedDocument: Option[Seq[RequestedDocument]]
) {
  val typeOfControlsToSeq: Seq[TypeOfControls] = TypeOfControls.getOrElse(Seq.empty)

  val requestedDocumentsToSeq: Seq[RequestedDocument] = RequestedDocument.getOrElse(Seq.empty)

  val requestedDocuments: Boolean =
    requestedDocumentsToSeq.nonEmpty || TransitOperation.notificationType == GoodsUnderControlRequestedDocuments
}

object IE060MessageData {
  implicit val formats: OFormat[IE060MessageData] = Json.format[IE060MessageData]
}
