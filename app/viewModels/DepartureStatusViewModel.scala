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
import controllers.testOnly.{routes => testRoutes}
import models.{Departure, DepartureId, ViewMovementAction}
import models.departure.DepartureStatus._

case class DepartureStatusViewModel(status: String, actions: Seq[ViewMovementAction])

object DepartureStatusViewModel {

  def apply(departure: Departure, config: FrontendAppConfig): DepartureStatusViewModel = {
    val partialFunctions: PartialFunction[Departure, DepartureStatusViewModel] =
      Seq(
        mrnAllocated(config),
        departureSubmitted,
        positiveAcknowledgement,
        releasedForTransit,
        transitDeclarationRejected(config),
        departureDeclarationReceived,
        guaranteeValidationFail(config),
        transitDeclarationSent,
        writeOffNotification,
        cancellationSubmitted,
        departureCancelled,
        cancellationDecision,
        declarationCancellationRequest(config),
        noReleasedForTransit(config),
        controlDecision(config),
        invalidStatus
      ).reduce(_ orElse _)
    partialFunctions.apply(departure)
  }

  private def downloadTADAction(departure: Departure) =
    ViewMovementAction(testRoutes.AccompanyingDocumentPDFController.getPDF(departure.departureId).url, "viewDepartures.table.action.viewPDF")

  private def mrnAllocated(config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == MrnAllocated =>
      DepartureStatusViewModel(
        "departure.status.mrnAllocated",
        actions =
          Seq(ViewMovementAction(config.departureFrontendConfirmCancellationUrl(departure.departureId), "viewDepartures.table.action.cancelDeclaration"))
      )
  }

  private def viewGuaranteeValidationFailAction(departureId: DepartureId, config: FrontendAppConfig) =
    ViewMovementAction(config.departureFrontendRejectedUrl(departureId), "viewDepartures.table.action.viewErrors")

  private def guaranteeValidationFail(config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == GuaranteeNotValid =>
      DepartureStatusViewModel(
        "departure.status.guaranteeValidationFail",
        actions = Seq(
          viewGuaranteeValidationFailAction(departure.departureId, config),
          ViewMovementAction(config.departureFrontendConfirmCancellationUrl(departure.departureId), "viewDepartures.table.action.cancelDeclaration")
        )
      )
  }

  private def departureSubmitted: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == DepartureSubmitted =>
      DepartureStatusViewModel("departure.status.submitted", actions = Nil)
  }

  private def positiveAcknowledgement: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == PositiveAcknowledgement =>
      DepartureStatusViewModel("departure.status.positiveAcknowledgement", actions = Nil)
  }

  private def releasedForTransit: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == ReleaseForTransit =>
      DepartureStatusViewModel("departure.status.releasedForTransit", actions = Seq(downloadTADAction(departure)))
  }

  private def transitDeclarationRejected(config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == TransitDeclarationRejected =>
      DepartureStatusViewModel(
        "departure.status.transitDeclarationRejected",
        actions = Seq(ViewMovementAction(config.departureFrontendDeclarationFailUrl(departure.departureId), "viewDepartures.table.action.viewErrors"))
      )
  }

  private def departureDeclarationReceived: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == DepartureDeclarationReceived =>
      DepartureStatusViewModel("departure.status.departureDeclarationReceived", actions = Nil)
  }

  private def transitDeclarationSent: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == TransitDeclarationSent =>
      DepartureStatusViewModel("departure.status.transitDeclarationSent", actions = Nil)
  }

  private def writeOffNotification: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == WriteOffNotification =>
      DepartureStatusViewModel("departure.status.writeOffNotification", actions = Nil)
  }

  private def cancellationSubmitted: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == CancellationSubmitted =>
      DepartureStatusViewModel("departure.status.cancellationSubmitted", actions = Nil)
  }

  private def departureCancelled: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == DepartureCancelled =>
      DepartureStatusViewModel("departure.status.departureCancelled", actions = Nil)
  }

  private def cancellationDecision: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == DeclarationCancellationRequest =>
      DepartureStatusViewModel("departure.status.declarationCancellationRequest", actions = Nil)
  }

  private def declarationCancellationRequest(config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == CancellationDecision =>
      DepartureStatusViewModel(
        "departure.status.declarationCancellationRequest",
        actions =
          Seq(ViewMovementAction(config.departureFrontendCancellationDecisionUrl(departure.departureId), "viewDepartures.table.action.viewCancellation"))
      )
  }

  private def noReleasedForTransit(config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == NoReleaseForTransit =>
      DepartureStatusViewModel(
        "departure.status.noReleaseForTransit",
        actions = Seq(
          ViewMovementAction(testRoutes.NoReleaseForTransitController.onPageLoad(departure.departureId).url, "departure.viewDetails"),
          ViewMovementAction(config.departureFrontendConfirmCancellationUrl(departure.departureId), "viewDepartures.table.action.cancelDeclaration")
        )
      )
  }

  private def controlDecision(config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == ControlDecisionNotification =>
      DepartureStatusViewModel(
        "departure.status.controlDecision",
        actions = Seq(
          ViewMovementAction(testRoutes.ControlDecisionController.onPageLoad(departure.departureId, departure.localReferenceNumber).url,
                             "departure.viewDetails"),
          ViewMovementAction(config.departureFrontendConfirmCancellationUrl(departure.departureId), "viewDepartures.table.action.cancelDeclaration")
        )
      )
  }

  private def invalidStatus: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure => DepartureStatusViewModel(departure.status.toString, actions = Nil)
  }
}
