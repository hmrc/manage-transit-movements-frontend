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
import models.ArrivalId
import models.arrivalP5.ArrivalMessageType._
import models.arrivalP5._
import viewModels.ViewMovementAction

case class ArrivalStatusP5ViewModel(status: String, actions: Seq[ViewMovementAction])

object ArrivalStatusP5ViewModel {

  def apply(movementAndMessage: ArrivalMovementAndMessage)(implicit frontendAppConfig: FrontendAppConfig): ArrivalStatusP5ViewModel =
    movementAndMessage match {
      case GoodsReleasedMovementAndMessage(_, message, indicator) => goodsReleasedStatus(indicator).apply(message.latestMessage)
      case RejectedMovementAndMessage(arrivalMovement, message, functionalErrorCount) =>
        rejectedStatus(arrivalMovement.arrivalId, functionalErrorCount).apply(message.latestMessage)
      case OtherMovementAndMessage(arrivalMovement, message) => otherStatus(arrivalMovement.arrivalId).apply(message.latestMessage)
    }

  def otherStatus(arrivalId: String)(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] =
    Seq(
      arrivalNotification,
      unloadingRemarks,
      unloadingPermission(arrivalId)
    ).reduce(_ orElse _)

  private def goodsReleasedStatus(releasedIndicator: String): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] =
    Seq(
      goodsReleased(releasedIndicator)
    ).reduce(_ orElse _)

  private def rejectedStatus(
    arrivalId: String,
    functionalErrorCount: Int
  ): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] =
    Seq(
      rejectionFromOfficeOfDestinationArrival(arrivalId, functionalErrorCount)
    ).reduce(_ orElse _)

  private def arrivalNotification: PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == ArrivalNotification =>
      ArrivalStatusP5ViewModel("movement.status.P5.arrivalNotificationSubmitted", actions = Nil)
  }

  private def unloadingRemarks: PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == UnloadingRemarks =>
      ArrivalStatusP5ViewModel("movement.status.P5.unloadingRemarksSubmitted", actions = Nil)
  }

  private def unloadingPermission(
    arrivalId: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == UnloadingPermission =>
      ArrivalStatusP5ViewModel(
        "movement.status.P5.unloadingPermissionReceived",
        actions = Seq(
          ViewMovementAction(
            s"${frontendAppConfig.manageTransitMovementsUnloadingFrontend}/$arrivalId/unloading-remarks/${message.messageId}",
            "movement.status.P5.action.unloadingPermission.unloadingRemarks"
          ),
          ViewMovementAction(
            controllers.testOnly.routes.UnloadingPermissionController.getUnloadingPermissionDocument(message.messageId, arrivalId).url,
            "movement.status.P5.action.unloadingPermission.pdf"
          )
        )
      )
  }

  private def goodsReleased(releaseIndicator: String): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == GoodsReleasedNotification =>
      val message = releaseIndicator match {
        case "4" => "movement.status.P5.goodsNotReleased"
        case _   => "movement.status.P5.goodsReleasedReceived"
      }

      ArrivalStatusP5ViewModel(message, actions = Nil)
  }

  private def rejectionFromOfficeOfDestinationUnloading(
    arrivalId: String,
    previousMessages: Seq[ArrivalMessage],
    functionalErrorCount: Int
  ): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == RejectionFromOfficeOfDestination && previousMessages.exists(_.messageType == UnloadingRemarks) =>
      val href = functionalErrorCount match {
        case 0 =>
          controllers.testOnly.routes.UnloadingRemarkWithoutFunctionalErrorsP5Controller.onPageLoad(arrivalId)
        case _ =>
          controllers.testOnly.routes.UnloadingRemarkWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalId)
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
    functionalErrorCount: Int
  ): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == RejectionFromOfficeOfDestination =>
      val href = functionalErrorCount match {
        case 0 =>
          controllers.testOnly.routes.ArrivalNotificationWithoutFunctionalErrorsP5Controller.onPageLoad(arrivalId)
        case _ =>
          controllers.testOnly.routes.ArrivalNotificationWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalId)
      }
      ArrivalStatusP5ViewModel(
        "movement.status.P5.rejectionFromOfficeOfDestinationReceived.arrival",
        actions = Seq(
          ViewMovementAction(s"$href", s"movement.status.P5.action.${errorsActionText(functionalErrorCount)}")
        )
      )
  }

  def errorsActionText(errors: Int): String = if (errors == 1) {
    "viewError"
  } else {
    "viewErrors"
  }

}
