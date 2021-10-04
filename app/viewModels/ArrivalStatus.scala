/*
 * Copyright 2021 HM Revenue & Customs
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

case class ArrivalStatus(status: String, actions: Seq[ViewMovementAction])

object ArrivalStatus {

  def apply(arrival: Arrival)(implicit config: FrontendAppConfig): ArrivalStatus = {
    val allPfs: PartialFunction[Arrival, ArrivalStatus] =
      Seq(unloadingPermission,
          arrivalRejected,
          unloadingRemarksRejected,
          arrivalNegativeAcknowledgement,
          unloadingRemarksNegativeAcknowledgement,
          displayStatus
      ).reduce(_ orElse _)

    allPfs.apply(arrival)
  }

  private def unloadingPermission(implicit config: FrontendAppConfig): PartialFunction[Arrival, ArrivalStatus] = {
    case arrival if arrival.status == "UnloadingPermission" =>
      ArrivalStatus(
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

  private def arrivalRejected(implicit config: FrontendAppConfig): PartialFunction[Arrival, ArrivalStatus] = {
    case arrival if arrival.status == "ArrivalRejected" =>
      val action: Seq[ViewMovementAction] = Seq(
        ViewMovementAction(config.arrivalFrontendRejectedUrl(arrival.arrivalId), "viewArrivalNotifications.table.action.viewErrors")
      )
      ArrivalStatus("movement.status.arrivalRejected", action)
  }

  private def unloadingRemarksRejected(implicit config: FrontendAppConfig): PartialFunction[Arrival, ArrivalStatus] = {
    case arrival if arrival.status == "UnloadingRemarksRejected" =>
      val action: Seq[ViewMovementAction] = Seq(
        ViewMovementAction(config.unloadingRemarksRejectedUrl(arrival.arrivalId), "viewArrivalNotifications.table.action.viewErrors")
      )
      ArrivalStatus("movement.status.unloadingRemarksRejected", action)
  }

  private def arrivalNegativeAcknowledgement: PartialFunction[Arrival, ArrivalStatus] = {
    case arrival if arrival.status == "ArrivalXMLSubmissionNegativeAcknowledgement" =>
      val action: Seq[ViewMovementAction] = Seq(
        ViewMovementAction(
          controllers.arrival.routes.ArrivalXmlNegativeAcknowledgementController.onPageLoad(arrival.arrivalId).url,
          "viewArrivalNotifications.table.action.viewErrors"
        )
      )
      ArrivalStatus("movement.status.XMLSubmissionNegativeAcknowledgement", action)
  }

  private def unloadingRemarksNegativeAcknowledgement: PartialFunction[Arrival, ArrivalStatus] = {
    case arrival if arrival.status == "UnloadingRemarksXMLSubmissionNegativeAcknowledgement" =>
      val action: Seq[ViewMovementAction] = Seq(
        ViewMovementAction(
          controllers.arrival.routes.UnloadingRemarksXmlNegativeAcknowledgementController.onPageLoad(arrival.arrivalId).url,
          "viewArrivalNotifications.table.action.viewErrors"
        )
      )
      ArrivalStatus("movement.status.UnloadingRemarksXMLSubmissionNegativeAcknowledgement", action)
  }

  private def displayStatus: PartialFunction[Arrival, ArrivalStatus] = {
    case arrival if arrival.status == "ArrivalSubmitted"          => ArrivalStatus("movement.status.arrivalSubmitted", actions = Nil)
    case arrival if arrival.status == "ArrivalRejected"           => ArrivalStatus("movement.status.arrivalRejected", actions = Nil)
    case arrival if arrival.status == "UnloadingPermission"       => ArrivalStatus("movement.status.unloadingPermission", actions = Nil)
    case arrival if arrival.status == "UnloadingRemarksSubmitted" => ArrivalStatus("movement.status.unloadingRemarksSubmitted", actions = Nil)
    case arrival if arrival.status == "UnloadingRemarksRejected"  => ArrivalStatus("movement.status.unloadingRemarksRejected", actions = Nil)
    case arrival if arrival.status == "GoodsReleased"             => ArrivalStatus("movement.status.goodsReleased", actions = Nil)
    case arrival if arrival.status == "XMLSubmissionNegativeAcknowledgement" =>
      ArrivalStatus("movement.status.XMLSubmissionNegativeAcknowledgement", actions = Nil)
    case arrival => ArrivalStatus(arrival.status, actions = Nil)
  }
}
