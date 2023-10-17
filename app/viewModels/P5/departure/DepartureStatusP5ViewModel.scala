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
import models.{LocalReferenceNumber, RejectionType}
import models.RejectionType.{DeclarationRejection, InvalidationRejection}
import models.departureP5.DepartureMessageType._
import models.departureP5._
import viewModels.ViewMovementAction

case class DepartureStatusP5ViewModel(status: String, actions: Seq[ViewMovementAction])

object DepartureStatusP5ViewModel {

  def apply(movementAndMessage: MovementAndMessage)(implicit frontendAppConfig: FrontendAppConfig): DepartureStatusP5ViewModel =
    movementAndMessage match {
      case PrelodgedMovementAndMessage(departureId, localReferenceNumber, _, message, isPrelodged) =>
        preLodgeStatus(departureId, message.latestMessage.messageId, localReferenceNumber, isPrelodged).apply(message.latestMessage)
      case RejectedMovementAndMessage(departureId, _, _, message, rejectionType, isDeclarationAmendable, xPaths) =>
        rejectedStatus(departureId, message.latestMessage.messageId, rejectionType, isDeclarationAmendable, xPaths)
          .apply(message.latestMessage)
      case OtherMovementAndMessage(departureId, localReferenceNumber, _, message) =>
        currentStatus(departureId, message.latestMessage.messageId, localReferenceNumber).apply(message.latestMessage)
    }

  private def rejectedStatus(
    departureId: String,
    messageId: String,
    rejectionType: RejectionType,
    isDeclarationAmendable: Boolean,
    xPaths: Seq[String]
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] =
    Seq(
      rejectedByOfficeOfDeparture(departureId, messageId, rejectionType, isDeclarationAmendable, xPaths)
    ).reduce(_ orElse _)

  private def preLodgeStatus(departureId: String, messageId: String, localReferenceNumber: LocalReferenceNumber, isPrelodge: Boolean)(implicit
    frontendAppConfig: FrontendAppConfig
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] =
    Seq(
      declarationAmendmentAccepted(departureId, isPrelodge),
      goodsUnderControl(departureId, messageId, localReferenceNumber, isPrelodge),
      declarationSent(departureId, localReferenceNumber, isPrelodge)
    ).reduce(_ orElse _)

  private def currentStatus(departureId: String, messageId: String, localReferenceNumber: LocalReferenceNumber)(implicit
    frontendAppConfig: FrontendAppConfig
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] =
    Seq(
      departureNotification(departureId, localReferenceNumber),
      allocatedMRN(departureId, localReferenceNumber),
      cancellationRequested,
      amendmentSubmitted,
      prelodgedDeclarationSent,
      movementNotArrivedResponseSent,
      movementNotArrived,
      cancellationDecision(departureId, messageId),
      discrepancies,
      invalidMRN(),
      releasedForTransit(departureId),
      goodsNotReleased(departureId),
      guaranteeRejected(departureId, localReferenceNumber),
      incidentDuringTransit(),
      goodsBeingRecovered(departureId, messageId),
      movementEnded
    ).reduce(_ orElse _)

