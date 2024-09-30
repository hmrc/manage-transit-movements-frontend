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
import models.departureP5.BusinessRejectionType._
import models.departureP5.DepartureMessageType._
import models.departureP5._
import viewModels.ViewMovementAction

case class DepartureStatusP5ViewModel(status: String, actions: Seq[ViewMovementAction])

object DepartureStatusP5ViewModel {

  def apply(movementAndMessage: MovementAndMessage)(implicit frontendAppConfig: FrontendAppConfig): DepartureStatusP5ViewModel =
    (movementAndMessage match {
      case DepartureMovementAndMessage(departureId, localReferenceNumber, _, message, isPrelodged) =>
        preLodgeStatus(departureId, message.latestMessage.messageId, localReferenceNumber, isPrelodged)
          .lift(message.latestMessage)
      case RejectedMovementAndMessage(departureId, _, _, message, rejectionType, isDeclarationAmendable, xPaths) =>
        rejectedStatus(departureId, message.latestMessage.messageId, rejectionType, isDeclarationAmendable, xPaths)
          .lift(message.latestMessage)
      case IncidentMovementAndMessage(departureId, _, _, message, hasMultipleIncidents) =>
        incidentDuringTransit(departureId, message.latestMessage.messageId, hasMultipleIncidents)
          .lift(message.latestMessage)
      case OtherMovementAndMessage(departureId, localReferenceNumber, _, message) =>
        currentStatus(departureId, message.latestMessage.messageId, localReferenceNumber)
          .lift(message.latestMessage)
    }).getOrElse(DepartureStatusP5ViewModel("", Seq.empty))

  private def rejectedStatus(
    departureId: String,
    messageId: String,
    rejectionType: BusinessRejectionType,
    isDeclarationAmendable: Boolean,
    xPaths: Seq[String]
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] =
    Seq(
      rejectedByOfficeOfDeparture(departureId, messageId, rejectionType, isDeclarationAmendable, xPaths)
    ).reduce(_ orElse _)

  private def preLodgeStatus(departureId: String, messageId: String, localReferenceNumber: String, isPrelodge: Boolean)(implicit
    frontendAppConfig: FrontendAppConfig
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] =
    Seq(
      declarationAmendmentAccepted(departureId, localReferenceNumber, isPrelodge),
      goodsUnderControl(departureId, messageId, localReferenceNumber, isPrelodge),
      declarationSent(departureId, localReferenceNumber, isPrelodge)
    ).reduce(_ orElse _)

  private def currentStatus(departureId: String, messageId: String, localReferenceNumber: String)(implicit
    frontendAppConfig: FrontendAppConfig
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] =
    Seq(
      departureNotification,
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
      goodsBeingRecovered(departureId, messageId),
      movementEnded
    ).reduce(_ orElse _)

  private def declarationAmendmentAccepted(departureId: String, lrn: String, prelodged: Boolean)(implicit
    frontendAppConfig: FrontendAppConfig
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == DeclarationAmendmentAccepted =>
      val prelodgeAction = if (prelodged) {
        Seq(
          ViewMovementAction(
            s"${frontendAppConfig.presentationNotificationFrontendUrl(departureId)}",
            "movement.status.P5.action.declarationAmendmentAccepted.completeDeclaration"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.p5Cancellation}/$departureId/index/$lrn",
            "movement.status.P5.action.declarationAmendmentAccepted.cancelDeclaration"
          )
        )
      } else {
        Seq.empty
      }

