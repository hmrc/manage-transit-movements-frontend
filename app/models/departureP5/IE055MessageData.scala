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

import play.api.libs.functional.syntax._
import play.api.libs.json.{__, Reads}

case class IE055MessageData(
  transitOperation: TransitOperationIE055,
  guaranteeReferences: Seq[GuaranteeReference]
)

object IE055MessageData {

  implicit lazy val reads: Reads[IE055MessageData] = (
    (__ \ "TransitOperation").read[TransitOperationIE055] and
      (__ \ "GuaranteeReference").read[Seq[GuaranteeReference]]
  )(IE055MessageData.apply _)
}
