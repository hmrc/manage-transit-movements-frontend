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

package models.departure

import models.Enumerable
import play.api.libs.json.{JsError, JsString, JsSuccess, Reads}

sealed trait DepartureStatus

object DepartureStatus {

  case object DepartureSubmitted extends DepartureStatus
  case object PositiveAcknowledgement extends DepartureStatus
  case object DepartureRejected extends DepartureStatus
  case object MrnAllocated extends DepartureStatus
  case object ReleaseForTransit extends DepartureStatus
  case object NoReleaseForTransit extends DepartureStatus
  case object ControlDecisionNotification extends DepartureStatus
  case object WriteOffNotification extends DepartureStatus
  case object GuaranteeNotValid extends DepartureStatus
  case object DeclarationCancellationRequest extends DepartureStatus
  case object CancellationDecision extends DepartureStatus
  case object DepartureSubmittedNegativeAcknowledgement extends DepartureStatus
  case object DeclarationCancellationRequestNegativeAcknowledgement extends DepartureStatus

  case class InvalidStatus(status: String) extends DepartureStatus {
    override def toString: String = status
  }

  val values: Seq[DepartureStatus] =
    Seq(
      MrnAllocated,
      DepartureSubmitted,
      PositiveAcknowledgement,
      ReleaseForTransit,
      DepartureRejected,
      GuaranteeNotValid,
      WriteOffNotification,
      DeclarationCancellationRequest,
      CancellationDecision,
      NoReleaseForTransit,
      ControlDecisionNotification,
      DepartureSubmittedNegativeAcknowledgement,
      DeclarationCancellationRequestNegativeAcknowledgement
    )

  implicit val enumerable: Enumerable[DepartureStatus] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )

  implicit def reads(implicit ev: Enumerable[DepartureStatus]): Reads[DepartureStatus] =
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
