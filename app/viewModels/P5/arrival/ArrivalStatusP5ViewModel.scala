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
        val allPfs: PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] =
          Seq(
            arrivalNotification,
            unloadingRemarks,
            unloadingPermission(arrivalId),
            goodsReleased,
            rejectionFromOfficeOfDestinationUnloading(arrivalId, messages.tail, functionalErrorCount),
            rejectionFromOfficeOfDestinationArrival(arrivalId, functionalErrorCount)
          ).reduce(_ orElse _)

        allPfs.apply(messages.head.messageType)
    }

  private def arrivalNotification: PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] = {
    case ArrivalNotification =>
      ArrivalStatusP5ViewModel("movement.status.P5.arrivalNotificationSubmitted", actions = Nil)
  }

  private def unloadingRemarks: PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] = {
    case UnloadingRemarks =>
      ArrivalStatusP5ViewModel("movement.status.P5.unloadingRemarksSubmitted", actions = Nil)
  }

  private def unloadingPermission(
    arrivalId: String
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] = {
    case UnloadingPermission =>
      ArrivalStatusP5ViewModel(
        "movement.status.P5.unloadingPermissionReceived",
        actions = Seq(
          ViewMovementAction(s"${frontendAppConfig.manageTransitMovementsUnloadingFrontend}/$arrivalId",
                             "movement.status.P5.action.unloadingPermission.unloadingRemarks"
          ),
          ViewMovementAction("#", "movement.status.P5.action.unloadingPermission.pdf")
        )
      )
  }

  private def goodsReleased: PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] = {
    case GoodsReleasedNotification =>
      ArrivalStatusP5ViewModel("movement.status.P5.goodsReleasedReceived", actions = Nil)
  }

  private def rejectionFromOfficeOfDestinationUnloading(
    arrivalId: String,
    previousMessages: Seq[ArrivalMessage],
    functionalErrorCount: Int
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] = {
    case RejectionFromOfficeOfDestination if previousMessages.exists(_.messageType == UnloadingRemarks) =>
      val href = functionalErrorCount match {
        case errors if errors == 0 || errors > frontendAppConfig.maxErrorsForArrivalNotification =>
          controllers.testOnly.routes.UnloadingRemarkErrorsP5Controller.onPageLoad(arrivalId)
        case errors if errors > 0 && errors <= frontendAppConfig.maxErrorsForArrivalNotification =>
          controllers.testOnly.routes.ReviewUnloadingRemarkErrorsP5Controller.onPageLoad(arrivalId)
      }
      ArrivalStatusP5ViewModel(
        "movement.status.P5.rejectionFromOfficeOfDestinationReceived.unloading",
        actions = Seq(
          ViewMovementAction(s"$href", "movement.status.P5.action.viewError")
        )
      )
  }

  //TODO: If count = 0 or > 10 go to CTCP-2918, if count <= 10 goto CTCP-2917
  private def rejectionFromOfficeOfDestinationArrival(
    arrivalId: String,
    functionalErrorCount: Int
  )(implicit frontendAppConfig: FrontendAppConfig): PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] = {
    case RejectionFromOfficeOfDestination =>
      val href = functionalErrorCount match {
        case errors if errors == 0 || errors > frontendAppConfig.maxErrorsForArrivalNotification =>
          controllers.testOnly.routes.ArrivalNotificationErrorP5Controller.onPageLoad(arrivalId)
        case errors if errors > 0 && errors <= frontendAppConfig.maxErrorsForArrivalNotification => ??? //CTCP-2917
      }
      ArrivalStatusP5ViewModel(
        "movement.status.P5.rejectionFromOfficeOfDestinationReceived.arrival",
        actions = Seq(
          ViewMovementAction(s"$href", "movement.status.P5.action.viewError")
        )
      )
  }

}
