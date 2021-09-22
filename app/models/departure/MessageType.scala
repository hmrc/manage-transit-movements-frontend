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
import play.api.libs.json.{Reads, __}

sealed trait MessageType

object MessageType extends Enumerable.Implicits {

  case object MrnAllocated extends WithName("IE028") with MessageType

  case object DepartureSubmitted extends WithName("IE015") with MessageType

  case object PositiveAcknowledgement extends WithName("IE028") with MessageType

  case object ReleaseForTransit extends WithName("IE029") with MessageType

  case object DepartureRejected extends WithName("IE016") with MessageType

  //case object DepartureDeclarationReceived  extends WithName("IE028") with MessageType
  // case object GuaranteeNotValid  extends WithName("IE028") with MessageType
  // case object TransitDeclarationSent  extends WithName("IE028") with MessageType
  case object WriteOffNotification extends WithName("IE045") with MessageType

  case object DeclarationCancellationRequest extends WithName("IE014") with MessageType

  //  case object CancellationSubmitted  extends WithName("IE028") with MessageType
  // case object DepartureCancelled  extends WithName("IE028") with MessageType
  case object CancellationDecision extends WithName("IE009") with MessageType

  case object NoReleaseForTransit extends WithName("IE029") with MessageType

  case object ControlDecisionNotification extends WithName("IE060") with MessageType
  //  case object DepartureSubmittedNegativeAcknowledgement  extends WithName("IE028") with MessageType
  //  case object DeclarationCancellationRequestNegativeAcknowledgement  extends WithName("IE028") with MessageType
  // case object InvalidStatus  extends WithName("IE028") with MessageType

  val values: Seq[MessageType] =
    Seq(
      MrnAllocated,
      DepartureSubmitted,
      PositiveAcknowledgement,
      ReleaseForTransit,
      DepartureRejected,
      //      DepartureDeclarationReceived,
      //      GuaranteeNotValid,
      //      TransitDeclarationSent,
      WriteOffNotification,
      DeclarationCancellationRequest,
      //      CancellationSubmitted,
      //      DepartureCancelled,
      CancellationDecision,
      NoReleaseForTransit,
      ControlDecisionNotification
      //      DepartureSubmittedNegativeAcknowledgement,
      //      InvalidStatus
    )

   implicit val enumerable: Enumerable[MessageType] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )

}
