/*
 * Copyright 2020 HM Revenue & Customs
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
import models.{Arrival, ViewMovementAction}
import play.api.i18n.Messages

case class MovementStatus(status: String, actions: Seq[ViewMovementAction])

object MovementStatus {

  def apply(arrival: Arrival)(implicit messages: Messages, config: FrontendAppConfig): MovementStatus = {
    val allPfs: PartialFunction[Arrival, MovementStatus] = Seq(unloadingPermission, arrivalRejected, others).reduce(_ orElse _)

    allPfs.apply(arrival)
  }

  private def unloadingPermission()(implicit messages: Messages, config: FrontendAppConfig): PartialFunction[Arrival, MovementStatus] = {
    case arrival if arrival.status == "UnloadingPermission" =>
      MovementStatus(
        Messages("movement.status.unloadingPermission"),
        Seq(ViewMovementAction(config.declareUnloadingRemarksUrl(arrival.arrivalId), Messages("viewArrivalNotifications.table.action.unloadingRemarks")))
      )
  }

  private def arrivalRejected()(implicit messages: Messages, config: FrontendAppConfig): PartialFunction[Arrival, MovementStatus] = {
    case arrival if arrival.status == "ArrivalRejected" =>
      val action: Seq[ViewMovementAction] = if (config.arrivalRejectedLinkToggle) {
        Seq(ViewMovementAction(config.arrivalFrontendRejectedUrl(arrival.arrivalId), Messages("viewArrivalNotifications.table.action.viewErrors")))
      } else {
        Nil
      }
      MovementStatus(Messages("movement.status.arrivalRejected"), action)
  }

  private def others()(implicit messages: Messages, frontendAppConfig: FrontendAppConfig): PartialFunction[Arrival, MovementStatus] = {
    case arrival if arrival.status == "ArrivalSubmitted" => MovementStatus(Messages("movement.status.arrivalSubmitted"), actions = Nil)
    case arrival if arrival.status == "GoodsReleased"    => MovementStatus(Messages("movement.status.goodsReleased"), actions    = Nil)
    case arrival                                         => MovementStatus(arrival.status, actions                               = Nil)
  }
}
