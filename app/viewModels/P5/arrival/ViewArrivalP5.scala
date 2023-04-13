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
import models.arrivalP5.ArrivalMovementAndMessage
import viewModels.{ViewMovement, ViewMovementAction}

import java.time._

final case class ViewArrivalP5(
  updatedDate: LocalDate,
  updatedTime: LocalTime,
  movementReferenceNumber: String,
  status: String,
  actions: Seq[ViewMovementAction]
) extends ViewMovement {

  override val referenceNumber: String = movementReferenceNumber
}

object ViewArrivalP5 {

  def apply(movementAndMessage: ArrivalMovementAndMessage)(implicit frontendAppConfig: FrontendAppConfig, clock: Clock): ViewArrivalP5 = {

    val arrivalStatus: ArrivalStatusP5ViewModel = ArrivalStatusP5ViewModel(movementAndMessage)

    val systemTime = movementAndMessage.arrivalMovement.updated

    ViewArrivalP5(
      updatedDate = systemTime.toLocalDate,
      updatedTime = systemTime.toLocalTime,
      movementReferenceNumber = movementAndMessage.arrivalMovement.movementReferenceNumber,
      status = arrivalStatus.status,
      actions = arrivalStatus.actions
    )
  }
}
