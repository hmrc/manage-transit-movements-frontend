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

package models.arrival

import cats.syntax.all._
import com.lucidchart.open.xtract.XmlReader._
import com.lucidchart.open.xtract.{__, XmlReader}
import models.FunctionalError
import play.api.libs.json.{Json, OWrites}

final case class XMLSubmissionNegativeAcknowledgementMessage(movementReferenceNumber: Option[String],
                                                             localReferenceNumber: Option[String],
                                                             error: FunctionalError
)

object XMLSubmissionNegativeAcknowledgementMessage {

  implicit val writes: OWrites[XMLSubmissionNegativeAcknowledgementMessage] = Json.writes[XMLSubmissionNegativeAcknowledgementMessage]

  implicit val xmlReader: XmlReader[XMLSubmissionNegativeAcknowledgementMessage] = (
    (__ \ "HEAHEA" \ "DocNumHEA5").read[String].optional,
    (__ \ "HEAHEA" \ "RefNumHEA4").read[String].optional,
    (__ \ "FUNERRER1").read[FunctionalError]
  ).mapN(apply)
}
