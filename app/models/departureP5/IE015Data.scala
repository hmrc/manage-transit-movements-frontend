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

import models.departureP5.Prelodged.PrelodgedDeclaration
import play.api.libs.json.{__, Reads}

case class IE015Data(data: IE015MessageData) {
  val isPrelodged: Boolean = data.transitOperation.additionalDeclarationType == PrelodgedDeclaration
}

object IE015Data {

  implicit val reads: Reads[IE015Data] = (__ \ "body" \ "n1:CC015C").read[IE015MessageData].map(IE015Data.apply)
}
