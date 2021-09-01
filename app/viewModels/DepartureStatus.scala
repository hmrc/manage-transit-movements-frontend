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
import controllers.departure.{routes => departureRoutes}
import models.departure.DepartureStatus._
import models.{Departure, DepartureId}

case class DepartureStatus(status: String, actions: Seq[ViewMovementAction])

object DepartureStatus {

  def apply(departure: Departure)(implicit config: FrontendAppConfig): DepartureStatus = {
    val partialFunctions: PartialFunction[Departure, DepartureStatus] =
      Seq(
        mrnAllocated,
        departureSubmitted,
        positiveAcknowledgement,
        releasedForTransit,
        departureDeclarationRejected,
        departureDeclarationReceived,
        guaranteeValidationFail,
        transitDeclarationSent,
        writeOffNotification,
        cancellationSubmitted,
        departureCancelled,
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

  private def mrnAllocated(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == MrnAllocated =>
      DepartureStatus(
        "departure.status.mrnAllocated",
        actions =
          Seq(ViewMovementAction(config.departureFrontendConfirmCancellationUrl(departure.departureId), "viewDepartures.table.action.cancelDeclaration"))
      )
  }

  private def viewGuaranteeValidationFailAction(departureId: DepartureId)(implicit config: FrontendAppConfig) =
    ViewMovementAction(config.departureFrontendRejectedUrl(departureId), "viewDepartures.table.action.viewErrors")

  private def guaranteeValidationFail(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == GuaranteeNotValid =>
      DepartureStatus(
        "departure.status.guaranteeValidationFail",
        actions = Seq(
          viewGuaranteeValidationFailAction(departure.departureId),
          ViewMovementAction(config.departureFrontendConfirmCancellationUrl(departure.departureId), "viewDepartures.table.action.cancelDeclaration")
        )
      )
  }

  private def departureSubmitted: PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == DepartureSubmitted =>
      DepartureStatus("departure.status.submitted", actions = Nil)
  }

  private def positiveAcknowledgement: PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == PositiveAcknowledgement =>
      DepartureStatus("departure.status.positiveAcknowledgement", actions = Nil)
  }

  private def releasedForTransit: PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == ReleaseForTransit =>
      DepartureStatus("departure.status.releasedForTransit", actions = Seq(downloadTADAction(departure)))
  }

  private def departureDeclarationRejected(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == DepartureRejected =>
      DepartureStatus(
        "departure.status.departureDeclarationRejected",
        actions = Seq(ViewMovementAction(config.departureFrontendDeclarationFailUrl(departure.departureId), "viewDepartures.table.action.viewErrors"))
      )
  }

  private def departureDeclarationReceived: PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == DepartureDeclarationReceived =>
      DepartureStatus("departure.status.departureDeclarationReceived", actions = Nil)
  }

  private def transitDeclarationSent: PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == TransitDeclarationSent =>
      DepartureStatus("departure.status.transitDeclarationSent", actions = Nil)
  }

  private def writeOffNotification: PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == WriteOffNotification =>
      DepartureStatus("departure.status.writeOffNotification", actions = Nil)
  }

  private def cancellationSubmitted: PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == CancellationSubmitted =>
      DepartureStatus("departure.status.cancellationSubmitted", actions = Nil)
  }

  private def departureCancelled: PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == DepartureCancelled =>
      DepartureStatus("departure.status.departureCancelled", actions = Nil)
  }

  private def declarationCancellationRequest: PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == DeclarationCancellationRequest =>
      DepartureStatus("departure.status.declarationCancellationRequest", actions = Nil)
  }

  private def cancellationDecision(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == CancellationDecision =>
      DepartureStatus(
        "departure.status.declarationCancellationDecision",
        actions =
          Seq(ViewMovementAction(config.departureFrontendCancellationDecisionUrl(departure.departureId), "viewDepartures.table.action.viewCancellation"))
      )
  }

  private def noReleasedForTransit(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == NoReleaseForTransit =>
      DepartureStatus(
        "departure.status.noReleaseForTransit",
        actions = Seq(
          ViewMovementAction(departureRoutes.NoReleaseForTransitController.onPageLoad(departure.departureId).url, "departure.viewDetails"),
          ViewMovementAction(config.departureFrontendConfirmCancellationUrl(departure.departureId), "viewDepartures.table.action.cancelDeclaration")
        )
      )
  }

  private def controlDecision(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == ControlDecisionNotification =>
      DepartureStatus(
        "departure.status.controlDecision",
        actions = Seq(
          ViewMovementAction(departureRoutes.ControlDecisionController.onPageLoad(departure.departureId, departure.localReferenceNumber).url,
                             "departure.viewDetails"
          ),
          ViewMovementAction(config.departureFrontendConfirmCancellationUrl(departure.departureId), "viewDepartures.table.action.cancelDeclaration")
        )
      )
  }

  private def departureXmlNegativeAcknowledgement: PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == DepartureSubmittedNegativeAcknowledgement =>
      DepartureStatus(
        "departure.status.XMLSubmissionNegativeAcknowledgement",
        actions = Seq(
          ViewMovementAction(departureRoutes.DepartureXmlNegativeAcknowledgementController.onPageLoad(departure.departureId).url,
                             "viewDepartures.table.action.viewErrors"
          )
        )
      )
  }

  private def cancellationXmlNegativeAcknowledgement: PartialFunction[Departure, DepartureStatus] = {
    case departure if departure.status == DeclarationCancellationRequestNegativeAcknowledgement =>
      DepartureStatus(
        "departure.status.XMLCancellationSubmissionNegativeAcknowledgement",
        actions = Seq(
          ViewMovementAction(departureRoutes.CancellationXmlNegativeAcknowledgementController.onPageLoad(departure.departureId).url,
                             "viewDepartures.table.action.viewErrors"
          )
        )
      )
  }

  private def invalidStatus: PartialFunction[Departure, DepartureStatus] = {
    case departure => DepartureStatus(departure.status.toString, actions = Nil)
  }
}
