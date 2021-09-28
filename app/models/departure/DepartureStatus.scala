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

  case object PositiveAcknowledgement              extends WithName("IE928") with DepartureStatus
  case object DepartureSubmitted                   extends WithName("IE015") with DepartureStatus
  case object MrnAllocated                         extends WithName("IE028") with DepartureStatus
  case object DepartureRejected                    extends WithName("IE016") with DepartureStatus
  case object ControlDecisionNotification          extends WithName("IE060") with DepartureStatus
  case object NoReleaseForTransit                  extends WithName("IE051") with DepartureStatus
  case object ReleaseForTransit                    extends WithName("IE029") with DepartureStatus
  case object DeclarationCancellationRequest       extends WithName("IE014") with DepartureStatus
  case object CancellationDecision                 extends WithName("IE009") with DepartureStatus
  case object WriteOffNotification                 extends WithName("IE045") with DepartureStatus
  case object GuaranteeNotValid                    extends WithName("IE055") with DepartureStatus
  case object XMLSubmissionNegativeAcknowledgement extends WithName("IE917") with DepartureStatus

  case object InvalidStatus                        extends DepartureStatus {
    override def toString: String = "Invalid status"
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
      InvalidStatus
    )

  implicit val enumerable: Enumerable[DepartureStatus] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )

  implicit def reads(implicit ev: Enumerable[DepartureStatus]): Reads[DepartureStatus] = {
    Reads {
      case JsString(str) =>
        ev.withName(str).map(JsSuccess(_)).getOrElse(JsSuccess(InvalidStatus))
      case _ =>
        JsError("error.invalid")
    }
  }
}
