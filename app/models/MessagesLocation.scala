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

import play.api.libs.json.{__, Reads}

case class MessagesLocation(departureMessage: String,
                            guaranteeNotValid: Option[String] = None,
                            declarationRejection: Option[String],
                            cancellationDecisionUpdate: Option[String],
                            declarationCancellation: Option[String])

object MessagesLocation {

  import play.api.libs.functional.syntax._

  implicit val reads: Reads[MessagesLocation] = {
    ((__ \ "IE015").read[String] and
      (__ \ "IE055").readNullable[String] and
      (__ \ "IE016").readNullable[String] and
      (__ \ "IE009").readNullable[String] and
      (__ \ "IE014").readNullable[String])(MessagesLocation.apply _)
  }
}
