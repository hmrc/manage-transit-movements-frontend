/*
 * Copyright 2022 HM Revenue & Customs
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

import com.lucidchart.open.xtract.{__, XmlReader}
import cats.syntax.all._
import models.XMLReads._
import play.api.libs.json.{Json, OWrites}

case class ControlResult(datLimERS69: LocalDate, code: String)

object ControlResult {
  implicit val writes: OWrites[ControlResult] = Json.writes[ControlResult]

  implicit val xmlReader: XmlReader[ControlResult] =
    ((__ \ "ConDatERS14").read[LocalDate], (__ \ "ConResCodERS16").read[String]).mapN(apply)

}
