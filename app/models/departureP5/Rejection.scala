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

import generated.*
import models.departureP5.BusinessRejectionType.DepartureBusinessRejectionType
import play.api.libs.json.{Json, Writes}

sealed trait Rejection {
  val departureId: String
}

object Rejection {

  case class IE055Rejection(
    departureId: String
  ) extends Rejection

  object IE055Rejection {

    implicit val writes: Writes[IE055Rejection] = Writes {
      rejection =>
        Json.obj(
          "type"        -> "IE055",
          "departureId" -> rejection.departureId
        )
    }
  }

  case class IE056Rejection(
    departureId: String,
    businessRejectionType: DepartureBusinessRejectionType,
    errorPointers: Seq[String]
  ) extends Rejection

  object IE056Rejection {

    def apply(departureId: String, ie056: CC056CType): IE056Rejection =
      new IE056Rejection(
        departureId = departureId,
        businessRejectionType = DepartureBusinessRejectionType(ie056),
        errorPointers = ie056.FunctionalError.map(_.errorPointer)
      )

    implicit val writes: Writes[IE056Rejection] = Writes {
      rejection =>
        Json.obj(
          "type"                  -> "IE056",
          "departureId"           -> rejection.departureId,
          "businessRejectionType" -> rejection.businessRejectionType,
          "errorPointers"         -> rejection.errorPointers
        )
    }
  }
}
