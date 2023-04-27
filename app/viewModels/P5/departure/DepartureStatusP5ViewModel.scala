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

package viewModels.P5.departure

import config.FrontendAppConfig
import models.departureP5.DepartureMessageType._
import models.departureP5.{DepartureMessage, DepartureMovement, DepartureMovementAndMessage, MessagesForDepartureMovement}
import viewModels.ViewMovementAction

case class DepartureStatusP5ViewModel(status: String, actions: Seq[ViewMovementAction])

object DepartureStatusP5ViewModel {

  def apply(movementAndMessages: DepartureMovementAndMessage)(implicit frontendAppConfig: FrontendAppConfig): DepartureStatusP5ViewModel =
    movementAndMessages match {
      case DepartureMovementAndMessage(DepartureMovement(departureId, _, _, _), MessagesForDepartureMovement(messages), lrn, isDeclarationAmendable) =>
        val allPfs: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] =
          Seq(
            departureNotification,
            cancellationRequested,
            amendmentSubmitted,
            prelodgedDeclarationSent,
            movementNotArrivedResponseSent,
            movementNotArrived,
            declarationAmendmentAccepted(),
            cancellationDecision(),
            discrepancies,
            invalidMRN(),
            allocatedMRN(),
            releasedForTransit(),
            goodsNotReleased(),
            guaranteeRejected(),
            rejectedByOfficeOfDeparture(lrn, isDeclarationAmendable),
            goodsUnderControl(departureId),
            incidentDuringTransit(),
            declarationSent(),
            goodsBeingRecovered(),
            guaranteeWrittenOff
          ).reduce(_ orElse _)

        allPfs.apply(messages.head)
    }

  private def departureNotification(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, DepartureNotification, _, _) =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.departureNotificationSubmitted",
        actions = Seq(
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsUnloadingFrontend}",
            "movement.status.P5.action.departureNotification.cancelDeclaration"
          )
        )
      )
  }

  private def cancellationRequested: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, CancellationRequested, _, _) =>
      DepartureStatusP5ViewModel("movement.status.P5.cancellationSubmitted", actions = Nil)
  }

  private def amendmentSubmitted: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, AmendmentSubmitted, _, _) =>
      DepartureStatusP5ViewModel("movement.status.P5.amendmentSubmitted", actions = Nil)
  }

  private def prelodgedDeclarationSent: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, PrelodgedDeclarationSent, _, _) =>
      DepartureStatusP5ViewModel("movement.status.P5.prelodgedDeclarationSent", actions = Nil)
  }

  private def movementNotArrivedResponseSent(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, MovementNotArrivedResponseSent, _, _) =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.movementNotArrivedResponseSent",
        actions = Seq(
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsUnloadingFrontend}",
            "movement.status.P5.action.movementNotArrivedResponseSent.viewErrors"
          )
        )
      )
  }

  private def movementNotArrived(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, MovementNotArrived, _, _) =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.movementNotArrived",
        actions = Seq(
          ViewMovementAction(s"${frontendAppConfig.manageTransitMovementsUnloadingFrontend}", "movement.status.P5.action.movementNotArrived.respond")
        )
      )
  }

  private def declarationAmendmentAccepted()(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, DeclarationAmendmentAccepted, _, _) =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.declarationAmendmentAccepted",
        actions = Seq(
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsUnloadingFrontend}",
            "movement.status.P5.action.declarationAmendmentAccepted.amendDeclaration"
          )
        )
      )
  }

  private def cancellationDecision(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, CancellationDecision, _, _) =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.cancellationDecision",
        actions = Seq(
          ViewMovementAction(s"", "movement.status.P5.action.cancellationDecision.viewCancellation")
        )
      )
  }

  private def discrepancies: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, Discrepancies, _, _) =>
      DepartureStatusP5ViewModel("movement.status.P5.discrepancies", actions = Nil)
  }

  private def invalidMRN(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, InvalidMRN, _, _) =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.invalidMRN",
        actions = Seq(
          ViewMovementAction(s"", "movement.status.P5.action.invalidMRN.amendErrors")
        )
      )
  }

  private def allocatedMRN(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, AllocatedMRN, _, _) =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.allocatedMRN",
        actions = Seq(
          ViewMovementAction(s"", "movement.status.P5.action.allocatedMRN.cancelDeclaration")
        )
      )
  }

  private def releasedForTransit(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, ReleasedForTransit, _, _) =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.releasedForTransit",
        actions = Seq(
          ViewMovementAction(
            s"",
            "movement.status.P5.action.releasedForTransit.viewAndPrintAccompanyingPDF"
          )
        )
      )
  }

  private def goodsNotReleased(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, GoodsNotReleased, _, _) =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.goodsNotReleased",
        actions = Seq(
          ViewMovementAction(s"", "movement.status.P5.action.goodsNotReleased.viewDetails")
        )
      )
  }

  private def guaranteeRejected(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, GuaranteeRejected, _, _) =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.guaranteeRejected",
        actions = Seq(
          ViewMovementAction(s"", "movement.status.P5.action.guaranteeRejected.viewErrors"),
          ViewMovementAction(s"", "movement.status.P5.action.guaranteeRejected.cancelDeclaration")
        )
      )
  }

  private def rejectedByOfficeOfDeparture(
    lrn: String,
    isDeclarationAmendable: Boolean
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, RejectedByOfficeOfDeparture, _, _) =>
      val key = if (isDeclarationAmendable) "amendErrors" else "viewErrors"
      DepartureStatusP5ViewModel(
        "movement.status.P5.rejectedByOfficeOfDeparture",
        actions = Seq(
          ViewMovementAction(
            s"", // TODO
            s"movement.status.P5.action.rejectedByOfficeOfDeparture.$key"
          )
        )
      )
  }

  private def goodsUnderControl(departureId: String): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, GoodsUnderControl, _, _) =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.goodsUnderControl",
        actions = Seq(
          ViewMovementAction(
            controllers.testOnly.routes.GoodsUnderControlIndexController.onPageLoad(departureId).url,
            "movement.status.P5.action.goodsUnderControl.viewDetails"
          ),
          ViewMovementAction(s"", "movement.status.P5.action.goodsUnderControl.cancelDeclaration")
        )
      )
  }

  private def incidentDuringTransit(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, IncidentDuringTransit, _, _) =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.incidentDuringTransit",
        actions = Seq(
          ViewMovementAction(s"", "movement.status.P5.action.incidentDuringTransit.viewErrors")
        )
      )
  }

  private def declarationSent(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, DeclarationSent, _, _) =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.declarationSent",
        actions = Seq(
          ViewMovementAction(s"", "movement.status.P5.action.declarationSent.amendDeclaration"),
          ViewMovementAction(s"", "movement.status.P5.action.declarationSent.cancelDeclaration")
        )
      )
  }

  private def goodsBeingRecovered(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, GoodsBeingRecovered, _, _) =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.goodsBeingRecovered",
        actions = Seq(
          ViewMovementAction(s"", "movement.status.P5.action.goodsBeingRecovered.viewErrors")
        )
      )
  }

  private def guaranteeWrittenOff: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case DepartureMessage(_, GuaranteeWrittenOff, _, _) =>
      DepartureStatusP5ViewModel("movement.status.P5.guaranteeWrittenOff", actions = Nil)
  }

}
