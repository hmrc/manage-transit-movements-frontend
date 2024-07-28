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

import generated.CC056CType
import play.api.libs.json.{JsString, Writes}

sealed trait BusinessRejectionType {
  val value: String
}

object BusinessRejectionType {

  sealed trait DepartureBusinessRejectionType extends BusinessRejectionType

  object DepartureBusinessRejectionType {

    def apply(value: String): DepartureBusinessRejectionType = value match {
      case AmendmentRejection.value   => AmendmentRejection
      case DeclarationRejection.value => DeclarationRejection
      case x                          => throw new IllegalArgumentException(s"Unexpected business rejection type: $x")
    }

    def apply(ie056: CC056CType): DepartureBusinessRejectionType =
      DepartureBusinessRejectionType.apply(ie056.TransitOperation.businessRejectionType)

    implicit val writes: Writes[DepartureBusinessRejectionType] = Writes {
      x => JsString(x.value)
    }
  }

  case object AmendmentRejection extends DepartureBusinessRejectionType {
    override val value: String = "013"
  }

  case object InvalidationRejection extends BusinessRejectionType {
    override val value: String = "014"
  }

  case object DeclarationRejection extends DepartureBusinessRejectionType {
    override val value: String = "015"
  }

  case class OtherBusinessRejectionType(value: String) extends BusinessRejectionType

  def apply(value: String): BusinessRejectionType = value match {
    case AmendmentRejection.value    => AmendmentRejection
    case InvalidationRejection.value => InvalidationRejection
    case DeclarationRejection.value  => DeclarationRejection
    case value                       => OtherBusinessRejectionType(value)
  }

  def apply(ie056: CC056CType): BusinessRejectionType =
    BusinessRejectionType.apply(ie056.TransitOperation.businessRejectionType)
}
