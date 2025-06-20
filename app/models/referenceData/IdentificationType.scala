/*
 * Copyright 2024 HM Revenue & Customs
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
import models.referenceData.RichComparison
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{__, Json, OFormat, Reads}

case class IdentificationType(`type`: String, description: String) {
  override def toString: String = s"$description - ${`type`}"
}

object IdentificationType {

  def reads(config: FrontendAppConfig): Reads[IdentificationType] =
    if (config.phase6Enabled) {
      (
        (__ \ "key").read[String] and
          (__ \ "value").read[String]
      )(IdentificationType.apply)
    } else {
      Json.reads[IdentificationType]
    }

  implicit val format: OFormat[IdentificationType] = Json.format[IdentificationType]

  implicit val order: Order[IdentificationType] = (x: IdentificationType, y: IdentificationType) => (x, y).compareBy(_.description, _.`type`)
}
