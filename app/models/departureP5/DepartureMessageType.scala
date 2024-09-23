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

package models.departureP5

import models.{Enumerable, WithName}
import play.api.libs.json.{JsError, JsString, JsSuccess, Reads}

sealed trait DepartureMessageType

object DepartureMessageType {

  case object DepartureNotification extends WithName("IE015") with DepartureMessageType
  case object CancellationRequested extends WithName("IE014") with DepartureMessageType
  case object AmendmentSubmitted extends WithName("IE013") with DepartureMessageType
  case object PrelodgedDeclarationSent extends WithName("IE170") with DepartureMessageType
  case object MovementNotArrivedResponseSent extends WithName("IE141") with DepartureMessageType
  case object MovementNotArrived extends WithName("IE140") with DepartureMessageType
  case object DeclarationAmendmentAccepted extends WithName("IE004") with DepartureMessageType
  case object CancellationDecision extends WithName("IE009") with DepartureMessageType
  case object Discrepancies extends WithName("IE019") with DepartureMessageType
  case object InvalidMRN extends WithName("IE022") with DepartureMessageType
  case object AllocatedMRN extends WithName("IE028") with DepartureMessageType
  case object ReleasedForTransit extends WithName("IE029") with DepartureMessageType
  case object GoodsNotReleased extends WithName("IE051") with DepartureMessageType
  case object GuaranteeRejected extends WithName("IE055") with DepartureMessageType
  case object RejectedByOfficeOfDeparture extends WithName("IE056") with DepartureMessageType
  case object GoodsUnderControl extends WithName("IE060") with DepartureMessageType
  case object IncidentDuringTransit extends WithName("IE182") with DepartureMessageType
  case object DeclarationSent extends WithName("IE928") with DepartureMessageType
  case object GoodsBeingRecovered extends WithName("IE035") with DepartureMessageType
  case object MovementEnded extends WithName("IE045") with DepartureMessageType

  case class UnknownMessageType(status: String) extends WithName(status) with DepartureMessageType

  val values: Seq[DepartureMessageType] = Seq(
    DepartureNotification,
    CancellationRequested,
    AmendmentSubmitted,
    PrelodgedDeclarationSent,
    MovementNotArrivedResponseSent,
    MovementNotArrived,
    DeclarationAmendmentAccepted,
    CancellationDecision,
    Discrepancies,
    InvalidMRN,
    AllocatedMRN,
    ReleasedForTransit,
    GoodsNotReleased,
    GuaranteeRejected,
    RejectedByOfficeOfDeparture,
    GoodsUnderControl,
    IncidentDuringTransit,
    DeclarationSent,
    GoodsBeingRecovered,
    MovementEnded
  )

  implicit val enumerable: Enumerable[DepartureMessageType] =
    Enumerable(
      values
        .map(
          v => v.toString -> v
        )*
    )

  implicit val reads: Reads[DepartureMessageType] =
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
