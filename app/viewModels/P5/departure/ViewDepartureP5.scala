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

package viewModels.P5.departure

import config.FrontendAppConfig
import models.RichLocalDateTime
import models.departureP5.MovementAndMessages
import viewModels.{ViewMovement, ViewMovementAction}

import java.time.*

final case class ViewDepartureP5(
  updatedDateTime: LocalDateTime,
  referenceNumber: String,
  status: String,
  actions: Seq[ViewMovementAction]
) extends ViewMovement

object ViewDepartureP5 {

  def apply(movement: MovementAndMessages)(implicit frontendAppConfig: FrontendAppConfig, clock: Clock): ViewDepartureP5 = {

    val departureStatus: DepartureStatusP5ViewModel = DepartureStatusP5ViewModel(movement)

    new ViewDepartureP5(
      updatedDateTime = movement.updated.toSystemDefaultTime,
      referenceNumber = movement.localReferenceNumber,
      status = departureStatus.status,
      actions = departureStatus.actions
    )
  }

}