      DepartureStatusP5ViewModel(
        "movement.status.P5.declarationAmendmentAccepted",
        actions = Seq(
          ViewMovementAction(
            controllers.departureP5.routes.AmendmentController.prepareForAmendment(departureId).url,
            "movement.status.P5.action.declarationAmendmentAccepted.amendDeclaration"
          )
        ) ++ prelodgeAction
      )
  }

  private def allocatedMRN(
    departureId: String,
    lrn: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == AllocatedMRN =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.allocatedMRN",
        actions = Seq(
          ViewMovementAction(
            controllers.departureP5.routes.AmendmentController.prepareForAmendment(departureId).url,
            "movement.status.P5.action.declarationAmendmentAccepted.amendDeclaration"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.p5Cancellation}/$departureId/index/$lrn",
            "movement.status.P5.action.allocatedMRN.cancelDeclaration"
          )
        )
      )
  }

  private def departureNotification: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == DepartureNotification =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.departureNotificationSubmitted",
        actions = Nil
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

  private def movementNotArrivedResponseSent: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == MovementNotArrivedResponseSent =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.movementNotArrivedResponseSent",
        actions = Nil
      )
  }

  private def movementNotArrived: PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == MovementNotArrived =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.movementNotArrived",
        actions = Nil
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
            controllers.departureP5.routes.IsDepartureCancelledP5Controller.isDeclarationCancelled(departureId, messageId).url,
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
            controllers.departureP5.routes.TransitAccompanyingDocumentController.getTAD(departureId, message.messageId).url,
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
            controllers.departureP5.routes.GoodsNotReleasedP5Controller.goodsNotReleased(departureId, message.messageId).url,
            "movement.status.P5.action.goodsNotReleased.viewDetails"
          )
        )
      )
  }

  private def guaranteeRejected(
    departureId: String,
    lrn: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == GuaranteeRejected =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.guaranteeRejected",
        actions = Seq(
          ViewMovementAction(
            controllers.departureP5.routes.GuaranteeRejectedP5Controller.onPageLoad(departureId, message.messageId).url,
            "movement.status.P5.action.guaranteeRejected.viewErrors"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.p5Cancellation}/$departureId/index/$lrn",
            "movement.status.P5.action.guaranteeRejected.cancelDeclaration"
          )
        )
      )
  }

  // scalastyle:off cyclomatic.complexity
  // scalastyle:off method.length
  private def rejectedByOfficeOfDeparture(
    departureId: String,
    messageId: String,
    rejectionType: BusinessRejectionType,
    isDeclarationAmendable: Boolean,
    xPaths: Seq[String]
  ): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {

    case message if message.messageType == RejectedByOfficeOfDeparture =>
      val (key, href) = rejectionType match {
        case DeclarationRejection | AmendmentRejection if isDeclarationAmendable =>
          ("amendDeclaration", controllers.departureP5.routes.RejectionMessageP5Controller.onPageLoad(None, departureId, messageId).url)

        case DeclarationRejection | AmendmentRejection if xPaths.isEmpty =>
          (errorsActionText(xPaths), controllers.departureP5.routes.DepartureDeclarationErrorsP5Controller.onPageLoad(departureId, messageId).url)

        case DeclarationRejection | AmendmentRejection =>
          (errorsActionText(xPaths), controllers.departureP5.routes.ReviewDepartureErrorsP5Controller.onPageLoad(None, departureId, messageId).url)

        case InvalidationRejection if xPaths.isEmpty =>
          (errorsActionText(xPaths), controllers.departureP5.routes.CancellationNotificationErrorsP5Controller.onPageLoad(departureId, messageId).url)

        case InvalidationRejection =>
          (errorsActionText(xPaths), controllers.departureP5.routes.ReviewCancellationErrorsP5Controller.onPageLoad(None, departureId, messageId).url)

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
    lrn: String,
    prelodged: Boolean
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == GoodsUnderControl =>
      val prelodgeAction = if (prelodged) {
        Seq(
          ViewMovementAction(
            s"${frontendAppConfig.presentationNotificationFrontendUrl(departureId)}",
            "movement.status.P5.action.goodsUnderControl.completeDeclaration"
          )
        )
      } else {
        Seq.empty
      }

      val goodsUnderControlAction = ViewMovementAction(
        controllers.departureP5.routes.GoodsUnderControlIndexController.onPageLoad(departureId, messageId).url,
        "movement.status.P5.action.goodsUnderControl.viewDetails"
      )

      DepartureStatusP5ViewModel(
        "movement.status.P5.goodsUnderControl",
        actions = Seq(
          goodsUnderControlAction,
          ViewMovementAction(
            s"${frontendAppConfig.p5Cancellation}/$departureId/index/$lrn",
            "movement.status.P5.action.goodsUnderControl.cancelDeclaration"
          )
        ) ++ prelodgeAction
      )
  }

  private def incidentDuringTransit(
    departureId: String,
    messageId: String,
    hasMultipleIncidents: Boolean
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == IncidentDuringTransit =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.incidentDuringTransit",
        actions = if (frontendAppConfig.isIE182Enabled) {
          Seq(
            ViewMovementAction(
              controllers.departureP5.routes.IncidentsDuringTransitP5Controller.onPageLoad(departureId, messageId).url,
              if (hasMultipleIncidents) {
                "movement.status.P5.action.incidentDuringTransit.viewIncidents"
              } else {
                "movement.status.P5.action.incidentDuringTransit.viewIncident"
              }
            )
          )
        } else {
          Seq.empty
        }
      )
  }

  private def declarationSent(
    departureId: String,
    lrn: String,
    prelodged: Boolean
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == DeclarationSent =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.declarationSent",
        actions = if (prelodged) {
          Seq(
            ViewMovementAction(
              controllers.departureP5.routes.AmendmentController.prepareForAmendment(departureId).url,
              "movement.status.P5.action.declarationAmendmentAccepted.amendDeclaration"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.p5Cancellation}/$departureId/index/$lrn",
              "movement.status.P5.action.declarationSent.cancelDeclaration"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.presentationNotificationFrontendUrl(departureId)}",
              "movement.status.P5.action.declarationSent.completeDeclaration"
            )
          )
        } else {
          Seq.empty
        }
      )
  }

  private def goodsBeingRecovered(departureId: String, messageId: String): PartialFunction[DepartureMessage, DepartureStatusP5ViewModel] = {
    case message if message.messageType == GoodsBeingRecovered =>
      DepartureStatusP5ViewModel(
        "movement.status.P5.goodsBeingRecovered",
        actions = Seq(
          ViewMovementAction(
            controllers.departureP5.routes.RecoveryNotificationController.onPageLoad(departureId, messageId).url,
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
