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

import base.SpecBase
import generators.Generators
import models.{Departure, ViewMovementAction}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import controllers.testOnly.{routes => testRoutes}
import play.api.libs.json.{JsString, Json}

class DepartureStatusSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "Departure Status" - {
    val viewTransitDeclarationFailAction =
      ViewMovementAction(frontendAppConfig.departureFrontendDeclarationFailUrl(departureId), "viewDepartures.table.action.viewErrors")

    val statusOptions = Seq(
      Map("title"      -> "TransitDeclarationRejected",
          "messageKey" -> "departure.status.transitDeclarationRejected",
          "link"       -> Seq(viewTransitDeclarationFailAction)),
      Map("title"      -> "DepartureDeclarationReceived", "messageKey" -> "departure.status.departureDeclarationReceived", "link" -> Nil),
      Map("title"      -> "TransitDeclarationSent", "messageKey" -> "departure.status.transitDeclarationSent", "link" -> Nil),
      Map("title"      -> "WriteOffNotification", "messageKey" -> "departure.status.writeOffNotification", "link" -> Nil)
    )

    "display correct data for each status" - {
      for (status <- statusOptions) {
        s"When status is `${status("title")}` display message and link" in {
          forAll(arbitrary[Departure]) {
            departure =>
              val dep = departure
                .copy(departureId = departureId)
                .copy(status = status("title").toString)
              val departureStatus = DepartureStatus(dep, frontendAppConfig)
              departureStatus.status mustBe status("messageKey").toString
              departureStatus.actions mustBe status("link")
          }
        }
      }
    }

