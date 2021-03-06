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

package models.departure

import base.SpecBase
import models.departure.DepartureStatus._
import play.api.libs.json.JsString

class DepartureStatusSpec extends SpecBase {

  "Deserialization" - {
    "is successful for MrnAllocated" in {
      val json = JsString("MrnAllocated")

      json.as[DepartureStatus] mustEqual MrnAllocated
    }

    "is successful for DepartureSubmitted" in {
      val json = JsString("DepartureSubmitted")

      json.as[DepartureStatus] mustEqual DepartureSubmitted
    }

    "is successful for PositiveAcknowledgement" in {
      val json = JsString("PositiveAcknowledgement")

      json.as[DepartureStatus] mustEqual PositiveAcknowledgement
    }

    "is successful for ReleaseForTransit" in {
      val json = JsString("ReleaseForTransit")

      json.as[DepartureStatus] mustEqual ReleaseForTransit
    }

    "is successful for TransitDeclarationRejected" in {
      val json = JsString("TransitDeclarationRejected")

      json.as[DepartureStatus] mustEqual TransitDeclarationRejected
    }

    "is successful for DepartureDeclarationReceived" in {
      val json = JsString("DepartureDeclarationReceived")

      json.as[DepartureStatus] mustEqual DepartureDeclarationReceived
    }

    "is successful for GuaranteeNotValid" in {
      val json = JsString("GuaranteeNotValid")

      json.as[DepartureStatus] mustEqual GuaranteeNotValid
    }

    "is successful for TransitDeclarationSent" in {
      val json = JsString("TransitDeclarationSent")

      json.as[DepartureStatus] mustEqual TransitDeclarationSent
    }

    "is successful for WriteOffNotification" in {
      val json = JsString("WriteOffNotification")

      json.as[DepartureStatus] mustEqual WriteOffNotification
    }

    "is successful for DeclarationCancellationRequest" in {
      val json = JsString("DeclarationCancellationRequest")

      json.as[DepartureStatus] mustEqual DeclarationCancellationRequest
    }

    "is successful for CancellationSubmitted" in {
      val json = JsString("CancellationSubmitted")

      json.as[DepartureStatus] mustEqual CancellationSubmitted
    }

    "is successful for DepartureCancelled" in {
      val json = JsString("DepartureCancelled")

      json.as[DepartureStatus] mustEqual DepartureCancelled
    }

    "is successful for CancellationDecision" in {
      val json = JsString("CancellationDecision")

      json.as[DepartureStatus] mustEqual CancellationDecision
    }

    "is successful for NoReleaseForTransit" in {
      val json = JsString("NoReleaseForTransit")

      json.as[DepartureStatus] mustEqual NoReleaseForTransit
    }

    "is successful for ControlDecisionNotification" in {
      val json = JsString("ControlDecisionNotification")

      json.as[DepartureStatus] mustEqual ControlDecisionNotification
    }

    "is successful for InvalidStatus" in {
      val json = JsString("invalid")

      json.as[DepartureStatus] mustEqual InvalidStatus
    }
  }
}
