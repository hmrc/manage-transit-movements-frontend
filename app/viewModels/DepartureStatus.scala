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

import models.{Departure, ViewMovementAction}

case class DepartureStatus(status: String, actions: Seq[ViewMovementAction])

object DepartureStatus {

  def apply(departure: Departure): DepartureStatus = {
    val partialFunctions: PartialFunction[Departure, DepartureStatus] = Seq(mrnAllocated, displayStatus).reduce(_ orElse _)
    partialFunctions.apply(departure)
  }

  private def mrnAllocated: PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == "MrnAllocated" =>
      DepartureStatus("departure.status.mrnAllocated", Seq(viewHistoryAction(departure)))
  }

  private def viewHistoryAction(departure: Departure) = ViewMovementAction("", "departure.viewHistory")

  private def displayStatus(): PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == "DepartureSubmitted" =>
      DepartureStatus("departure.status.submitted", actions = Seq(viewHistoryAction(departure)))
    case departure if departure.status == "ReleasedForTransit" =>
      DepartureStatus("departure.status.releasedForTransit", actions = Seq(viewHistoryAction(departure)))
    case departure if departure.status == "TransitDeclarationRejected" =>
      DepartureStatus("departure.status.transitDeclarationRejected", actions = Seq(viewHistoryAction(departure)))
    case departure if departure.status == "DepartureDeclarationReceived" =>
      DepartureStatus("departure.status.departureDeclarationReceived", actions = Seq(viewHistoryAction(departure)))
    case departure if departure.status == "QuaranteeValidationFail" =>
      DepartureStatus("departure.status.guaranteeValidationFail", actions = Seq(viewHistoryAction(departure)))
    case departure if departure.status == "TransitDeclarationSent" =>
      DepartureStatus("departure.status.transitDeclarationSent", actions = Seq(viewHistoryAction(departure)))
    case departure => DepartureStatus(departure.status, actions = Nil)
  }
}
