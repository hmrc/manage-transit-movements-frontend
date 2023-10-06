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
import models.RejectionType
import models.RejectionType.{DeclarationRejection, InvalidationRejection}
import models.LocalReferenceNumber
import models.departureP5.DepartureMessageType._
import models.departureP5._
import viewModels.ViewMovementAction

case class DepartureStatusP5ViewModel(status: String, actions: Seq[ViewMovementAction])

object DepartureStatusP5ViewModel {

  def apply(movementAndMessages: DepartureMovementAndMessage)(implicit frontendAppConfig: FrontendAppConfig): DepartureStatusP5ViewModel =
    movementAndMessages match {
      case DepartureMovementAndMessage(
            DepartureMovement(
              departureId,
              _,
              localReferenceNumber,
              _,
              _
            ),
            messagesForDepartureMovements,
            _,
            rejectionType,
            isDeclarationAmendable,
            xPaths
          ) =>
        currentStatus(
          departureId,
          localReferenceNumber,
          rejectionType,
          isDeclarationAmendable,
          xPaths
        ).apply(
          messagesForDepartureMovements.messages.head
        )
    }

  private def currentStatus(departureId: String,
                            localReferenceNumber: LocalReferenceNumber,
                            rejectionType: Option[RejectionType],
                            isDeclarationAmendable: Boolean,
                            xPaths: Seq[String]
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] =
    Seq(
      departureNotification(departureId, localReferenceNumber),
      cancellationRequested,
      amendmentSubmitted,
      prelodgedDeclarationSent,
      movementNotArrivedResponseSent,
      movementNotArrived,
      declarationAmendmentAccepted(),
      cancellationDecision(departureId, localReferenceNumber),
      discrepancies,
      invalidMRN(),
      allocatedMRN(departureId, localReferenceNumber),
      releasedForTransit(departureId),
      goodsNotReleased(),
      guaranteeRejected(departureId, localReferenceNumber),
      rejectedByOfficeOfDeparture(
        departureId,
        rejectionType,
        isDeclarationAmendable,
        xPaths,
        localReferenceNumber
      ),
      goodsUnderControl(departureId, localReferenceNumber),
      incidentDuringTransit(),
      declarationSent(departureId, localReferenceNumber),
      goodsBeingRecovered(),
      movementEnded
    ).reduce(_ orElse _)

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

  private def cancellationDecision(departureId: String,
                                   localReferenceNumber: LocalReferenceNumber
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == CancellationDecision =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.cancellationDecision",
        actions = Seq(
          ViewMovementAction(
            controllers.testOnly.routes.DepartureCancelledP5Controller.isDeclarationCancelled(departureId, localReferenceNumber).url,
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
        status = "movement.status.P5.goodsNotReleased",
        actions = Nil
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
    rejectionType: Option[RejectionType],
    isDeclarationAmendable: Boolean,
    xPaths: Seq[String],
    localReferenceNumber: LocalReferenceNumber
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {

    case message if message.messageType == RejectedByOfficeOfDeparture =>
      val (key, href) = rejectionType match {
        case Some(DeclarationRejection) if isDeclarationAmendable =>
          ("amendDeclaration", controllers.testOnly.routes.RejectionMessageP5Controller.onPageLoad(None, departureId, localReferenceNumber).url)

        case Some(DeclarationRejection) if xPaths.isEmpty =>
          (errorsActionText(xPaths), controllers.testOnly.routes.DepartureDeclarationErrorsP5Controller.onPageLoad(departureId, localReferenceNumber).url)

        case Some(DeclarationRejection) =>
          (errorsActionText(xPaths), controllers.testOnly.routes.ReviewDepartureErrorsP5Controller.onPageLoad(None, departureId, localReferenceNumber).url)

        case Some(InvalidationRejection) if xPaths.isEmpty =>
          (errorsActionText(xPaths), controllers.testOnly.routes.CancellationNotificationErrorsP5Controller.onPageLoad(departureId, localReferenceNumber).url)

        case Some(InvalidationRejection) =>
          (errorsActionText(xPaths), controllers.testOnly.routes.ReviewCancellationErrorsP5Controller.onPageLoad(None, departureId, localReferenceNumber).url)

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
    lrn: LocalReferenceNumber
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
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureId/index/$lrn",
            "movement.status.P5.action.goodsUnderControl.cancelDeclaration"
          )
        )
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
    lrn: LocalReferenceNumber
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == DeclarationSent =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.declarationSent",
        actions = Seq(
//          ViewMovementAction(s"", "movement.status.P5.action.declarationSent.amendDeclaration"),
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsCancellationFrontend}/$departureId/index/$lrn",
            "movement.status.P5.action.declarationSent.cancelDeclaration"
          )
        )
      )
  }

  private def goodsBeingRecovered(): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == GoodsBeingRecovered =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.goodsBeingRecovered",
        actions = Nil
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
