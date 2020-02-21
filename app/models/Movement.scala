/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Reads, Writes, __}

case class Movement(movementReferenceNumber: String,
                    traderName: String,
                    presentationOffice: String,
                    procedure: String)

object Movement{

 implicit val writes: Writes[Movement] = Json.writes[Movement]

 implicit val reads: Reads[Movement] = (
   (__ \ "movementReferenceNumber").read[String] and
     (__ \ "trader" \ "name").read[String] and
     (__ \ "presentationOffice").read[String] and
     (__ \ "procedure").read[String]
   )(Movement.apply _)
}
