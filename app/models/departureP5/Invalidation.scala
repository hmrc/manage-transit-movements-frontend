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

import models.booleanReads
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDateTime

case class Invalidation(decisionDateAndTime: Option[LocalDateTime], decision: Boolean, initiatedByCustoms: Boolean, justification: Option[String])

object Invalidation {

  implicit val reads: Reads[Invalidation] = (
    (__ \ "decisionDateAndTime").readNullable[LocalDateTime] and
      (__ \ "decision").read[Boolean](booleanReads) and
      (__ \ "initiatedByCustoms").read[Boolean](booleanReads) and
      (__ \ "justification").readNullable[String]
  )(Invalidation.apply _)
}
