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

import models.{Enumerable, WithName}
import play.api.libs.json.{JsError, JsString, JsSuccess, Reads, Writes}

trait Prelodged

object Prelodged {

  case object NonPrelodgedDeclaration extends WithName("A") with Prelodged
  case object PrelodgedDeclaration extends WithName("D") with Prelodged
  case class OtherDeclaration(additionalDeclarationType: String) extends WithName(additionalDeclarationType) with Prelodged

  val values: Seq[Prelodged] = Seq(
    NonPrelodgedDeclaration,
    PrelodgedDeclaration
  )

  implicit val enumerable: Enumerable[Prelodged] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )

  implicit def reads(implicit ev: Enumerable[Prelodged]): Reads[Prelodged] =
    Reads {
      case JsString(str) =>
        ev.withName(str)
          .map(JsSuccess(_))
          .getOrElse(
            JsSuccess(OtherDeclaration(str))
          )
      case _ =>
        JsError("error.invalid")
    }

  implicit def writes: Writes[Prelodged] =
    Writes(
      value => JsString(value.toString)
    )

}
