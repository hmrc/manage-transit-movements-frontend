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
import models.arrivalP5.ArrivalMessageType._
import models.arrivalP5._
import viewModels.ViewMovementAction

case class ArrivalStatusP5ViewModel(status: String, actions: Seq[ViewMovementAction])

object ArrivalStatusP5ViewModel {

  def apply(movementAndMessages: ArrivalMovementAndMessage)(implicit frontendAppConfig: FrontendAppConfig): ArrivalStatusP5ViewModel =
    movementAndMessages match {
      case ArrivalMovementAndMessage(ArrivalMovement(arrivalId, _, _, _), MessagesForArrivalMovement(messages), functionalErrorCount) =>
        val allPfs: PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] =
          Seq(
            arrivalNotification,
            unloadingRemarks,
            unloadingPermission(arrivalId),
            goodsReleased,
            rejectionFromOfficeOfDestinationUnloading(arrivalId, messages.tail, functionalErrorCount),
            rejectionFromOfficeOfDestinationArrival(arrivalId, functionalErrorCount)
          ).reduce(_ orElse _)

        allPfs.apply(messages.head)
    }

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
            controllers.arrivalP5.routes.UnloadingPermissionController.getUnloadingPermissionDocument(message.messageId, arrivalId).url,
            "movement.status.P5.action.unloadingPermission.pdf"
          )
        )
      )
  }

  private def goodsReleased: PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == GoodsReleasedNotification =>
      ArrivalStatusP5ViewModel("movement.status.P5.goodsReleasedReceived", actions = Nil)
  }

  private def rejectionFromOfficeOfDestinationUnloading(
    arrivalId: String,
    previousMessages: Seq[ArrivalMessage],
    functionalErrorCount: Int
  ): PartialFunction[ArrivalMessage, ArrivalStatusP5ViewModel] = {
    case message if message.messageType == RejectionFromOfficeOfDestination && previousMessages.exists(_.messageType == UnloadingRemarks) =>
      val href = functionalErrorCount match {
        case 0 =>
          controllers.arrivalP5.routes.UnloadingRemarkWithoutFunctionalErrorsP5Controller.onPageLoad(arrivalId)
        case _ =>
          controllers.arrivalP5.routes.UnloadingRemarkWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalId)
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
          controllers.arrivalP5.routes.ArrivalNotificationWithoutFunctionalErrorsP5Controller.onPageLoad(arrivalId)
        case _ =>
          controllers.arrivalP5.routes.ArrivalNotificationWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalId)
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
