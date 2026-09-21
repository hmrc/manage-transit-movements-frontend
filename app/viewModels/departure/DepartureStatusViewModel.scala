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

package viewModels.departure

import config.FrontendAppConfig
import models.departure.*
import models.departure.BusinessRejectionType.*
import models.departure.DepartureMessageType.*
import viewModels.ViewMovementAction

case class DepartureStatusViewModel(status: String, actions: Seq[ViewMovementAction])

object DepartureStatusViewModel {

  def apply(movement: MovementAndMessages)(implicit frontendAppConfig: FrontendAppConfig): DepartureStatusViewModel =
    (movement match {
      case DepartureMovementAndMessages(departureId, localReferenceNumber, _, messages, isPrelodged) =>
        preLodgeStatus(departureId, messages.latestMessage.messageId, localReferenceNumber, isPrelodged, messages)
          .lift(messages.latestMessage)
      case RejectedMovementAndMessages(departureId, _, _, messages, rejectionType, isDeclarationAmendable, xPaths) =>
        rejectedStatus(departureId, messages.latestMessage.messageId, rejectionType, isDeclarationAmendable, xPaths)
          .lift(messages.latestMessage)
      case PrelodgeRejectedMovementAndMessages(departureId, _, _, messages, xPaths) =>
        prelodgeRejectedStatus(departureId, messages.latestMessage.messageId, xPaths)
          .lift(messages.latestMessage)
      case IncidentMovementAndMessages(departureId, _, _, messages, hasMultipleIncidents) =>
        incidentDuringTransit(departureId, messages.latestMessage.messageId, hasMultipleIncidents)
          .lift(messages.latestMessage)
      case DeclarationAmendmentRejectedMovementAndMessages(departureId, _, _, messages, isRejectionAmendable, xPaths) =>
        declarationAmendmentRejectedStatus(departureId, messages.latestMessage.messageId, isRejectionAmendable, xPaths)
          .lift(messages.latestMessage)
      case OtherMovementAndMessages(departureId, localReferenceNumber, _, messages) =>
        currentStatus(departureId, messages.latestMessage.messageId, localReferenceNumber)
          .lift(messages.latestMessage)
    }).getOrElse(DepartureStatusViewModel("", Seq.empty))

  private def rejectedStatus(
    departureId: String,
    messageId: String,
    rejectionType: BusinessRejectionType,
    isDeclarationAmendable: Boolean,
    xPaths: Seq[String]
  ): PartialFunction[DepartureMessage, DepartureStatusViewModel] =
    Seq(
      rejectedByOfficeOfDeparture(departureId, messageId, rejectionType, isDeclarationAmendable, xPaths)
    ).reduce(_ orElse _)

  private def prelodgeRejectedStatus(
    departureId: String,
    messageId: String,
    xPaths: Seq[String]
  ): PartialFunction[DepartureMessage, DepartureStatusViewModel] =
    Seq(
      prelodgeRejected(departureId, messageId, xPaths)
    ).reduce(_ orElse _)

  private def preLodgeStatus(
    departureId: String,
    messageId: String,
    localReferenceNumber: String,
    isPrelodge: Boolean,
    messages: DepartureMovementMessages
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusViewModel] =
    Seq(
      declarationAmendmentAccepted(departureId, localReferenceNumber, isPrelodge),
      goodsUnderControl(departureId, messageId, localReferenceNumber, isPrelodge, messages),
      declarationSent(departureId, localReferenceNumber, isPrelodge)
    ).reduce(_ orElse _)

  private def declarationAmendmentRejectedStatus(
    departureId: String,
    messageId: String,
    isDeclarationAmendable: Boolean,
    xPaths: Seq[Option[String]]
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusViewModel] =
    Seq(
      declarationAmendmentRejected(departureId, messageId, isDeclarationAmendable, xPaths)
    ).reduce(_ orElse _)

