/*
 * Copyright 2022 HM Revenue & Customs
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

package viewModels

import config.FrontendAppConfig
import models.Arrival
import models.arrival.ArrivalStatus._
case class ArrivalStatusViewModel(status: String, actions: Seq[ViewMovementAction])

object ArrivalStatusViewModel {

  def apply(arrival: Arrival)(implicit config: FrontendAppConfig): ArrivalStatusViewModel = {
    val allPfs: PartialFunction[Arrival, ArrivalStatusViewModel] =
      Seq(unloadingPermission,
          arrivalRejected,
          unloadingRemarksRejected,
          arrivalNegativeAcknowledgement,
          unloadingRemarksNegativeAcknowledgement,
          displayStatus
      ).reduce(_ orElse _)

    allPfs.apply(arrival)
  }

  private def unloadingPermission(implicit config: FrontendAppConfig): PartialFunction[Arrival, ArrivalStatusViewModel] = {
    case arrival if arrival.status == UnloadingPermission =>
      ArrivalStatusViewModel(
        "movement.status.unloadingPermission",
        Seq(
          ViewMovementAction(config.declareUnloadingRemarksUrl(arrival.arrivalId), "viewArrivalNotifications.table.action.unloadingRemarks"),
          ViewMovementAction(
            controllers.arrival.routes.UnloadingPermissionPDFController.getPDF(arrival.arrivalId).url,
            "viewArrivalNotifications.table.action.viewPDF"
          )
        )
      )
  }

  private def arrivalRejected(implicit config: FrontendAppConfig): PartialFunction[Arrival, ArrivalStatusViewModel] = {
    case arrival if arrival.status == ArrivalRejected =>
      val action: Seq[ViewMovementAction] = Seq(
        ViewMovementAction(config.arrivalFrontendRejectedUrl(arrival.arrivalId), "viewArrivalNotifications.table.action.viewErrors")
      )
      ArrivalStatusViewModel("movement.status.arrivalRejected", action)
  }

  private def unloadingRemarksRejected(implicit config: FrontendAppConfig): PartialFunction[Arrival, ArrivalStatusViewModel] = {
    case arrival if arrival.status == UnloadingRemarksRejected =>
      val action: Seq[ViewMovementAction] = Seq(
        ViewMovementAction(config.unloadingRemarksRejectedUrl(arrival.arrivalId), "viewArrivalNotifications.table.action.viewErrors")
      )
      ArrivalStatusViewModel("movement.status.unloadingRemarksRejected", action)
  }

  private def arrivalNegativeAcknowledgement: PartialFunction[Arrival, ArrivalStatusViewModel] = {
    case arrival if arrival.status == ArrivalSubmittedNegativeAcknowledgement =>
      val action: Seq[ViewMovementAction] = Seq(
        ViewMovementAction(
          controllers.arrival.routes.ArrivalXmlNegativeAcknowledgementController.onPageLoad(arrival.arrivalId).url,
          "viewArrivalNotifications.table.action.viewErrors"
        )
      )
      ArrivalStatusViewModel("movement.status.XMLSubmissionNegativeAcknowledgement", action)
  }

  private def unloadingRemarksNegativeAcknowledgement: PartialFunction[Arrival, ArrivalStatusViewModel] = {
    case arrival if arrival.status == UnloadingRemarksSubmittedNegativeAcknowledgement =>
      val action: Seq[ViewMovementAction] = Seq(
        ViewMovementAction(
          controllers.arrival.routes.UnloadingRemarksXmlNegativeAcknowledgementController.onPageLoad(arrival.arrivalId).url,
          "viewArrivalNotifications.table.action.viewErrors"
        )
      )
      ArrivalStatusViewModel("movement.status.UnloadingRemarksXMLSubmissionNegativeAcknowledgement", action)
  }

  private def displayStatus: PartialFunction[Arrival, ArrivalStatusViewModel] = {
    case arrival if arrival.status == ArrivalSubmitted          => ArrivalStatusViewModel("movement.status.arrivalSubmitted", actions = Nil)
    case arrival if arrival.status == ArrivalRejected           => ArrivalStatusViewModel("movement.status.arrivalRejected", actions = Nil)
    case arrival if arrival.status == UnloadingPermission       => ArrivalStatusViewModel("movement.status.unloadingPermission", actions = Nil)
    case arrival if arrival.status == UnloadingRemarksSubmitted => ArrivalStatusViewModel("movement.status.unloadingRemarksSubmitted", actions = Nil)
    case arrival if arrival.status == UnloadingRemarksRejected  => ArrivalStatusViewModel("movement.status.unloadingRemarksRejected", actions = Nil)
    case arrival if arrival.status == GoodsReleased             => ArrivalStatusViewModel("movement.status.goodsReleased", actions = Nil)
    case arrival if arrival.status == XMLSubmissionNegativeAcknowledgement =>
      ArrivalStatusViewModel("movement.status.XMLSubmissionNegativeAcknowledgement", actions = Nil)
    case arrival => ArrivalStatusViewModel(arrival.status.toString, actions = Nil)
  }
}
