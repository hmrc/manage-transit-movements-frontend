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

case class Rejection(
  departureId: String,
  `type`: String,
  businessRejectionType: Option[DepartureBusinessRejectionType],
  errorPointers: Option[Seq[String]]
)

object Rejection {

  def apply(departureId: String): Rejection =
    new Rejection(departureId, "IE055", None, None)

  def apply(departureId: String, businessRejectionType: DepartureBusinessRejectionType, errorPointers: Seq[String]): Rejection =
    new Rejection(departureId, "IE056", Some(businessRejectionType), Some(errorPointers))

  def apply(departureId: String, ie056: CC056CType): Rejection =
    Rejection(
      departureId,
      DepartureBusinessRejectionType(ie056),
      ie056.FunctionalError.map(_.errorPointer)
    )

  implicit val writes: Writes[Rejection] = Json.writes[Rejection]
}
