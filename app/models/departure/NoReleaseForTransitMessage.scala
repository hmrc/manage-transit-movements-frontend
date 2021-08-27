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

import cats.syntax.all._
import com.lucidchart.open.xtract.{__, XmlReader}
import play.api.libs.json.{Json, OWrites}
import models.XMLReads._

case class NoReleaseForTransitMessage(mrn: String,
                                      noReleaseMotivation: Option[String],
                                      totalNumberOfItems: Int,
                                      officeOfDepartureRefNumber: String,
                                      controlResult: ControlResult,
                                      resultsOfControl: Option[Seq[ResultsOfControl]]
)

object NoReleaseForTransitMessage {
  implicit val writes: OWrites[NoReleaseForTransitMessage] = Json.writes[NoReleaseForTransitMessage]

  implicit val xmlReader: XmlReader[NoReleaseForTransitMessage] = (
    (__ \ "HEAHEA" \ "DocNumHEA5").read[String],
    (__ \ "HEAHEA" \ "NoRelMotHEA272").read[String].optional,
    (__ \ "HEAHEA" \ "TotNumOfIteHEA305").read[Int],
    (__ \ "CUSOFFDEPEPT" \ "RefNumEPT1").read[String],
    (__ \ "CONRESERS").read[ControlResult],
    (__ \ "RESOFCON534").read(strictReadOptionSeq[ResultsOfControl])
  ).mapN(apply)
}
