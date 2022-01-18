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

import base.SpecBase
import controllers.departure.{routes => departureRoutes}
import generators.Generators
import models.Departure
import models.departure.DepartureStatus._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class DepartureStatusViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "Departure Status" - {

    "When status is TransitDeclarationRejected show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure = departure.copy(status = DepartureRejected)

          val departureStatus: DepartureStatusViewModel = DepartureStatusViewModel(updatedDeparture)(frontendAppConfig)
          departureStatus.status mustBe "departure.status.departureDeclarationRejected"
          departureStatus.actions.size mustBe 1
          departureStatus.actions.head.href mustBe frontendAppConfig.departureFrontendDeclarationFailUrl(updatedDeparture.departureId)
          departureStatus.actions.head.key mustBe "viewDepartures.table.action.viewErrors"
      }
    }

    "When status is WriteOffNotification show correct status" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure = departure.copy(status = WriteOffNotification)

          val departureStatus: DepartureStatusViewModel = DepartureStatusViewModel(updatedDeparture)(frontendAppConfig)
          departureStatus.status mustBe "departure.status.writeOffNotification"
          departureStatus.actions.size mustBe 0
      }
    }

    "include tad link on ReleasedForTransit status" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure = departure.copy(status = ReleaseForTransit)

          val departureStatus: DepartureStatusViewModel = DepartureStatusViewModel(updatedDeparture)(frontendAppConfig)
          departureStatus.status mustBe "departure.status.releasedForTransit"
          departureStatus.actions.size mustBe 1
          departureStatus.actions.head.href mustBe departureRoutes.AccompanyingDocumentPDFController.getPDF(departure.departureId).url
          departureStatus.actions.head.key mustBe "viewDepartures.table.action.viewPDF"
      }
    }

    "When status is GuaranteeValidationFail show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure = departure.copy(status = GuaranteeNotValid)

          val departureStatus: DepartureStatusViewModel = DepartureStatusViewModel(updatedDeparture)(frontendAppConfig)
          departureStatus.status mustBe "departure.status.guaranteeValidationFail"
          departureStatus.actions.size mustBe 2
          departureStatus.actions.head.href mustBe frontendAppConfig.departureFrontendRejectedUrl(updatedDeparture.departureId)
          departureStatus.actions.head.key mustBe "viewDepartures.table.action.viewErrors"
      }
    }

    "When status is DeclarationCancellationRequest show correct status" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure =
            departure.copy(status = DeclarationCancellationRequest)

          val departureStatus: DepartureStatusViewModel = DepartureStatusViewModel(updatedDeparture)(frontendAppConfig)
          departureStatus.status mustBe "departure.status.declarationCancellationRequest"
          departureStatus.actions.size mustBe 0
      }
    }

    "When status is CancellationDecision show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure = departure.copy(status = CancellationDecision)

          val departureStatus: DepartureStatusViewModel = DepartureStatusViewModel(updatedDeparture)(frontendAppConfig)
          departureStatus.status mustBe "departure.status.declarationCancellationDecision"
          departureStatus.actions.size mustBe 1
          departureStatus.actions.head.href mustBe frontendAppConfig.departureFrontendCancellationDecisionUrl(updatedDeparture.departureId)
          departureStatus.actions.head.key mustBe "viewDepartures.table.action.viewCancellation"
      }
    }

    "When status is NoReleasedForTransit show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure = departure.copy(status = NoReleaseForTransit)

          val departureStatus: DepartureStatusViewModel = DepartureStatusViewModel(updatedDeparture)(frontendAppConfig)
          departureStatus.status mustBe "departure.status.noReleaseForTransit"
          departureStatus.actions.size mustBe 2
          departureStatus.actions.head.href mustBe departureRoutes.NoReleaseForTransitController.onPageLoad(updatedDeparture.departureId).url
          departureStatus.actions.head.key mustBe "departure.viewDetails"
      }
    }

    "When status is ControlDecision show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure = departure.copy(status = ControlDecisionNotification)

          val departureStatus: DepartureStatusViewModel = DepartureStatusViewModel(updatedDeparture)(frontendAppConfig)
          departureStatus.status mustBe "departure.status.controlDecision"
          departureStatus.actions.size mustBe 2
          departureStatus.actions.head.href mustBe departureRoutes.ControlDecisionController
            .onPageLoad(updatedDeparture.departureId, updatedDeparture.localReferenceNumber)
            .url
          departureStatus.actions.head.key mustBe "departure.viewDetails"
      }
    }

    "When status is MrnAllocated show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure = departure.copy(status = MrnAllocated)

          val departureStatus: DepartureStatusViewModel = DepartureStatusViewModel(updatedDeparture)(frontendAppConfig)
          departureStatus.status mustBe "departure.status.mrnAllocated"
          departureStatus.actions.size mustBe 1
          departureStatus.actions.head.href mustBe frontendAppConfig.departureFrontendConfirmCancellationUrl(updatedDeparture.departureId)
          departureStatus.actions.head.key mustBe "viewDepartures.table.action.cancelDeclaration"
      }
    }

    "When status is PositiveAcknowledgement show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure = departure.copy(status = PositiveAcknowledgement)

          val departureStatus: DepartureStatusViewModel = DepartureStatusViewModel(updatedDeparture)(frontendAppConfig)
          departureStatus.status mustBe "departure.status.positiveAcknowledgement"
          departureStatus.actions.size mustBe 0
      }
    }

    "When status is DepartureSubmitted show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure = departure.copy(status = DepartureSubmitted)

          val departureStatus: DepartureStatusViewModel = DepartureStatusViewModel(updatedDeparture)(frontendAppConfig)
          departureStatus.status mustBe "departure.status.submitted"
          departureStatus.actions.size mustBe 0
      }
    }

    "When status is XMLSubmissionNegativeAcknowledgement and previous message was DepartureSubmitted show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure = departure.copy(
            status = XMLSubmissionNegativeAcknowledgement,
            previousStatus = Some(DepartureSubmitted)
          )

          val departureStatus: DepartureStatusViewModel = DepartureStatusViewModel(updatedDeparture)(frontendAppConfig)
          departureStatus.status mustBe "departure.status.XMLSubmissionNegativeAcknowledgement"
          departureStatus.actions.size mustBe 1
      }
    }

    "When status is XMLSubmissionNegativeAcknowledgement and previous message was DeclarationCancellationRequest show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure = departure.copy(
            status = XMLSubmissionNegativeAcknowledgement,
            previousStatus = Some(DeclarationCancellationRequest)
          )

          val departureStatus: DepartureStatusViewModel = DepartureStatusViewModel(updatedDeparture)(frontendAppConfig)
          departureStatus.status mustBe "departure.status.XMLCancellationSubmissionNegativeAcknowledgement"
          departureStatus.actions.size mustBe 1
      }
    }
  }
}
