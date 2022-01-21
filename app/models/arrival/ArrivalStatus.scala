/*
 * Copyright 2022 HM Revenue & Customs
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

package models.arrival

import models.Enumerable
import play.api.libs.json.{JsError, JsString, JsSuccess, Reads}

sealed trait ArrivalStatus

object ArrivalStatus {

  case object ArrivalSubmitted extends ArrivalStatus
  case object ArrivalRejected extends ArrivalStatus
  case object UnloadingPermission extends ArrivalStatus
  case object UnloadingRemarksSubmitted extends ArrivalStatus
  case object UnloadingRemarksRejected extends ArrivalStatus
  case object GoodsReleased extends ArrivalStatus
  case object XMLSubmissionNegativeAcknowledgement extends ArrivalStatus

  case class InvalidStatus(status: String) extends ArrivalStatus {
    override def toString: String = status
  }

  val values: Seq[ArrivalStatus] =
    Seq(
      ArrivalSubmitted,
      ArrivalRejected,
      UnloadingPermission,
      UnloadingRemarksSubmitted,
      UnloadingRemarksRejected,
      GoodsReleased,
      XMLSubmissionNegativeAcknowledgement
    )

  implicit val enumerable: Enumerable[ArrivalStatus] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )

  implicit def reads(implicit ev: Enumerable[ArrivalStatus]): Reads[ArrivalStatus] =
    Reads {
      case JsString(str) =>
        ev.withName(str)
          .map(JsSuccess(_))
          .getOrElse(
            JsSuccess(InvalidStatus(s"Invalid status: $str"))
          )
      case _ =>
        JsError("error.invalid")
    }
}
