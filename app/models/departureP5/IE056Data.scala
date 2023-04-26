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

import play.api.libs.json.{__, Json, OWrites, Reads}

case class IE056Data(data: IE056MessageData)

object IE056Data {
  implicit val reads: Reads[IE056Data]    = (__ \ "body" \ "n1:CC056C").read[IE056MessageData].map(IE056Data.apply)
  implicit val writes: OWrites[IE056Data] = Json.writes[IE056Data]
}
