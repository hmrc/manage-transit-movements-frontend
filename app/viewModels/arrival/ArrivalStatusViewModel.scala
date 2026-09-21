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

package viewModels.arrival

import config.FrontendAppConfig
import models.arrival.*
import models.arrival.ArrivalMessageType.*
import viewModels.ViewMovementAction

case class ArrivalStatusViewModel(status: String, actions: Seq[ViewMovementAction])

object ArrivalStatusViewModel {

  def apply(movementAndMessage: ArrivalMovementAndMessage)(implicit frontendAppConfig: FrontendAppConfig): ArrivalStatusViewModel =
    (movementAndMessage match {
      case GoodsReleasedMovementAndMessage(_, message, indicator) =>
        goodsReleasedStatus(indicator)
          .lift(message.latestMessage)
      case RejectedMovementAndMessage(arrivalMovement, message, functionalErrorCount, businessRejectionType) =>
        rejectedStatus(arrivalMovement.arrivalId, functionalErrorCount, businessRejectionType)
          .lift(message.latestMessage)
      case OtherMovementAndMessage(arrivalMovement, message) =>
        otherStatus(arrivalMovement.arrivalId)
          .lift(message.latestMessage)
    }).getOrElse(ArrivalStatusViewModel("", Seq.empty))

  private def otherStatus(arrivalId: String)(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[ArrivalMessage, ArrivalStatusViewModel] =
    Seq(
      arrivalNotification,
      unloadingPermission(arrivalId),
      unloadingRemarks(arrivalId),
      movementEnded
    ).reduce(_ orElse _)

  private def goodsReleasedStatus(releasedIndicator: String): PartialFunction[ArrivalMessage, ArrivalStatusViewModel] =
    Seq(
      goodsReleased(releasedIndicator)
    ).reduce(_ orElse _)

  private def rejectedStatus(
    arrivalId: String,
    functionalErrorCount: Int,
    rejectionType: String
  ): PartialFunction[ArrivalMessage, ArrivalStatusViewModel] =
    Seq(
      rejectionFromOfficeOfDestinationArrival(arrivalId, functionalErrorCount, rejectionType),
      rejectionFromOfficeOfDestinationUnloading(arrivalId, functionalErrorCount, rejectionType)
    ).reduce(_ orElse _)

  private def arrivalNotification(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[ArrivalMessage, ArrivalStatusViewModel] = {
    case message if message.messageType == ArrivalNotification =>
      if (message.status.failed) {
        ArrivalStatusViewModel(
          status = "movement.status.arrivalNotificationFailed",
          actions = Seq(
            ViewMovementAction(
              href = frontendAppConfig.arrival,
              key = "movement.status.resendArrivalNotification"
            )
          )
        )
      } else {
        ArrivalStatusViewModel(
          status = "movement.status.arrivalNotificationSubmitted",
          actions = Nil
        )
      }
  }

  private def unloadingRemarks(
    arrivalId: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[ArrivalMessage, ArrivalStatusViewModel] = {
    case message if message.messageType == UnloadingRemarks =>
      if (message.status.failed) {
        ArrivalStatusViewModel(
          status = "movement.status.unloadingRemarksFailed",
          actions = Seq(
            ViewMovementAction(
              frontendAppConfig.unloadingStart(arrivalId, message.messageId),
              "movement.status.action.unloadingPermission.resendUnloadingRemarks"
            )
          )
        )
      } else {
        ArrivalStatusViewModel(
          status = "movement.status.unloadingRemarksSubmitted",
          actions = Nil
        )
      }
  }

  private def unloadingPermission(
    arrivalId: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[ArrivalMessage, ArrivalStatusViewModel] = {
    case message if message.messageType == UnloadingPermission =>
      ArrivalStatusViewModel(
        "movement.status.unloadingPermissionReceived",
        actions = Seq(
          ViewMovementAction(
            frontendAppConfig.unloadingStart(arrivalId, message.messageId),
            "movement.status.action.unloadingPermission.unloadingRemarks"
          ),
          ViewMovementAction(
            controllers.arrival.routes.UnloadingPermissionController.getUnloadingPermissionDocument(arrivalId, message.messageId).url,
            "movement.status.action.unloadingPermission.pdf"
          )
        )
      )
  }

  private def goodsReleased(releaseIndicator: String): PartialFunction[ArrivalMessage, ArrivalStatusViewModel] = {
    case message if message.messageType == GoodsReleasedNotification =>
      val message = releaseIndicator match {
        case "4" => "movement.status.arrival.goodsNotReleased"
        case _   => "movement.status.goodsReleased"
      }

      ArrivalStatusViewModel(message, actions = Nil)
  }

  private def rejectionFromOfficeOfDestinationUnloading(
    arrivalId: String,
    functionalErrorCount: Int,
    rejectionType: String
  ): PartialFunction[ArrivalMessage, ArrivalStatusViewModel] = {
    case message if message.messageType == RejectionFromOfficeOfDestination && rejectionType == "044" =>
      val href = functionalErrorCount match {
        case 0 =>
          controllers.arrival.routes.UnloadingRemarkWithoutFunctionalErrorsController.onPageLoad(arrivalId, message.messageId)
        case _ =>
          controllers.arrival.routes.UnloadingRemarkWithFunctionalErrorsController.onPageLoad(None, arrivalId, message.messageId)
      }
      ArrivalStatusViewModel(
        "movement.status.rejectionFromOfficeOfDestinationReceived.unloading",
        actions = Seq(
          ViewMovementAction(s"$href", s"movement.status.action.${errorsActionText(functionalErrorCount)}")
        )
      )
  }

  private def rejectionFromOfficeOfDestinationArrival(
    arrivalId: String,
    functionalErrorCount: Int,
    rejectionType: String
  ): PartialFunction[ArrivalMessage, ArrivalStatusViewModel] = {
    case message if message.messageType == RejectionFromOfficeOfDestination && rejectionType == "007" =>
      val href = functionalErrorCount match {
        case 0 =>
          controllers.arrival.routes.ArrivalNotificationWithoutFunctionalErrorsController.onPageLoad(arrivalId, message.messageId)
        case _ =>
          controllers.arrival.routes.ArrivalNotificationWithFunctionalErrorsController.onPageLoad(None, arrivalId, message.messageId)
      }
      ArrivalStatusViewModel(
        "movement.status.rejectionFromOfficeOfDestinationReceived.arrival",
        actions = Seq(
          ViewMovementAction(s"$href", s"movement.status.action.${errorsActionText(functionalErrorCount)}")
        )
      )
  }

  private def movementEnded: PartialFunction[ArrivalMessage, ArrivalStatusViewModel] = {
    case message if message.messageType == MovementEnded =>
      ArrivalStatusViewModel("movement.status.movementEnded", actions = Nil)
  }

  def errorsActionText(errors: Int): String = if (errors == 1) {
    "viewError"
  } else {
    "viewErrors"
  }

}
