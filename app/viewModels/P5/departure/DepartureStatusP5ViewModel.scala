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
import models.LinkedLrn
import models.SubmissionState.Submitted
import models.departureP5.DepartureMessageType._
import models.departureP5._
import play.api.i18n.Messages
import viewModels.ViewMovementAction

case class DepartureStatusP5ViewModel(status: String, actions: Seq[ViewMovementAction], args: Option[String] = None)

object DepartureStatusP5ViewModel {

  def apply(movementAndMessages: DepartureMovementAndMessage)(implicit frontendAppConfig: FrontendAppConfig): DepartureStatusP5ViewModel =
    movementAndMessages match {
      case DepartureMovementAndMessage(DepartureMovement(departureId, _, _, _),
                                       messagesForDepartureMovements,
                                       _,
                                       isDeclarationAmendable,
                                       xPaths,
                                       reSubmittedLinkedLRN
          ) =>
        val allPfs: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] =
          Seq(
            departureNotification(departureId),
            cancellationRequested,
            amendmentSubmitted,
            prelodgedDeclarationSent,
            movementNotArrivedResponseSent,
            movementNotArrived,
            declarationAmendmentAccepted(),
            cancellationDecision(departureId),
            discrepancies,
            invalidMRN(),
            allocatedMRN(departureId),
            releasedForTransit(departureId),
            goodsNotReleased(),
            guaranteeRejected(departureId),
            rejectedByOfficeOfDeparture(departureId, messagesForDepartureMovements, isDeclarationAmendable, xPaths, reSubmittedLinkedLRN),
            goodsUnderControl(departureId),
            incidentDuringTransit(),
            declarationSent(departureId),
            goodsBeingRecovered(),
            guaranteeWrittenOff
          ).reduce(_ orElse _)

