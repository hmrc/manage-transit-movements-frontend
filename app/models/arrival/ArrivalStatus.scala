/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{Enumerable, WithName}
import play.api.libs.json.{JsError, JsString, JsSuccess, Reads}

sealed trait ArrivalStatus

object ArrivalStatus {

  case object ArrivalNotificationSubmitted extends WithName("IE007") with ArrivalStatus
  case object ArrivalRejection extends WithName("IE008") with ArrivalStatus
  case object UnloadingPermission extends WithName("IE043") with ArrivalStatus
  case object UnloadingRemarksSubmitted extends WithName("IE044") with ArrivalStatus
  case object UnloadingRemarksRejection extends WithName("IE058") with ArrivalStatus
  case object GoodsReleased extends WithName("IE025") with ArrivalStatus
  case object XMLSubmissionNegativeAcknowledgement extends WithName("IE917") with ArrivalStatus

  case class InvalidStatus(status: String) extends ArrivalStatus {
    override def toString: String = status
  }

  implicit val ordering: Ordering[ArrivalStatus] = (x: ArrivalStatus, y: ArrivalStatus) => {
    (x, y) match {
      case (ArrivalNotificationSubmitted, _)                                    => -1

      case (ArrivalRejection, ArrivalNotificationSubmitted)                     => 1
      case (ArrivalRejection, _)                                                => -1

      case (XMLSubmissionNegativeAcknowledgement, ArrivalNotificationSubmitted) => 1
      case (XMLSubmissionNegativeAcknowledgement, UnloadingRemarksSubmitted)    => 1
      case (XMLSubmissionNegativeAcknowledgement, _)                            => -1

      case (UnloadingPermission, ArrivalNotificationSubmitted)                  => 1
      case (UnloadingPermission, ArrivalRejection)                              => 1
      case (UnloadingPermission, XMLSubmissionNegativeAcknowledgement)          => 1
      case (UnloadingPermission, _)                                             => -1

      case (UnloadingRemarksSubmitted, ArrivalNotificationSubmitted)            => 1
      case (UnloadingRemarksSubmitted, UnloadingPermission)                     => 1
      case (UnloadingRemarksSubmitted, ArrivalRejection)                        => 1
      case (UnloadingRemarksSubmitted, _)                                       => -1

      case (UnloadingRemarksRejection, ArrivalNotificationSubmitted)            => 1
      case (UnloadingRemarksRejection, UnloadingPermission)                     => 1
      case (UnloadingRemarksRejection, UnloadingRemarksSubmitted)               => 1
      case (UnloadingRemarksRejection, ArrivalRejection)                        => 1
      case (UnloadingRemarksRejection, XMLSubmissionNegativeAcknowledgement)    => 1
      case (UnloadingRemarksRejection, _)                                       => -1

      case (GoodsReleased, _)                                                   => 1

      case (_, _)                                                              => -1
    }
  }

  val values: Seq[ArrivalStatus] =
    Seq(
      ArrivalNotificationSubmitted,
      ArrivalRejection,
      UnloadingPermission,
      UnloadingRemarksSubmitted,
      UnloadingRemarksRejection,
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