  private def currentStatus(
    departureId: String,
    messageId: String,
    localReferenceNumber: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusViewModel] =
    Seq(
      departureNotification,
      allocatedMRN(departureId, localReferenceNumber),
      cancellationRequested(departureId, localReferenceNumber),
      amendmentSubmitted(localReferenceNumber),
      prelodgedDeclarationSent(departureId),
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
  ): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == DeclarationAmendmentAccepted =>
      val prelodgeAction = if (prelodged) {
        Seq(
          ViewMovementAction(
            s"${frontendAppConfig.presentationNotificationFrontendUrl(departureId)}",
            "movement.status.action.declarationAmendmentAccepted.completeDeclaration"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.cancellationStart(departureId, lrn)}",
            "movement.status.action.declarationAmendmentAccepted.cancelDeclaration"
          )
        )
      } else {
        Seq.empty
      }

      DepartureStatusViewModel(
        "movement.status.declarationAmendmentAccepted",
        actions = Seq(
          ViewMovementAction(
            controllers.departure.routes.AmendmentController.prepareForAmendment(departureId).url,
            "movement.status.action.declarationAmendmentAccepted.amendDeclaration"
          )
        ) ++ prelodgeAction
      )
  }

  private def allocatedMRN(
    departureId: String,
    lrn: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == AllocatedMRN =>
      DepartureStatusViewModel(
        "movement.status.allocatedMRN",
        actions = Seq(
          ViewMovementAction(
            controllers.departure.routes.AmendmentController.prepareForAmendment(departureId).url,
            "movement.status.action.declarationAmendmentAccepted.amendDeclaration"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.cancellationStart(departureId, lrn)}",
            "movement.status.action.allocatedMRN.cancelDeclaration"
          )
        )
      )
  }

  private def departureNotification(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == DepartureNotification =>
      if (message.status.failed) {
        DepartureStatusViewModel(
          status = "movement.status.departureNotificationFailed",
          actions = Seq(
            ViewMovementAction(
              href = frontendAppConfig.departure,
              key = "movement.status.resendDepartureNotification"
            )
          )
        )
      } else {
        DepartureStatusViewModel(
          status = "movement.status.departureNotificationSubmitted",
          actions = Nil
        )
      }
  }

  private def cancellationRequested(
    departureId: String,
    lrn: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == CancellationRequested =>
      if (message.status.failed) {
        DepartureStatusViewModel(
          status = "movement.status.cancellationFailed",
          actions = Seq(
            ViewMovementAction(
              frontendAppConfig.cancellationStart(departureId, lrn),
              "movement.status.resendCancellation"
            )
          )
        )
      } else {
        DepartureStatusViewModel(
          status = "movement.status.cancellationSubmitted",
          actions = Nil
        )
      }
  }

  private def amendmentSubmitted(lrn: String)(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == AmendmentSubmitted =>
      if (message.status.failed) {
        DepartureStatusViewModel(
          status = "movement.status.amendmentFailed",
          actions = Seq(
            ViewMovementAction(
              frontendAppConfig.departureFrontendTaskListUrl(lrn),
              "movement.status.resendAmendment"
            )
          )
        )
      } else {
        DepartureStatusViewModel(
          status = "movement.status.amendmentSubmitted",
          actions = Nil
        )
      }
  }

  private def prelodgedDeclarationSent(
    departureId: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == PrelodgedDeclarationSent =>
      if (message.status.failed) {
        DepartureStatusViewModel(
          status = "movement.status.prelodgedDeclarationFailed",
          actions = Seq(
            ViewMovementAction(
              href = frontendAppConfig.presentationNotificationFrontendUrl(departureId),
              key = "movement.status.resendPrelodgedDeclaration"
            )
          )
        )
      } else {
        DepartureStatusViewModel(
          status = "movement.status.prelodgedDeclarationSent",
          actions = Nil
        )
      }
  }

  private def movementNotArrivedResponseSent: PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == MovementNotArrivedResponseSent =>
      DepartureStatusViewModel(
        "movement.status.movementNotArrivedResponseSent",
        actions = Nil
      )
  }

  private def movementNotArrived: PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == MovementNotArrived =>
      DepartureStatusViewModel(
        "movement.status.movementNotArrived",
        actions = Nil
      )
  }

  private def cancellationDecision(
    departureId: String,
    messageId: String
  ): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == CancellationDecision =>
      DepartureStatusViewModel(
        "movement.status.cancellationDecision",
        actions = Seq(
          ViewMovementAction(
            controllers.departure.routes.IsDepartureCancelledController.isDeclarationCancelled(departureId, messageId).url,
            "movement.status.action.cancellationDecision.viewCancellation"
          )
        )
      )
  }

  private def discrepancies: PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == Discrepancies =>
      DepartureStatusViewModel("movement.status.discrepancies", actions = Nil)
  }

  private def invalidMRN(): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == InvalidMRN =>
      DepartureStatusViewModel(
        "movement.status.invalidMRN",
        actions = Nil
      )
  }

  private def releasedForTransit(
    departureId: String
  ): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == ReleasedForTransit =>
      DepartureStatusViewModel(
        "movement.status.releasedForTransit",
        actions = Seq(
          ViewMovementAction(
            controllers.departure.routes.TransitAccompanyingDocumentController.getTAD(departureId, message.messageId).url,
            "movement.status.action.releasedForTransit.viewAndPrintAccompanyingPDF"
          )
        )
      )
  }

  private def goodsNotReleased(departureId: String): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == GoodsNotReleased =>
      DepartureStatusViewModel(
        "movement.status.goodsNotReleased",
        actions = Seq(
          ViewMovementAction(
            controllers.departure.routes.GoodsNotReleasedController.goodsNotReleased(departureId, message.messageId).url,
            "movement.status.action.goodsNotReleased.viewDetails"
          )
        )
      )
  }

  private def guaranteeRejected(
    departureId: String,
    lrn: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == GuaranteeRejected =>
      DepartureStatusViewModel(
        "movement.status.guaranteeRejected",
        actions = Seq(
          ViewMovementAction(
            controllers.departure.routes.GuaranteeRejectedController.onPageLoad(departureId, message.messageId).url,
            "movement.status.action.guaranteeRejected.viewErrors"
          ),
          ViewMovementAction(
            s"${frontendAppConfig.cancellationStart(departureId, lrn)}",
            "movement.status.action.guaranteeRejected.cancelDeclaration"
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
  ): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {

    case message if message.messageType == RejectedByOfficeOfDeparture =>
      val (key, href) = rejectionType match {
        case DeclarationRejection | AmendmentRejection if isDeclarationAmendable =>
          ("amendDeclaration", controllers.departure.routes.RejectionMessageController.onPageLoad(None, departureId, messageId).url)

        case DeclarationRejection | AmendmentRejection if xPaths.isEmpty =>
          (errorsActionText(xPaths), controllers.departure.routes.DepartureDeclarationErrorsController.onPageLoad(departureId, messageId).url)

        case DeclarationRejection | AmendmentRejection =>
          (errorsActionText(xPaths), controllers.departure.routes.ReviewDepartureErrorsController.onPageLoad(None, departureId, messageId).url)

        case InvalidationRejection if xPaths.isEmpty =>
          (errorsActionText(xPaths), controllers.departure.routes.CancellationNotificationErrorsController.onPageLoad(departureId, messageId).url)

        case InvalidationRejection =>
          (errorsActionText(xPaths), controllers.departure.routes.ReviewCancellationErrorsController.onPageLoad(None, departureId, messageId).url)

        case _ => ("", "")
      }

      val keyFormatted = if (key.isEmpty) key else s"movement.status.action.rejectedByOfficeOfDeparture.$key"
      val actions      = Seq(ViewMovementAction(href, keyFormatted))
      DepartureStatusViewModel(
        "movement.status.rejectedByOfficeOfDeparture",
        actions
      )
  }
  // scalastyle:on cyclomatic.complexity

  private def declarationAmendmentRejected(
    departureId: String,
    messageId: String,
    isRejectionAmendable: Boolean,
    xPaths: Seq[Option[String]]
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == InvalidMRN =>
      val (key, href) = if (isRejectionAmendable) {
        ("amendDeclaration", controllers.departure.routes.DeclarationAmendmentRejectionMessageController.onPageLoad(None, departureId, messageId).url)
      } else if (xPaths.flatten.isEmpty) {
        (errorsActionText(xPaths.flatten), controllers.departure.routes.AmendDeclarationErrorsController.onPageLoad(departureId, messageId).url)
      } else {
        (errorsActionText(xPaths.flatten), controllers.departure.routes.ReviewDepartureAmendmentErrorsController.onPageLoad(None, departureId, messageId).url)
      }

      val keyFormatted = if (key.isEmpty) key else s"movement.status.action.invalidMRN.$key"
      val actions      = Seq(ViewMovementAction(href, keyFormatted))
      DepartureStatusViewModel(
        "movement.status.invalidMRN",
        if (frontendAppConfig.isIE022Enabled) actions else Seq.empty
      )
  }

  private def prelodgeRejected(
    departureId: String,
    messageId: String,
    xPaths: Seq[String]
  ): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == RejectedByOfficeOfDeparture =>
      val href = xPaths match {
        case Nil =>
          controllers.departure.routes.PreLodgedDeclarationErrorsController.onPageLoad(departureId, messageId).url
        case _ =>
          controllers.departure.routes.ReviewPrelodgedDeclarationErrorsController.onPageLoad(None, departureId, messageId).url
      }

      val key     = errorsActionText(xPaths)
      val actions = Seq(ViewMovementAction(href, s"movement.status.action.rejectedByOfficeOfDeparture.$key"))
      DepartureStatusViewModel(
        "movement.status.rejectedByOfficeOfDeparture",
        actions
      )
  }

  private def goodsUnderControl(
    departureId: String,
    messageId: String,
    lrn: String,
    prelodged: Boolean,
    messages: DepartureMovementMessages
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == GoodsUnderControl =>
      DepartureStatusViewModel(
        "movement.status.goodsUnderControl",
        actions = Seq(
          Some(
            ViewMovementAction(
              controllers.departure.routes.GoodsUnderControlIndexController.onPageLoad(departureId, messageId).url,
              "movement.status.action.goodsUnderControl.viewDetails"
            )
          ),
          Some(
            ViewMovementAction(
              s"${frontendAppConfig.cancellationStart(departureId, lrn)}",
              "movement.status.action.goodsUnderControl.cancelDeclaration"
            )
          ),
          if (prelodged && !messages.contains(PrelodgedDeclarationSent)) {
            Some(
              ViewMovementAction(
                s"${frontendAppConfig.presentationNotificationFrontendUrl(departureId)}",
                "movement.status.action.goodsUnderControl.completeDeclaration"
              )
            )
          } else {
            None
          }
        ).flatten
      )
  }

  private def incidentDuringTransit(
    departureId: String,
    messageId: String,
    hasMultipleIncidents: Boolean
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == IncidentDuringTransit =>
      DepartureStatusViewModel(
        "movement.status.incidentDuringTransit",
        actions = if (frontendAppConfig.isIE182Enabled) {
          Seq(
            ViewMovementAction(
              controllers.departure.routes.IncidentsDuringTransitController.onPageLoad(departureId, messageId).url,
              if (hasMultipleIncidents) {
                "movement.status.action.incidentDuringTransit.viewIncidents"
              } else {
                "movement.status.action.incidentDuringTransit.viewIncident"
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
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == DeclarationSent =>
      DepartureStatusViewModel(
        "movement.status.declarationSent",
        actions = if (prelodged) {
          Seq(
            ViewMovementAction(
              controllers.departure.routes.AmendmentController.prepareForAmendment(departureId).url,
              "movement.status.action.declarationAmendmentAccepted.amendDeclaration"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.cancellationStart(departureId, lrn)}",
              "movement.status.action.declarationSent.cancelDeclaration"
            ),
            ViewMovementAction(
              s"${frontendAppConfig.presentationNotificationFrontendUrl(departureId)}",
              "movement.status.action.declarationSent.completeDeclaration"
            )
          )
        } else {
          Seq.empty
        }
      )
  }

  private def goodsBeingRecovered(departureId: String, messageId: String): PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == GoodsBeingRecovered =>
      DepartureStatusViewModel(
        "movement.status.goodsBeingRecovered",
        actions = Seq(
          ViewMovementAction(
            controllers.departure.routes.RecoveryNotificationController.onPageLoad(departureId, messageId).url,
            "movement.status.action.goodsBeingRecovered.viewDetails"
          )
        )
      )
  }

  private def movementEnded: PartialFunction[DepartureMessage, DepartureStatusViewModel] = {
    case message if message.messageType == MovementEnded =>
      DepartureStatusViewModel("movement.status.movementEnded", actions = Nil)
  }

  def errorsActionText(errors: Seq[String]): String = if (errors.length == 1) {
    "viewError"
  } else {
    "viewErrors"
  }

}
