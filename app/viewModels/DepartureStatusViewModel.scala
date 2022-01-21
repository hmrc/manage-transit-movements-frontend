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
import controllers.departure.{routes => departureRoutes}
import models.departure.DepartureStatus._
import models.{Departure, DepartureId}

case class DepartureStatusViewModel(status: String, actions: Seq[ViewMovementAction])

object DepartureStatusViewModel {

  def apply(departure: Departure)(implicit config: FrontendAppConfig): DepartureStatusViewModel = {
    val partialFunctions: PartialFunction[Departure, DepartureStatusViewModel] =
      Seq(
        mrnAllocated,
        departureSubmitted,
        positiveAcknowledgement,
        releasedForTransit,
        departureDeclarationRejected,
        guaranteeValidationFail,
        writeOffNotification,
        declarationCancellationRequest,
        cancellationDecision,
        noReleasedForTransit,
        controlDecision,
        departureXmlNegativeAcknowledgement,
        cancellationXmlNegativeAcknowledgement,
        invalidStatus
      ).reduce(_ orElse _)
    partialFunctions.apply(departure)
  }

  private def downloadTADAction(departure: Departure) =
    ViewMovementAction(departureRoutes.AccompanyingDocumentPDFController.getPDF(departure.departureId).url, "viewDepartures.table.action.viewPDF")

  private def mrnAllocated(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == MrnAllocated =>
      DepartureStatusViewModel(
        "departure.status.mrnAllocated",
        actions =
          Seq(ViewMovementAction(config.departureFrontendConfirmCancellationUrl(departure.departureId), "viewDepartures.table.action.cancelDeclaration"))
      )
  }

  private def viewGuaranteeValidationFailAction(departureId: DepartureId)(implicit config: FrontendAppConfig) =
    ViewMovementAction(config.departureFrontendRejectedUrl(departureId), "viewDepartures.table.action.viewErrors")

  private def guaranteeValidationFail(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == GuaranteeNotValid =>
      DepartureStatusViewModel(
        "departure.status.guaranteeValidationFail",
        actions = Seq(
          viewGuaranteeValidationFailAction(departure.departureId),
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

  private def departureDeclarationRejected(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == DepartureRejected =>
      DepartureStatusViewModel(
        "departure.status.departureDeclarationRejected",
        actions = Seq(ViewMovementAction(config.departureFrontendDeclarationFailUrl(departure.departureId), "viewDepartures.table.action.viewErrors"))
      )
  }

  private def writeOffNotification: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == WriteOffNotification =>
      DepartureStatusViewModel("departure.status.writeOffNotification", actions = Nil)
  }

  private def declarationCancellationRequest: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == DeclarationCancellationRequest =>
      DepartureStatusViewModel("departure.status.declarationCancellationRequest", actions = Nil)
  }

  private def cancellationDecision(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == CancellationDecision =>
      DepartureStatusViewModel(
        "departure.status.declarationCancellationDecision",
        actions =
          Seq(ViewMovementAction(config.departureFrontendCancellationDecisionUrl(departure.departureId), "viewDepartures.table.action.viewCancellation"))
      )
  }

  private def noReleasedForTransit(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == NoReleaseForTransit =>
      DepartureStatusViewModel(
        "departure.status.noReleaseForTransit",
        actions = Seq(
          ViewMovementAction(departureRoutes.NoReleaseForTransitController.onPageLoad(departure.departureId).url, "departure.viewDetails"),
          ViewMovementAction(config.departureFrontendConfirmCancellationUrl(departure.departureId), "viewDepartures.table.action.cancelDeclaration")
        )
      )
  }

  private def controlDecision(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == ControlDecisionNotification =>
      DepartureStatusViewModel(
        "departure.status.controlDecision",
        actions = Seq(
          ViewMovementAction(departureRoutes.ControlDecisionController.onPageLoad(departure.departureId, departure.localReferenceNumber).url,
                             "departure.viewDetails"
          ),
          ViewMovementAction(config.departureFrontendConfirmCancellationUrl(departure.departureId), "viewDepartures.table.action.cancelDeclaration")
        )
      )
  }

  private def departureXmlNegativeAcknowledgement: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure
        if departure.status == XMLSubmissionNegativeAcknowledgement &&
          departure.previousStatus == Some(DepartureSubmitted) =>
      DepartureStatusViewModel(
        "departure.status.XMLSubmissionNegativeAcknowledgement",
        actions = Seq(
          ViewMovementAction(departureRoutes.DepartureXmlNegativeAcknowledgementController.onPageLoad(departure.departureId).url,
                             "viewDepartures.table.action.viewErrors"
          )
        )
      )
  }

  private def cancellationXmlNegativeAcknowledgement: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure
        if departure.status == XMLSubmissionNegativeAcknowledgement &&
          departure.previousStatus == Some(DeclarationCancellationRequest) =>
      DepartureStatusViewModel(
        "departure.status.XMLCancellationSubmissionNegativeAcknowledgement",
        actions = Seq(
          ViewMovementAction(departureRoutes.CancellationXmlNegativeAcknowledgementController.onPageLoad(departure.departureId).url,
                             "viewDepartures.table.action.viewErrors"
          )
        )
      )
  }

  private def invalidStatus: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure => DepartureStatusViewModel(departure.status.toString, actions = Nil)
  }
}
