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
import controllers.departure.routes._
import models.Departure
import models.departure.DepartureStatus
import models.departure.DepartureStatus._
import play.api.mvc.Call

case class DepartureStatusViewModel(status: String, actions: Seq[ViewMovementAction])

object DepartureStatusViewModel {

  def apply(departure: Departure)(implicit config: FrontendAppConfig): DepartureStatusViewModel =
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
    ).reduce(_ orElse _).apply(departure)

  private def mrnAllocated(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] =
    createViewModel(
      status = MrnAllocated,
      statusKey = "mrnAllocated",
      actions = departure => Seq(cancelDeclarationAction(departure))
    )

  private def guaranteeValidationFail(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] =
    createViewModel(
      status = GuaranteeNotValid,
      statusKey = "guaranteeValidationFail",
      actions = departure =>
        Seq(
          viewErrorsAction(config.departureFrontendRejectedUrl(departure.departureId)),
          cancelDeclarationAction(departure)
        )
    )

  private def departureSubmitted: PartialFunction[Departure, DepartureStatusViewModel] =
    createViewModel(
      status = DepartureSubmitted,
      statusKey = "submitted",
      actions = _ => Nil
    )

  private def positiveAcknowledgement: PartialFunction[Departure, DepartureStatusViewModel] =
    createViewModel(
      status = PositiveAcknowledgement,
      statusKey = "positiveAcknowledgement",
      actions = _ => Nil
    )

  private def releasedForTransit: PartialFunction[Departure, DepartureStatusViewModel] =
    createViewModel(
      status = ReleaseForTransit,
      statusKey = "releasedForTransit",
      actions = departure =>
        Seq(
          ViewMovementAction(
            AccompanyingDocumentPDFController.getPDF(departure.departureId).url,
            "viewDepartures.table.action.viewPDF"
          )
        )
    )

  private def departureDeclarationRejected(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] =
    createViewModel(
      status = DepartureRejected,
      statusKey = "departureDeclarationRejected",
      actions = departure =>
        Seq(
          viewErrorsAction(config.departureFrontendDeclarationFailUrl(departure.departureId))
        )
    )

  private def writeOffNotification: PartialFunction[Departure, DepartureStatusViewModel] =
    createViewModel(
      status = WriteOffNotification,
      statusKey = "writeOffNotification",
      actions = _ => Nil
    )

  private def declarationCancellationRequest: PartialFunction[Departure, DepartureStatusViewModel] =
    createViewModel(
      status = DeclarationCancellationRequest,
      statusKey = "declarationCancellationRequest",
      actions = _ => Nil
    )

  private def cancellationDecision(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] =
    createViewModel(
      status = CancellationDecision,
      statusKey = "declarationCancellationDecision",
      actions = departure =>
        Seq(
          ViewMovementAction(
            config.departureFrontendCancellationDecisionUrl(departure.departureId),
            "viewDepartures.table.action.viewCancellation"
          )
        )
    )

  private def noReleasedForTransit: PartialFunction[Departure, DepartureStatusViewModel] =
    createViewModel(
      status = NoReleaseForTransit,
      statusKey = "noReleaseForTransit",
      actions = departure =>
        Seq(
          viewDetailsAction(NoReleaseForTransitController.onPageLoad(departure.departureId))
        )
    )

  private def controlDecision(implicit config: FrontendAppConfig): PartialFunction[Departure, DepartureStatusViewModel] =
    createViewModel(
      status = ControlDecisionNotification,
      statusKey = "controlDecision",
      actions = departure =>
        Seq(
          viewDetailsAction(ControlDecisionController.onPageLoad(departure.departureId, departure.localReferenceNumber)),
          cancelDeclarationAction(departure)
        )
    )

  private def departureXmlNegativeAcknowledgement: PartialFunction[Departure, DepartureStatusViewModel] =
    createViewModel(
      status = DepartureSubmittedNegativeAcknowledgement,
      statusKey = "XMLSubmissionNegativeAcknowledgement",
      actions = departure =>
        Seq(
          viewErrorsAction(DepartureXmlNegativeAcknowledgementController.onPageLoad(departure.departureId).url)
        )
    )

  private def cancellationXmlNegativeAcknowledgement: PartialFunction[Departure, DepartureStatusViewModel] =
    createViewModel(
      status = DeclarationCancellationRequestNegativeAcknowledgement,
      statusKey = "XMLCancellationSubmissionNegativeAcknowledgement",
      actions = departure =>
        Seq(
          viewErrorsAction(CancellationXmlNegativeAcknowledgementController.onPageLoad(departure.departureId).url)
        )
    )

  private def invalidStatus: PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure =>
      DepartureStatusViewModel(
        status = departure.status.toString,
        actions = Nil
      )
  }

  private def createViewModel(
    status: DepartureStatus,
    statusKey: String,
    actions: Departure => Seq[ViewMovementAction]
  ): PartialFunction[Departure, DepartureStatusViewModel] = {
    case departure if departure.status == status =>
      DepartureStatusViewModel(
        status = s"departure.status.p4.$statusKey",
        actions = actions(departure)
      )
  }

  private def cancelDeclarationAction(departure: Departure)(implicit config: FrontendAppConfig) =
    ViewMovementAction(
      config.departureFrontendConfirmCancellationUrl(departure.departureId),
      "viewDepartures.table.action.cancelDeclaration"
    )

  private def viewErrorsAction(href: String) =
    ViewMovementAction(href, "viewDepartures.table.action.viewErrors")

  private def viewDetailsAction(call: Call) =
    ViewMovementAction(call.url, "departure.viewDetails")
}