        allPfs.apply(messagesForDepartureMovements.messages.head)
    }

  private def departureNotification(
    departureId: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == DepartureNotification =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.departureNotificationSubmitted",
        actions = Seq(
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureId",
            "movement.status.P5.action.departureNotification.cancelDeclaration"
          )
        )
      )
  }

  private def cancellationRequested: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == CancellationRequested =>
      DepartureStatusP5ViewModel("movement.status.P5.cancellationSubmitted", actions = Nil)
  }

  private def amendmentSubmitted: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == AmendmentSubmitted =>
      DepartureStatusP5ViewModel("movement.status.P5.amendmentSubmitted", actions = Nil)
  }

  private def prelodgedDeclarationSent: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == PrelodgedDeclarationSent =>
      DepartureStatusP5ViewModel("movement.status.P5.prelodgedDeclarationSent", actions = Nil)
  }

  private def movementNotArrivedResponseSent(implicit
    frontendAppConfig: FrontendAppConfig
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == MovementNotArrivedResponseSent =>
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
    case message if message.messageType == MovementNotArrived =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.movementNotArrived",
        actions = Seq(
          ViewMovementAction(s"${frontendAppConfig.manageTransitMovementsUnloadingFrontend}", "movement.status.P5.action.movementNotArrived.respond")
        )
      )
  }

  private def declarationAmendmentAccepted()(implicit
    frontendAppConfig: FrontendAppConfig
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == DeclarationAmendmentAccepted =>
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

  private def cancellationDecision(departureId: String): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == CancellationDecision =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.cancellationDecision",
        actions = Seq(
          ViewMovementAction(
            controllers.testOnly.routes.DepartureCancelledP5Controller.isDeclarationCancelled(departureId).url,
            "movement.status.P5.action.cancellationDecision.viewCancellation"
          )
        )
      )
  }

  private def discrepancies: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == Discrepancies =>
      DepartureStatusP5ViewModel("movement.status.P5.discrepancies", actions = Nil)
  }

  private def invalidMRN(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == InvalidMRN =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.invalidMRN",
        actions = Seq(
          ViewMovementAction(s"", "movement.status.P5.action.invalidMRN.amendErrors")
        )
      )
  }

  private def allocatedMRN(
    departureId: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == AllocatedMRN =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.allocatedMRN",
        actions = Seq(
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureId",
            "movement.status.P5.action.allocatedMRN.cancelDeclaration"
          )
        )
      )
  }

  private def releasedForTransit(
    departureId: String
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == ReleasedForTransit =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.releasedForTransit",
        actions = Seq(
          ViewMovementAction(
            controllers.testOnly.routes.TransitAccompanyingDocumentController.getTAD(departureId, message.messageId).url,
            "movement.status.P5.action.releasedForTransit.viewAndPrintAccompanyingPDF"
          )
        )
      )
  }

  private def goodsNotReleased(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == GoodsNotReleased =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.goodsNotReleased",
        actions = Seq(
          ViewMovementAction(s"", "movement.status.P5.action.goodsNotReleased.viewDetails")
        )
      )
  }

  private def guaranteeRejected(
    departureId: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == GuaranteeRejected =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.guaranteeRejected",
        actions = Seq(
          ViewMovementAction(s"", "movement.status.P5.action.guaranteeRejected.viewErrors"),
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureId",
            "movement.status.P5.action.guaranteeRejected.cancelDeclaration"
          )
        )
      )
  }

  private def rejectedByOfficeOfDeparture(
    departureId: String,
    messagesForDepartureMovement: MessagesForDepartureMovement,
    isDeclarationAmendable: Boolean,
    xPaths: Seq[String],
    reSubmittedLinkedLRN: Option[LinkedLrn]
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {

    case message if message.messageType == RejectedByOfficeOfDeparture =>
      val (status, key, href) = movementMessageMatch(departureId, messagesForDepartureMovement, isDeclarationAmendable, xPaths, reSubmittedLinkedLRN)

      val keyFormatted = if (key.isEmpty) key else s"movement.status.P5.action.rejectedByOfficeOfDeparture.$key"
      DepartureStatusP5ViewModel(
        status,
        actions = Seq(
          ViewMovementAction(
            href,
            keyFormatted
          )
        ),
        reSubmittedLinkedLRN.flatMap(_.resubmittedLrn)
      )
  }

  private def movementMessageMatch(departureId: String,
                                   messagesForDepartureMovement: MessagesForDepartureMovement,
                                   isDeclarationAmendable: Boolean,
                                   xPaths: Seq[String],
                                   reSubmittedLinkedLRN: Option[LinkedLrn]
  )(implicit frontendAppConfig: FrontendAppConfig) =
    messagesForDepartureMovement.messageBeforeLatest.map(_.messageType) match {
      case Some(DepartureNotification) => departureNotification(departureId, isDeclarationAmendable, xPaths, reSubmittedLinkedLRN)
      case Some(CancellationRequested) => cancellationRequested(departureId, xPaths)
      case _                           => ("", "", "")
    }

  private def departureNotification(departureId: String, isDeclarationAmendable: Boolean, xPaths: Seq[String], reSubmittedLinkedLRN: Option[LinkedLrn])(implicit
    frontendAppConfig: FrontendAppConfig
  ): (String, String, String) =
    reSubmittedLinkedLRN match {
      case Some(LinkedLrn(Some(_), Some(Submitted))) => ("movement.status.P5.replacedByLRN", "", "")
      case _                                         => declarationAmendable(departureId, isDeclarationAmendable, xPaths)
    }

  private def cancellationRequested(departureId: String, xPaths: Seq[String])(implicit frontendAppConfig: FrontendAppConfig) =
    if (xPaths.isEmpty || xPaths.size > frontendAppConfig.maxErrorsForAmendableDeclaration) {
      ("movement.status.P5.rejectedByOfficeOfDeparture",
       "viewErrors",
       controllers.testOnly.routes.CancellationNotificationErrorsP5Controller.onPageLoad(departureId).url
      )
    } else {
      ("movement.status.P5.rejectedByOfficeOfDeparture",
       "viewErrors",
       controllers.testOnly.routes.ReviewCancellationErrorsP5Controller.onPageLoad(departureId).url
      )
    }

  private def declarationAmendable(departureId: String, isDeclarationAmendable: Boolean, xPaths: Seq[String])(implicit frontendAppConfig: FrontendAppConfig) =
    if (isDeclarationAmendable) {
      ("movement.status.P5.rejectedByOfficeOfDeparture",
       "amendDeclaration",
       controllers.testOnly.routes.RejectionMessageP5Controller.onPageLoad(departureId).url
      )
    } else if (xPaths.isEmpty || xPaths.size > frontendAppConfig.maxErrorsForAmendableDeclaration) {
      ("movement.status.P5.rejectedByOfficeOfDeparture",
       "viewErrors",
       controllers.testOnly.routes.DepartureDeclarationErrorsP5Controller.onPageLoad(departureId).url
      )
    } else {
      ("movement.status.P5.rejectedByOfficeOfDeparture",
       "viewErrors",
       controllers.testOnly.routes.ReviewDepartureErrorsP5Controller.onPageLoad(departureId).url
      )
    }

  private def goodsUnderControl(
    departureId: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == GoodsUnderControl =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.goodsUnderControl",
        actions = Seq(
          ViewMovementAction(
            controllers.testOnly.routes.GoodsUnderControlIndexController.onPageLoad(departureId).url,
            "movement.status.P5.action.goodsUnderControl.viewDetails"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureId",
            "movement.status.P5.action.goodsUnderControl.cancelDeclaration"
          )
        )
      )
  }

  private def incidentDuringTransit(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == IncidentDuringTransit =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.incidentDuringTransit",
        actions = Seq(
          ViewMovementAction(s"", "movement.status.P5.action.incidentDuringTransit.viewErrors")
        )
      )
  }

  private def declarationSent(
    departureId: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == DeclarationSent =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.declarationSent",
        actions = Seq(
          ViewMovementAction(s"", "movement.status.P5.action.declarationSent.amendDeclaration"),
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureId",
            "movement.status.P5.action.declarationSent.cancelDeclaration"
          )
        )
      )
  }

  private def goodsBeingRecovered(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == GoodsBeingRecovered =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.goodsBeingRecovered",
        actions = Seq(
          ViewMovementAction(s"", "movement.status.P5.action.goodsBeingRecovered.viewErrors")
        )
      )
  }

  private def guaranteeWrittenOff: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == GuaranteeWrittenOff =>
      DepartureStatusP5ViewModel("movement.status.P5.guaranteeWrittenOff", actions = Nil)
  }

}
