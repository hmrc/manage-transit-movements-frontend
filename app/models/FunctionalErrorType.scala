/*
 * Copyright 2025 HM Revenue & Customs
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

import generated.*
import play.api.libs.functional.syntax.*
import play.api.libs.json.{__, Writes}

case class FunctionalErrorType(
  errorPointer: String,
  errorCode: String,
  errorReason: String,
  originalAttributeValue: Option[String]
)

object FunctionalErrorType {

  def apply(value: FunctionalErrorType02): FunctionalErrorType =
    new FunctionalErrorType(
      errorPointer = value.errorPointer,
      errorCode = value.errorCode,
      errorReason = value.errorReason,
      originalAttributeValue = value.originalAttributeValue
    )

  def apply(value: FunctionalErrorType07): FunctionalErrorType =
    new FunctionalErrorType(
      errorPointer = value.errorPointer,
      errorCode = value.errorCode.toString,
      errorReason = value.errorReason,
      originalAttributeValue = value.originalAttributeValue
    )

  implicit val writes: Writes[FunctionalErrorType] = (
    (__ \ "errorPointer").write[String] and
      (__ \ "errorCode").write[String] and
      (__ \ "errorReason").write[String] and
      (__ \ "originalAttributeValue").writeNullable[String]
  )(
    functionalError => (functionalError.errorPointer, functionalError.errorCode, functionalError.errorReason, functionalError.originalAttributeValue)
  )
}
