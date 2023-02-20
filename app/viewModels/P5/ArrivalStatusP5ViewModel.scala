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

package viewModels.P5

import models.arrivalP5.ArrivalMessageType._
import models.arrivalP5.{ArrivalMessageType, Message}
import viewModels.ViewMovementAction

case class ArrivalStatusP5ViewModel(status: String, actions: Seq[ViewMovementAction])

object ArrivalStatusP5ViewModel {

  def apply(message: Message): ArrivalStatusP5ViewModel = {
    val allPfs: PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] =
      Seq(
        arrivalNotification,
        unloadingRemarks,
        unloadingPermission,
        goodsReleased,
        rejectionFromOfficeOfDestination,
        functionalNack,
        xmlNack
      ).reduce(_ orElse _)

    allPfs.apply(message.messageType)
  }

  private def arrivalNotification: PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] = {
    case ArrivalNotification =>
      ArrivalStatusP5ViewModel("movement.status.P5.arrivalNotificationSubmitted", actions = Nil)
  }

  private def unloadingRemarks: PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] = {
    case UnloadingRemarks =>
      ArrivalStatusP5ViewModel("movement.status.P5.unloadingRemarksSubmitted", actions = Nil)
  }

  private def unloadingPermission: PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] = {
    case UnloadingPermission =>
      ArrivalStatusP5ViewModel(
        "movement.status.P5.unloadingPermissionReceived",
        actions = Seq(
          ViewMovementAction("#", "movement.status.P5.action.unloadingPermission.unloadingRemarks"),
          ViewMovementAction("#", "movement.status.P5.action.unloadingPermission.pdf")
        )
      )
  }

  private def goodsReleased: PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] = {
    case GoodsReleasedNotification =>
      ArrivalStatusP5ViewModel("movement.status.P5.goodsReleasedReceived", actions = Nil)
  }

  private def rejectionFromOfficeOfDestination: PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] = {
    case RejectionFromOfficeOfDestination =>
      ArrivalStatusP5ViewModel(
        "movement.status.P5.rejectionFromOfficeOfDestinationReceived",
        actions = Seq(
          ViewMovementAction("#", "movement.status.P5.action.viewError")
        )
      )
  }

  private def functionalNack: PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] = {
    case FunctionalNack =>
      ArrivalStatusP5ViewModel("movement.status.P5.functionalNackReceived",
                               actions = Seq(
                                 ViewMovementAction("#", "movement.status.P5.action.viewError")
                               )
      )
  }

  private def xmlNack: PartialFunction[ArrivalMessageType, ArrivalStatusP5ViewModel] = {
    case XmlNack =>
      ArrivalStatusP5ViewModel("movement.status.P5.xmlNackReceived",
                               actions = Seq(
                                 ViewMovementAction("#", "movement.status.P5.action.viewError")
                               )
      )
  }

}
