/*
 * Copyright 2021 HM Revenue & Customs
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

package models.departure

import java.time.LocalDate

import cats.implicits.catsSyntaxTuple4Semigroupal
import com.lucidchart.open.xtract.{__, XmlReader}
import models.XMLReads._
import play.api.libs.json.{Json, OWrites}
import utils.Format

case class ControlDecision(
  movementReferenceNumber: String,
  dateOfControl: LocalDate,
  principleTraderName: String,
  principleEori: Option[String]
)

object ControlDecision {
 
  implicit val writes: OWrites[ControlDecision] =
    (controlDecision: ControlDecision) =>
      Json.obj(
        "movementReferenceNumber" -> controlDecision.movementReferenceNumber,
        "dateOfControl"           -> Format.controlDecisionDateFormatted(controlDecision.dateOfControl),
        "principleTraderName"     -> controlDecision.principleTraderName,
        "principleEori"           -> controlDecision.principleEori
      )

  implicit val xmlReader: XmlReader[ControlDecision] = (
    (__ \ "HEAHEA" \ "DocNumHEA5").read[String],
    (__ \ "HEAHEA" \ "DatOfConNotHEA148").read[LocalDate],
    (__ \ "TRAPRIPC1" \ "NamPC17").read[String],
    (__ \ "TRAPRIPC1" \ "TINPC159").read[String].optional
  ).mapN(apply)
}
