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

package viewModels.P5

import config.FrontendAppConfig
import models.departureP5.DepartureMessageType.{AllocatedMRN, AmendmentSubmitted, CancellationDecision, CancellationRequested, DeclarationAmendmentAccepted, DeclarationSent, DepartureNotification, Discrepancies, GoodsBeingRecovered, GoodsNotReleased, GoodsUnderControl, GuaranteeRejected, GuaranteeWrittenOff, IncidentDuringTransit, InvalidMRN, MovementNotArrived, MovementNotArrivedResponseSent, PrelodgedDeclarationSent, RejectedByOfficeOfDeparture, ReleasedForTransit}
import models.departureP5.{DepartureMessageType, DepartureMovement, DepartureMovementAndMessage, MessagesForDepartureMovement}
import viewModels.ViewMovementAction

case class DepartureStatusP5ViewModel(status: String, actions: Seq[ViewMovementAction])

object DepartureStatusP5ViewModel {

  def apply(movementAndMessages: DepartureMovementAndMessage)(implicit frontendAppConfig: FrontendAppConfig): DepartureStatusP5ViewModel =
    movementAndMessages match {
      case DepartureMovementAndMessage(DepartureMovement(departureId, _, _, _), MessagesForDepartureMovement(messages)) =>
        val allPfs: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] =
          Seq(
            departureNotification,
            cancellationRequested,
            amendmentSubmitted,
            prelodgedDeclarationSent,
            movementNotArrivedResponseSent,
            movementNotArrived,
            declarationAmendmentAccepted,
            cancellationDecision,
            discrepancies,
            invalidMRN,
            allocatedMRN,
            releasedForTransit,
            goodsNotReleased,
            guaranteeRejected,
            rejectedByOfficeOfDeparture,
            goodsUnderControl,
            incidentDuringTransit,
            declarationSent,
            goodsBeingRecovered,
            guaranteeWrittenOff
          ).reduce(_ orElse _)

        allPfs.apply(messages.head.messageType)
    }

  private def departureNotification: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case DepartureNotification =>
      DepartureStatusP5ViewModel("movement.status.P5.departureNotificationSubmitted", actions = Nil)
  }

  private def cancellationRequested: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case CancellationRequested =>
      DepartureStatusP5ViewModel("movement.status.P5.cancellationSubmitted", actions = Nil)
  }

  private def amendmentSubmitted: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case AmendmentSubmitted =>
      DepartureStatusP5ViewModel("movement.status.P5.amendmentSubmitted", actions = Nil)
  }

  private def prelodgedDeclarationSent: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case PrelodgedDeclarationSent =>
      DepartureStatusP5ViewModel("movement.status.P5.prelodgedDeclarationSent", actions = Nil)
  }

  private def movementNotArrivedResponseSent: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case MovementNotArrivedResponseSent =>
      DepartureStatusP5ViewModel("movement.status.P5.movementNotArrivedResponseSent", actions = Nil)
  }

  private def movementNotArrived: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case MovementNotArrived =>
      DepartureStatusP5ViewModel("movement.status.P5.movementNotArrived", actions = Nil)
  }

  private def declarationAmendmentAccepted: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case DeclarationAmendmentAccepted =>
      DepartureStatusP5ViewModel("movement.status.P5.declarationAmendmentAccepted", actions = Nil)
  }

  private def cancellationDecision: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case CancellationDecision =>
      DepartureStatusP5ViewModel("movement.status.P5.cancellationDecision", actions = Nil)
  }

  private def discrepancies: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case Discrepancies =>
      DepartureStatusP5ViewModel("movement.status.P5.discrepancies", actions = Nil)
  }

  private def invalidMRN: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case InvalidMRN =>
      DepartureStatusP5ViewModel("movement.status.P5.invalidMRN", actions = Nil)
  }

  private def allocatedMRN: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case AllocatedMRN =>
      DepartureStatusP5ViewModel("movement.status.P5.allocatedMRN", actions = Nil)
  }

  private def releasedForTransit: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case ReleasedForTransit =>
      DepartureStatusP5ViewModel("movement.status.P5.releasedForTransit", actions = Nil)
  }

  private def goodsNotReleased: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case GoodsNotReleased =>
      DepartureStatusP5ViewModel("movement.status.P5.goodsNotReleased", actions = Nil)
  }

  private def guaranteeRejected: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case GuaranteeRejected =>
      DepartureStatusP5ViewModel("movement.status.P5.guaranteeRejected", actions = Nil)
  }

  private def rejectedByOfficeOfDeparture: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case RejectedByOfficeOfDeparture =>
      DepartureStatusP5ViewModel("movement.status.P5.rejectedByOfficeOfDeparture", actions = Nil)
  }

  private def goodsUnderControl: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case GoodsUnderControl =>
      DepartureStatusP5ViewModel("movement.status.P5.goodsUnderControl", actions = Nil)
  }

  private def incidentDuringTransit: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case IncidentDuringTransit =>
      DepartureStatusP5ViewModel("movement.status.P5.incidentDuringTransit", actions = Nil)
  }

  private def declarationSent: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case DeclarationSent =>
      DepartureStatusP5ViewModel("movement.status.P5.declarationSent", actions = Nil)
  }

  private def goodsBeingRecovered: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case GoodsBeingRecovered =>
      DepartureStatusP5ViewModel("movement.status.P5.goodsBeingRecovered", actions = Nil)
  }

  private def guaranteeWrittenOff: PartialFunction[DepartureMessageType, DepartureStatusP5ViewModel] = {
    case GuaranteeWrittenOff =>
      DepartureStatusP5ViewModel("movement.status.P5.guaranteeWrittenOff", actions = Nil)
  }

}
