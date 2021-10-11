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

package models.departure

import models.{Enumerable, WithName}
import play.api.libs.json.{JsError, JsString, JsSuccess, Reads}

sealed trait DepartureStatus

object DepartureStatus {

  case object DepartureSubmitted extends WithName("IE015") with DepartureStatus
  case object PositiveAcknowledgement extends WithName("IE928") with DepartureStatus
  case object DepartureRejected extends WithName("IE016") with DepartureStatus
  case object MrnAllocated extends WithName("IE028") with DepartureStatus
  case object ReleaseForTransit extends WithName("IE029") with DepartureStatus
  case object NoReleaseForTransit extends WithName("IE051") with DepartureStatus
  case object ControlDecisionNotification extends WithName("IE060") with DepartureStatus
  case object GuaranteeNotValid extends WithName("IE055") with DepartureStatus
  case object WriteOffNotification extends WithName("IE045") with DepartureStatus
  case object DeclarationCancellationRequest extends WithName("IE014") with DepartureStatus
  case object CancellationDecision extends WithName("IE009") with DepartureStatus
  case object XMLSubmissionNegativeAcknowledgement extends WithName("IE917") with DepartureStatus

  case class InvalidStatus(status: String) extends DepartureStatus {
    override def toString: String = status
  }

  implicit val ordering: Ordering[DepartureStatus] = (x: DepartureStatus, y: DepartureStatus) => {
    (x, y) match {
      case (DepartureSubmitted, _) => -1

      case (PositiveAcknowledgement, DepartureSubmitted) => 1
      case (PositiveAcknowledgement, _)                  => -1

      case (DepartureRejected, DepartureSubmitted)      => 1
      case (DepartureRejected, PositiveAcknowledgement) => 1
      case (DepartureRejected, _)                       => -1

      case (MrnAllocated, DepartureSubmitted)      => 1
      case (MrnAllocated, PositiveAcknowledgement) => 1
      case (MrnAllocated, DepartureRejected)       => 1
      case (MrnAllocated, _)                       => -1

      case (GuaranteeNotValid, DepartureSubmitted)      => 1
      case (GuaranteeNotValid, PositiveAcknowledgement) => 1
      case (GuaranteeNotValid, DepartureRejected)       => 1
      case (GuaranteeNotValid, MrnAllocated)            => 1
      case (GuaranteeNotValid, _)                       => -1

      case (ControlDecisionNotification, DepartureSubmitted)      => 1
      case (ControlDecisionNotification, PositiveAcknowledgement) => 1
      case (ControlDecisionNotification, DepartureRejected)       => 1
      case (ControlDecisionNotification, MrnAllocated)            => 1
      case (ControlDecisionNotification, GuaranteeNotValid)       => 1
      case (ControlDecisionNotification, _)                       => -1

      case (NoReleaseForTransit, DepartureSubmitted)          => 1
      case (NoReleaseForTransit, PositiveAcknowledgement)     => 1
      case (NoReleaseForTransit, DepartureRejected)           => 1
      case (NoReleaseForTransit, MrnAllocated)                => 1
      case (NoReleaseForTransit, ControlDecisionNotification) => 1
      case (NoReleaseForTransit, GuaranteeNotValid)           => 1
      case (NoReleaseForTransit, _)                           => -1

      case (ReleaseForTransit, DepartureSubmitted)          => 1
      case (ReleaseForTransit, PositiveAcknowledgement)     => 1
      case (ReleaseForTransit, DepartureRejected)           => 1
      case (ReleaseForTransit, MrnAllocated)                => 1
      case (ReleaseForTransit, ControlDecisionNotification) => 1
      case (ReleaseForTransit, GuaranteeNotValid)           => 1
      case (ReleaseForTransit, NoReleaseForTransit)         => 1
      case (ReleaseForTransit, _)                           => -1

      case (DeclarationCancellationRequest, DepartureSubmitted)          => 1
      case (DeclarationCancellationRequest, PositiveAcknowledgement)     => 1
      case (DeclarationCancellationRequest, DepartureRejected)           => 1
      case (DeclarationCancellationRequest, MrnAllocated)                => 1
      case (DeclarationCancellationRequest, ControlDecisionNotification) => 1
      case (DeclarationCancellationRequest, GuaranteeNotValid)           => 1
      case (DeclarationCancellationRequest, NoReleaseForTransit)         => 1
      case (DeclarationCancellationRequest, ReleaseForTransit)           => 1
      case (DeclarationCancellationRequest, _)                           => -1

      case (CancellationDecision, DepartureSubmitted)             => 1
      case (CancellationDecision, PositiveAcknowledgement)        => 1
      case (CancellationDecision, DepartureRejected)              => 1
      case (CancellationDecision, MrnAllocated)                   => 1
      case (CancellationDecision, ControlDecisionNotification)    => 1
      case (CancellationDecision, GuaranteeNotValid)              => 1
      case (CancellationDecision, NoReleaseForTransit)            => 1
      case (CancellationDecision, ReleaseForTransit)              => 1
      case (CancellationDecision, DeclarationCancellationRequest) => 1
      case (CancellationDecision, _)                              => -1

      case (XMLSubmissionNegativeAcknowledgement, DepartureSubmitted)             => 1
      case (XMLSubmissionNegativeAcknowledgement, DeclarationCancellationRequest) => 1
      case (XMLSubmissionNegativeAcknowledgement, _)                              => -1

      case (WriteOffNotification, _) => 1

      case (_, _) => -1
    }
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
      XMLSubmissionNegativeAcknowledgement
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
