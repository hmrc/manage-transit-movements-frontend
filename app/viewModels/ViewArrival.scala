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

package viewModels

import config.FrontendAppConfig
import models.{Arrival, RichLocalDateTime}

import java.time._

final case class ViewArrival(
  updatedDate: LocalDate,
  updatedTime: LocalTime,
  movementReferenceNumber: String,
  status: String,
  actions: Seq[ViewMovementAction],
  args: Option[String] = None
) extends ViewMovement {

  override val referenceNumber: String = movementReferenceNumber
}

object ViewArrival {

  def apply(arrival: Arrival)(implicit frontendAppConfig: FrontendAppConfig, clock: Clock): ViewArrival = {

    val arrivalStatus: ArrivalStatusViewModel = ArrivalStatusViewModel(arrival)

    val systemTime = arrival.updated.toSystemDefaultTime

    ViewArrival(
      updatedDate = systemTime.toLocalDate,
      updatedTime = systemTime.toLocalTime,
      movementReferenceNumber = arrival.movementReferenceNumber,
      status = arrivalStatus.status,
      actions = arrivalStatus.actions,
      args = None
    )
  }
}