    "include tad link on ReleasedForTransit status" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val dep             = departure.copy(status = "ReleaseForTransit")
          val departureStatus = DepartureStatus(dep, frontendAppConfig)
          departureStatus.status mustBe "departure.status.releasedForTransit"
          departureStatus.actions.head.href mustBe s"/manage-transit-movements/test-only/departures/${departure.departureId.index}/tad-pdf"
          departureStatus.actions.head.key mustBe "viewDepartures.table.action.viewPDF"
      }
    }

    "When status is guaranteeValidationFail show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure      = departure.copy(status = "GuaranteeNotValid")
          val departureStatus: DepartureStatus = DepartureStatus(updatedDeparture, frontendAppConfig)
          departureStatus.status mustBe "departure.status.guaranteeValidationFail"
          departureStatus.actions.head.href mustBe frontendAppConfig.departureFrontendRejectedUrl(updatedDeparture.departureId)
          departureStatus.actions.head.key mustBe "viewDepartures.table.action.viewErrors"
      }
    }

    "When status is CancellationDecision show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure      = departure.copy(status = "CancellationDecision")
          val departureStatus: DepartureStatus = DepartureStatus(updatedDeparture, frontendAppConfig)
          departureStatus.status mustBe "departure.status.declarationCancellationRequest"
          departureStatus.actions.head.href mustBe frontendAppConfig.departureFrontendCancellationDecisionUrl(updatedDeparture.departureId)
          departureStatus.actions.head.key mustBe "viewDepartures.table.action.viewCancellation"
      }
    }

    "When status is noReleasedForTransit show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure      = departure.copy(status = "NoReleaseForTransit")
          val departureStatus: DepartureStatus = DepartureStatus(updatedDeparture, frontendAppConfig)
          departureStatus.status mustBe "departure.status.noReleaseForTransit"
          departureStatus.actions.head.href mustBe testRoutes.NoReleaseForTransitController.onPageLoad(updatedDeparture.departureId).url
          departureStatus.actions.head.key mustBe "departure.viewDetails"
      }
    }

    "When status is controlDecision show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure      = departure.copy(status = "ControlDecisionNotification")
          val departureStatus: DepartureStatus = DepartureStatus(updatedDeparture, frontendAppConfig)
          departureStatus.status mustBe "departure.status.controlDecision"
          departureStatus.actions.head.href mustBe testRoutes.ControlDecisionController
            .onPageLoad(updatedDeparture.departureId, updatedDeparture.localReferenceNumber)
            .url
          departureStatus.actions.head.key mustBe "departure.viewDetails"
      }
    }

    "When status is mrnAllocated show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure      = departure.copy(status = "MrnAllocated")
          val departureStatus: DepartureStatus = DepartureStatus(updatedDeparture, frontendAppConfig)
          departureStatus.status mustBe "departure.status.mrnAllocated"
          departureStatus.actions.head.href mustBe frontendAppConfig.departureFrontendConfirmCancellationUrl(updatedDeparture.departureId)
          departureStatus.actions.head.key mustBe "viewDepartures.table.action.cancelDeclaration"
      }
    }

    "When status is positiveAcknowledgement show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure      = departure.copy(status = "PositiveAcknowledgement")
          val departureStatus: DepartureStatus = DepartureStatus(updatedDeparture, frontendAppConfig)
          departureStatus.status mustBe "departure.status.positiveAcknowledgement"
          departureStatus.actions.head.href mustBe frontendAppConfig.departureFrontendConfirmCancellationUrl(updatedDeparture.departureId)
          departureStatus.actions.head.key mustBe "viewDepartures.table.action.cancelDeclaration"
      }
    }

    "When status is departureSubmitted show correct status and action" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val updatedDeparture: Departure      = departure.copy(status = "DepartureSubmitted")
          val departureStatus: DepartureStatus = DepartureStatus(updatedDeparture, frontendAppConfig)
          departureStatus.status mustBe "departure.status.submitted"
          departureStatus.actions.head.href mustBe frontendAppConfig.departureFrontendConfirmCancellationUrl(updatedDeparture.departureId)
          departureStatus.actions.head.key mustBe "viewDepartures.table.action.cancelDeclaration"
      }
    }
  }

  "Deserialization" - {
    "is successful for MrnAllocated" in {
      val json = JsString("MrnAllocated")

      json.as[DepartureStatus2] mustEqual MrnAllocated
    }

    "is successful for DepartureSubmitted" in {
      val json = JsString("DepartureSubmitted")

      json.as[DepartureStatus2] mustEqual DepartureSubmitted
    }

    "is successful for PositiveAcknowledgement" in {
      val json = JsString("PositiveAcknowledgement")

      json.as[DepartureStatus2] mustEqual PositiveAcknowledgement
    }

    "is successful for ReleaseForTransit" in {
      val json = JsString("ReleaseForTransit")

      json.as[DepartureStatus2] mustEqual ReleaseForTransit
    }

    "is successful for TransitDeclarationRejected" in {
      val json = JsString("TransitDeclarationRejected")

      json.as[DepartureStatus2] mustEqual TransitDeclarationRejected
    }

    "is successful for DepartureDeclarationReceived" in {
      val json = JsString("DepartureDeclarationReceived")

      json.as[DepartureStatus2] mustEqual DepartureDeclarationReceived
    }

    "is successful for GuaranteeNotValid" in {
      val json = JsString("GuaranteeNotValid")

      json.as[DepartureStatus2] mustEqual GuaranteeNotValid
    }

    "is successful for TransitDeclarationSent" in {
      val json = JsString("TransitDeclarationSent")

      json.as[DepartureStatus2] mustEqual TransitDeclarationSent
    }

    "is successful for WriteOffNotification" in {
      val json = JsString("WriteOffNotification")

      json.as[DepartureStatus2] mustEqual WriteOffNotification
    }

    "is successful for DeclarationCancellationRequest" in {
      val json = JsString("DeclarationCancellationRequest")

      json.as[DepartureStatus2] mustEqual DeclarationCancellationRequest
    }

    "is successful for CancellationDecision" in {
      val json = JsString("CancellationDecision")

      json.as[DepartureStatus2] mustEqual CancellationDecision
    }

    "is successful for NoReleaseForTransit" in {
      val json = JsString("NoReleaseForTransit")

      json.as[DepartureStatus2] mustEqual NoReleaseForTransit
    }

    "is successful for ControlDecisionNotification" in {
      val json = JsString("ControlDecisionNotification")

      json.as[DepartureStatus2] mustEqual ControlDecisionNotification
    }

    "is successful for InvalidStatus" in {
      val json = JsString("invalid")

      json.as[DepartureStatus2] mustEqual InvalidStatus
    }
  }
}
