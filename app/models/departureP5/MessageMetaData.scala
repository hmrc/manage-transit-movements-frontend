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

import play.api.libs.json.{__, Reads}

import java.time.LocalDateTime

case class MessageMetaData(received: LocalDateTime, messageType: DepartureMessageType, path: String)

object MessageMetaData {

  implicit lazy val reads: Reads[MessageMetaData] = {
    import play.api.libs.functional.syntax._
    (
      (__ \ "received").read[LocalDateTime] and
        (__ \ "type").read[DepartureMessageType] and
        (__ \ "_links" \ "self" \ "href").read[String].map(_.replace("/customs/transits/", ""))
    )(MessageMetaData.apply _)
  }
}
