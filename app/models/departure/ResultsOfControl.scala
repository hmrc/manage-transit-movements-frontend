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

package models.departure

import cats.syntax.all._
import com.lucidchart.open.xtract.{__, XmlReader}
import play.api.libs.json.{Json, OWrites}

case class ResultsOfControl(controlIndicator: String, description: Option[String])

object ResultsOfControl {
  val maxResultsOfControl = 9

  implicit val writes: OWrites[ResultsOfControl] = Json.writes[ResultsOfControl]

  implicit val xmlReader: XmlReader[ResultsOfControl] =
    ((__ \ "ConInd424").read[String], (__ \ "DesTOC2").read[String].optional).mapN(apply)
}
