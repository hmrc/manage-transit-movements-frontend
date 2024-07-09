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

package models.departureP5

import config.Constants
import generated.CC056CType

sealed trait BusinessRejectionType {
  val value: String
}

object BusinessRejectionType {

  case object AmendmentRejection extends BusinessRejectionType {
    override val value: String = Constants.BusinessRejectionType.AmendmentRejection
  }

  case object DeclarationRejection extends BusinessRejectionType {
    override val value: String = Constants.BusinessRejectionType.DeclarationRejection
  }

  def apply(value: String): BusinessRejectionType = value match {
    case AmendmentRejection.value   => AmendmentRejection
    case DeclarationRejection.value => DeclarationRejection
    case x                          => throw new IllegalArgumentException(s"Unexpected business rejection type: $x")
  }

  def apply(ie056: CC056CType): BusinessRejectionType =
    BusinessRejectionType.apply(ie056.TransitOperation.businessRejectionType)
}