  private def declarationAmendmentAccepted(departureId: String, prelodged: Boolean)(implicit
    frontendAppConfig: FrontendAppConfig
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == DeclarationAmendmentAccepted =>
      val prelodgeAction = prelodged match {
        case true =>
          Seq(
            ViewMovementAction(
              s"${frontendAppConfig.presentationNotificationFrontendUrl(departureId)}",
              "movement.status.P5.action.declarationAmendmentAccepted.completeDeclaration"
            )
          )
        case false => Seq.empty
      }

      DepartureStatusP5ViewModel(
        "movement.status.P5.declarationAmendmentAccepted",
        actions = Seq(
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsUnloadingFrontend}",
            "movement.status.P5.action.declarationAmendmentAccepted.amendDeclaration"
          )
        ) ++ prelodgeAction
      )
  }

  private def allocatedMRN(
    departureId: String,
    lrn: LocalReferenceNumber
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == AllocatedMRN =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.allocatedMRN",
        actions = Seq(
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureId/index/$lrn",
            "movement.status.P5.action.allocatedMRN.cancelDeclaration"
          )
        )
      )
  }

  private def departureNotification(
    departureId: String,
    lrn: LocalReferenceNumber
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == DepartureNotification =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.departureNotificationSubmitted",
        actions = Seq(
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureId/index/$lrn",
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

  private def cancellationDecision(
    departureId: String,
    messageId: String
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == CancellationDecision =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.cancellationDecision",
        actions = Seq(
          ViewMovementAction(
            controllers.testOnly.routes.DepartureCancelledP5Controller.isDeclarationCancelled(departureId, messageId).url,
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
        actions = Nil
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

  private def goodsNotReleased(departureId: String): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == GoodsNotReleased =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.goodsNotReleased",
        actions = Seq(
          ViewMovementAction(
            controllers.testOnly.routes.GoodsNotReleasedP5Controller.goodsNotReleased(departureId, message.messageId).url,
            "movement.status.P5.action.goodsNotReleased.viewDetails"
          )
        )
      )
  }

  private def guaranteeRejected(
    departureId: String,
    lrn: LocalReferenceNumber
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == GuaranteeRejected =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.guaranteeRejected",
        actions = Seq(
          ViewMovementAction(
            controllers.testOnly.routes.GuaranteeRejectedP5Controller.onPageLoad(departureId, message.messageId, lrn).url,
            "movement.status.P5.action.guaranteeRejected.viewErrors"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureId/index/$lrn",
            "movement.status.P5.action.guaranteeRejected.cancelDeclaration"
          )
        )
      )
  }

  // scalastyle:off cyclomatic.complexity
  private def rejectedByOfficeOfDeparture(
    departureId: String,
    messageId: String,
    rejectionType: RejectionType,
    isDeclarationAmendable: Boolean,
    xPaths: Seq[String]
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {

    case message if message.messageType == RejectedByOfficeOfDeparture =>
      val (key, href) = rejectionType match {
        case DeclarationRejection if isDeclarationAmendable =>
          ("amendDeclaration", controllers.testOnly.routes.RejectionMessageP5Controller.onPageLoad(None, departureId, messageId).url)

        case DeclarationRejection if xPaths.isEmpty =>
          (errorsActionText(xPaths), controllers.testOnly.routes.DepartureDeclarationErrorsP5Controller.onPageLoad(departureId, messageId).url)

        case DeclarationRejection =>
          (errorsActionText(xPaths), controllers.testOnly.routes.ReviewDepartureErrorsP5Controller.onPageLoad(None, departureId, messageId).url)

        case InvalidationRejection if xPaths.isEmpty =>
          (errorsActionText(xPaths), controllers.testOnly.routes.CancellationNotificationErrorsP5Controller.onPageLoad(departureId, messageId).url)

        case InvalidationRejection =>
          (errorsActionText(xPaths), controllers.testOnly.routes.ReviewCancellationErrorsP5Controller.onPageLoad(None, departureId, messageId).url)

        case _ => ("", "")
      }

      val keyFormatted = if (key.isEmpty) key else s"movement.status.P5.action.rejectedByOfficeOfDeparture.$key"
      val actions      = Seq(ViewMovementAction(href, keyFormatted))
      DepartureStatusP5ViewModel(
        "movement.status.P5.rejectedByOfficeOfDeparture",
        actions
      )
  }
  // scalastyle:on cyclomatic.complexity

  private def goodsUnderControl(
    departureId: String,
    messageId: String,
    lrn: LocalReferenceNumber,
    prelodged: Boolean
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == GoodsUnderControl =>
      val prelodgeAction = prelodged match {
        case true =>
          Seq(
            ViewMovementAction(
              s"${frontendAppConfig.presentationNotificationFrontendUrl(departureId)}",
              "movement.status.P5.action.goodsUnderControl.completeDeclaration"
            )
          )
        case false => Seq.empty
      }

      DepartureStatusP5ViewModel(
        "movement.status.P5.goodsUnderControl",
        actions = Seq(
          ViewMovementAction(
            controllers.testOnly.routes.GoodsUnderControlIndexController.onPageLoad(departureId, messageId).url,
            "movement.status.P5.action.goodsUnderControl.viewDetails"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureId/index/$lrn",
            "movement.status.P5.action.goodsUnderControl.cancelDeclaration"
          )
        ) ++ prelodgeAction
      )
  }

  private def incidentDuringTransit(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == IncidentDuringTransit =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.incidentDuringTransit",
        actions = Nil
      )
  }

  private def declarationSent(
    departureId: String,
    lrn: LocalReferenceNumber,
    prelodged: Boolean
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == DeclarationSent =>
      val prelodgeAction = prelodged match {
        case true =>
          Seq(
            ViewMovementAction(
              s"${frontendAppConfig.presentationNotificationFrontendUrl(departureId)}",
              "movement.status.P5.action.declarationSent.completeDeclaration"
            )
          )
        case false => Seq.empty
      }

      DepartureStatusP5ViewModel(
        "movement.status.P5.declarationSent",
        actions = Seq(
//          ViewMovementAction(s"", "movement.status.P5.action.declarationSent.amendDeclaration"),
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureId/index/$lrn",
            "movement.status.P5.action.declarationSent.cancelDeclaration"
          )
        ) ++ prelodgeAction
      )
  }

  private def goodsBeingRecovered(departureId: String, messageId: String): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == GoodsBeingRecovered =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.goodsBeingRecovered",
        actions = Seq(
          ViewMovementAction(
            controllers.testOnly.routes.RecoveryNotificationController.onPageLoad(departureId, messageId).url,
            "movement.status.P5.action.goodsBeingRecovered.viewDetails"
          )
        )
      )
  }

  private def movementEnded: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == MovementEnded =>
      DepartureStatusP5ViewModel("movement.status.P5.movementEnded", actions = Nil)
  }

  def errorsActionText(errors: Seq[String]): String = if (errors.length == 1) {
    "viewError"
  } else {
    "viewErrors"
  }

}
