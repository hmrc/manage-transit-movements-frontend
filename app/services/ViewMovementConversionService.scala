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

package services

import javax.inject.Inject
import models.{Arrival, ViewMovementAction}
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.ViewMovement
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels._

import scala.concurrent.ExecutionContext


//TODO: Is this needed? Don't think it's being used so we can remove
class ViewMovementConversionService @Inject()(implicit ec: ExecutionContext) {

  def convertToViewArrival(arrival: Arrival)(implicit hc: HeaderCarrier): ViewMovement =
    ViewMovement(
      arrival.updated.toLocalDate,
      arrival.updated.toLocalTime,
      arrival.movementReferenceNumber,
      arrival.status,
      actions(arrival.status)
    )

  private[services] def actions(status: String): Seq[ViewMovementAction] = status match {
    case "GoodsReleased"       => Seq(ViewMovementAction("history", "GoodsReleasedLink"))
    case "UnloadingPermission" => Seq(ViewMovementAction("history", "UnloadingPermissionLink"))
    case "ArrivalSubmitted"    => Seq(ViewMovementAction("history", "ArrivalSubmittedLink"))
    case "Rejection"           => Seq(ViewMovementAction("history", "RejectionLink"))
    case _                     => Nil
  }

}
