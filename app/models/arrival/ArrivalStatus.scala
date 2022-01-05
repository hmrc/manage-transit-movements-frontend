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

import play.api.libs.json.{__, Reads}

sealed trait ArrivalStatus

object ArrivalStatus {

  case object ArrivalNotificationSubmitted extends ArrivalStatus
  case object ArrivalRejection extends ArrivalStatus
  case object UnloadingPermission extends ArrivalStatus
  case object UnloadingRemarksSubmitted extends ArrivalStatus
  case object UnloadingRemarksRejection extends ArrivalStatus
  case object GoodsReleased extends ArrivalStatus
  case object XMLSubmissionNegativeAcknowledgement extends ArrivalStatus

  case class InvalidStatus(status: String) extends ArrivalStatus {
    override def toString: String = status
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

  implicit val reads: Reads[ArrivalStatus] = __.read[String].map {
    case "ArrivalSubmitted"                     => ArrivalNotificationSubmitted
    case "ArrivalRejected"                      => ArrivalRejection
    case "UnloadingPermission"                  => UnloadingPermission
    case "UnloadingRemarksSubmitted"            => UnloadingRemarksSubmitted
    case "UnloadingRemarksRejected"             => UnloadingRemarksRejection
    case "GoodsReleased"                        => GoodsReleased
    case "XMLSubmissionNegativeAcknowledgement" => XMLSubmissionNegativeAcknowledgement
    case status                                 => InvalidStatus(status)
  }
}
