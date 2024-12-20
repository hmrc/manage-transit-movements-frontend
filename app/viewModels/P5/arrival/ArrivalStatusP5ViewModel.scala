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

package viewModels.P5.arrival

import config.FrontendAppConfig
import models.arrivalP5.*
import models.arrivalP5.ArrivalMessageType.*
import viewModels.ViewMovementAction

case class ArrivalStatusP5ViewModel(status: String, actions: Seq[ViewMovementAction])

object ArrivalStatusP5ViewModel {

  def apply(movementAndMessage: ArrivalMovementAndMessage)(implicit frontendAppConfig: FrontendAppConfig): ArrivalStatusP5ViewModel =
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
    }).getOrElse(ArrivalStatusP5ViewModel("", Seq.empty))

  private def otherStatus(arrivalId: String)(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] =
    Seq(
      arrivalNotification,
      unloadingPermission(arrivalId),
      unloadingRemarks(arrivalId),
      movementEnded
    ).reduce(_ orElse _)

  private def goodsReleasedStatus(releasedIndicator: String): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] =
    Seq(
      goodsReleased(releasedIndicator)
    ).reduce(_ orElse _)

  private def rejectedStatus(
    arrivalId: String,
    functionalErrorCount: Int,
    rejectionType: String
  ): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] =
    Seq(
      rejectionFromOfficeOfDestinationArrival(arrivalId, functionalErrorCount, rejectionType),
      rejectionFromOfficeOfDestinationUnloading(arrivalId, functionalErrorCount, rejectionType)
    ).reduce(_ orElse _)

  private def arrivalNotification(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == ArrivalNotification =>
      if (message.status.failed) {
        ArrivalStatusP5ViewModel(
          status = "movement.status.P5.arrivalNotificationFailed",
          actions = Seq(
            ViewMovementAction(
              href = frontendAppConfig.p5Arrival,
              key = "movement.status.P5.resendArrivalNotification"
            )
          )
        )
      } else {
        ArrivalStatusP5ViewModel(
          status = "movement.status.P5.arrivalNotificationSubmitted",
          actions = Nil
        )
      }
  }

  private def unloadingRemarks(
    arrivalId: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == UnloadingRemarks =>
      if (message.status.failed) {
        ArrivalStatusP5ViewModel(
          status = "movement.status.P5.unloadingRemarksFailed",
          actions = Seq(
            ViewMovementAction(
              frontendAppConfig.p5UnloadingStart(arrivalId, message.messageId),
              "movement.status.P5.action.unloadingPermission.resendUnloadingRemarks"
            )
          )
        )
      } else {
        ArrivalStatusP5ViewModel(
          status = "movement.status.P5.unloadingRemarksSubmitted",
          actions = Nil
        )
      }
  }

  private def unloadingPermission(
    arrivalId: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == UnloadingPermission =>
      ArrivalStatusP5ViewModel(
        "movement.status.P5.unloadingPermissionReceived",
        actions = Seq(
          ViewMovementAction(
            frontendAppConfig.p5UnloadingStart(arrivalId, message.messageId),
            "movement.status.P5.action.unloadingPermission.unloadingRemarks"
          ),
          ViewMovementAction(
            controllers.arrivalP5.routes.UnloadingPermissionController.getUnloadingPermissionDocument(arrivalId, message.messageId).url,
            "movement.status.P5.action.unloadingPermission.pdf"
          )
        )
      )
  }

  private def goodsReleased(releaseIndicator: String): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == GoodsReleasedNotification =>
      val message = releaseIndicator match {
        case "4" => "movement.status.P5.arrival.goodsNotReleased"
        case _   => "movement.status.P5.goodsReleased"
      }

      ArrivalStatusP5ViewModel(message, actions = Nil)
  }

  private def rejectionFromOfficeOfDestinationUnloading(
    arrivalId: String,
    functionalErrorCount: Int,
    rejectionType: String
  ): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == RejectionFromOfficeOfDestination && rejectionType == "044" =>
      val href = functionalErrorCount match {
        case 0 =>
          controllers.arrivalP5.routes.UnloadingRemarkWithoutFunctionalErrorsP5Controller.onPageLoad(arrivalId, message.messageId)
        case _ =>
          controllers.arrivalP5.routes.UnloadingRemarkWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalId, message.messageId)
      }
      ArrivalStatusP5ViewModel(
        "movement.status.P5.rejectionFromOfficeOfDestinationReceived.unloading",
        actions = Seq(
          ViewMovementAction(s"$href", s"movement.status.P5.action.${errorsActionText(functionalErrorCount)}")
        )
      )
  }

  private def rejectionFromOfficeOfDestinationArrival(
    arrivalId: String,
    functionalErrorCount: Int,
    rejectionType: String
  ): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == RejectionFromOfficeOfDestination && rejectionType == "007" =>
      val href = functionalErrorCount match {
        case 0 =>
          controllers.arrivalP5.routes.ArrivalNotificationWithoutFunctionalErrorsP5Controller.onPageLoad(arrivalId, message.messageId)
        case _ =>
          controllers.arrivalP5.routes.ArrivalNotificationWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalId, message.messageId)
      }
      ArrivalStatusP5ViewModel(
        "movement.status.P5.rejectionFromOfficeOfDestinationReceived.arrival",
        actions = Seq(
          ViewMovementAction(s"$href", s"movement.status.P5.action.${errorsActionText(functionalErrorCount)}")
        )
      )
  }

  private def movementEnded: PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == MovementEnded =>
      ArrivalStatusP5ViewModel("movement.status.P5.movementEnded", actions = Nil)
  }

  def errorsActionText(errors: Int): String = if (errors == 1) {
    "viewError"
  } else {
    "viewErrors"
  }

}
