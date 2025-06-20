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

package models.referenceData

import cats.Order
import config.FrontendAppConfig
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{__, Json, Reads}

case class InvalidGuaranteeReason(code: String, description: String) {

  override def toString: String =
    description match {
      case "" => code
      case _  => s"$code - $description"
    }

}

object InvalidGuaranteeReason {

  def reads(config: FrontendAppConfig): Reads[InvalidGuaranteeReason] =
    if (config.phase6Enabled) {
      (
        (__ \ "key").read[String] and
          (__ \ "value").read[String]
      )(InvalidGuaranteeReason.apply)
    } else {
      Json.reads[InvalidGuaranteeReason]
    }

  implicit val order: Order[InvalidGuaranteeReason] = (x: InvalidGuaranteeReason, y: InvalidGuaranteeReason) => (x, y).compareBy(_.code)

}
