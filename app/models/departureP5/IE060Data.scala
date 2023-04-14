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

case class IE060Data(data: MessageData)

object IE060Data {
  implicit val reads: Reads[IE060Data]    = (__ \ "body" \ "n1:CC060C").read[MessageData].map(IE060Data.apply)
  implicit val writes: OWrites[IE060Data] = Json.writes[IE060Data]
}
