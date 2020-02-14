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

import play.api.i18n.Messages
import play.api.libs.functional.syntax._
import play.api.libs.json.{OWrites, _}
import uk.gov.hmrc.viewmodels.SummaryList.Action

case class Movement(
                     updated: String,
                     mrn: String,
                     traderName: String,
                     office: String,
                     procedure: String,
                     status: String,
                     actions: Seq[Action])

object Movement{

 implicit def writes(implicit messages: Messages): OWrites[Movement] = (
   (__ \ "updated").write[String] and
     (__ \ "mrn").write[String] and
     (__ \ "traderName").write[String] and
     (__ \ "office").write[String] and
     (__ \ "procedure").write[String] and
     (__ \ "status").write[String] and
     (__ \ "actions" \ "items").writeNullable[Seq[Action]]
   ){ movement =>
  val actions = Some(movement.actions).filter(_.nonEmpty)
  (movement.updated, movement.mrn, movement.traderName, movement.office, movement.procedure, movement.status,  actions)
 }
}
