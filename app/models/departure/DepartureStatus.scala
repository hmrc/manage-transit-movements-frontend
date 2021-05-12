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

import play.api.libs.json.{__, Reads}

sealed trait DepartureStatus

object DepartureStatus {

  case object MrnAllocated extends DepartureStatus
  case object DepartureSubmitted extends DepartureStatus
  case object PositiveAcknowledgement extends DepartureStatus
  case object ReleaseForTransit extends DepartureStatus
  case object DepartureRejected extends DepartureStatus
  case object DepartureDeclarationReceived extends DepartureStatus
  case object GuaranteeNotValid extends DepartureStatus
  case object TransitDeclarationSent extends DepartureStatus
  case object WriteOffNotification extends DepartureStatus
  case object DeclarationCancellationRequest extends DepartureStatus
  case object CancellationSubmitted extends DepartureStatus
  case object DepartureCancelled extends DepartureStatus
  case object CancellationDecision extends DepartureStatus
  case object NoReleaseForTransit extends DepartureStatus
  case object ControlDecisionNotification extends DepartureStatus
  case object XMLSubmissionNegativeAcknowledgement extends DepartureStatus
  case object InvalidStatus extends DepartureStatus

  val values: Seq[DepartureStatus] = {
    Seq(
      MrnAllocated,
      DepartureSubmitted,
      PositiveAcknowledgement,
      ReleaseForTransit,
      DepartureRejected,
      DepartureDeclarationReceived,
      GuaranteeNotValid,
      TransitDeclarationSent,
      WriteOffNotification,
      DeclarationCancellationRequest,
      CancellationSubmitted,
      DepartureCancelled,
      CancellationDecision,
      NoReleaseForTransit,
      ControlDecisionNotification,
      XMLSubmissionNegativeAcknowledgement,
      InvalidStatus
    )
  }

  implicit val reads: Reads[DepartureStatus] = __.read[String].map {
    case "MrnAllocated"                         => MrnAllocated
    case "DepartureSubmitted"                   => DepartureSubmitted
    case "PositiveAcknowledgement"              => PositiveAcknowledgement
    case "ReleaseForTransit"                    => ReleaseForTransit
    case "DepartureRejected"                    => DepartureRejected
    case "DepartureDeclarationReceived"         => DepartureDeclarationReceived
    case "GuaranteeNotValid"                    => GuaranteeNotValid
    case "TransitDeclarationSent"               => TransitDeclarationSent
    case "WriteOffNotification"                 => WriteOffNotification
    case "DeclarationCancellationRequest"       => DeclarationCancellationRequest
    case "CancellationSubmitted"                => CancellationSubmitted
    case "DepartureCancelled"                   => DepartureCancelled
    case "CancellationDecision"                 => CancellationDecision
    case "NoReleaseForTransit"                  => NoReleaseForTransit
    case "ControlDecisionNotification"          => ControlDecisionNotification
    case "XMLSubmissionNegativeAcknowledgement" => XMLSubmissionNegativeAcknowledgement
    case _                                      => InvalidStatus
  }
}
