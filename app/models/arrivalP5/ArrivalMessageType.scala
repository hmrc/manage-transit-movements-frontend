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

package models.arrivalP5

import models.{Enumerable, WithName}
import play.api.libs.json.{JsError, JsString, JsSuccess, Reads}

sealed trait ArrivalMessageType

object ArrivalMessageType {

  case object ArrivalNotification extends WithName("IE007") with ArrivalMessageType
  case object UnloadingRemarks extends WithName("IE044") with ArrivalMessageType
  case object GoodsReleasedNotification extends WithName("IE025") with ArrivalMessageType
  case object UnloadingPermission extends WithName("IE043") with ArrivalMessageType
  case object RejectionFromOfficeOfDestination extends WithName("IE057") with ArrivalMessageType

  case class UnknownMessageType(status: String) extends WithName(status) with ArrivalMessageType

  val values: Seq[ArrivalMessageType] = Seq(
    ArrivalNotification,
    UnloadingRemarks,
    GoodsReleasedNotification,
    UnloadingPermission,
    RejectionFromOfficeOfDestination
  )

  implicit val enumerable: Enumerable[ArrivalMessageType] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )

  implicit val reads: Reads[ArrivalMessageType] =
    Reads {
      case JsString(str) =>
        enumerable
          .withName(str)
          .map(JsSuccess(_))
          .getOrElse(JsSuccess(UnknownMessageType(str)))
      case x =>
        JsError(s"Expected JsString but got $x")
    }
}
