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

package config

import base.SpecBase
import models.{ArrivalId, DepartureId}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import org.scalacheck.Arbitrary.arbitrary

class FrontendAppConfigSpec extends SpecBase with ScalaCheckPropertyChecks {

  "frontend urls" - {

    "when phase 4" - {
      "must point to to phase 4 frontend urls" in {
        val app: Application = new GuiceApplicationBuilder()
          .configure(
            "microservice.services.features.phase5Enabled" -> Map(
              "departure"    -> false,
              "unloading"    -> false,
              "cancellation" -> false,
              "arrival"      -> false
            )
          )
          .build()

        val config = app.injector.instanceOf[FrontendAppConfig]

        forAll(arbitrary[Int]) {
          index =>
            config.declareDepartureStartWithLRNUrl mustEqual
              "http://localhost:9489/manage-transit-movements-departures/local-reference-number"
            config.departureFrontendRejectedUrl(DepartureId(index)) mustEqual
              s"http://localhost:9489/manage-transit-movements-departures/$index/guarantee-rejection"
            config.departureFrontendDeclarationFailUrl(DepartureId(index)) mustEqual
              s"http://localhost:9489/manage-transit-movements-departures/$index/departure-declaration-fail"
            config.departureFrontendCancellationDecisionUrl(DepartureId(index)) mustEqual
              s"http://localhost:9489/manage-transit-movements-departures/$index/cancellation-decision-update"
            config.departureTadPdfUrl(DepartureId(index)) mustEqual
              s"http://localhost:9489/manage-transit-movements-departures/$index/tad-pdf"

            config.declareArrivalNotificationStartUrl mustEqual
              "http://localhost:9483/manage-transit-movements-arrivals/movement-reference-number"
            config.arrivalFrontendRejectedUrl(ArrivalId(index)) mustEqual
              s"http://localhost:9483/manage-transit-movements-arrivals/$index/arrival-rejection"

            config.declareUnloadingRemarksUrl(ArrivalId(index)) mustEqual
              s"http://localhost:9488/manage-transit-movements-unloading-remarks/$index"
            config.unloadingRemarksRejectedUrl(ArrivalId(index)) mustEqual
              s"http://localhost:9488/manage-transit-movements-unloading-remarks/$index/unloading-rejection"

            config.departureFrontendConfirmCancellationUrl(DepartureId(index)) mustEqual
              s"http://localhost:9495/manage-transit-movements-departures-cancel/$index/confirm-cancellation"
        }
      }
    }

    "when phase 5" - {
      "must point to to phase 5 frontend urls" in {
        val app: Application = new GuiceApplicationBuilder()
          .configure(
            "microservice.services.features.phase5Enabled" -> Map(
              "departure"    -> true,
              "unloading"    -> true,
              "cancellation" -> true,
              "arrival"      -> true
            )
          )
          .build()

        val config = app.injector.instanceOf[FrontendAppConfig]

        forAll(arbitrary[Int]) {
          index =>
            config.declareDepartureStartWithLRNUrl mustEqual
              "http://localhost:10120/manage-transit-movements/departure/local-reference-number"
            config.departureFrontendRejectedUrl(DepartureId(index)) mustEqual
              s"http://localhost:10120/manage-transit-movements/departure/$index/guarantee-rejection"
            config.departureFrontendDeclarationFailUrl(DepartureId(index)) mustEqual
              s"http://localhost:10120/manage-transit-movements/departure/$index/departure-declaration-fail"
            config.departureFrontendCancellationDecisionUrl(DepartureId(index)) mustEqual
              s"http://localhost:10120/manage-transit-movements/departure/$index/cancellation-decision-update"
            config.departureTadPdfUrl(DepartureId(index)) mustEqual
              s"http://localhost:10120/manage-transit-movements/departure/$index/tad-pdf"

            config.declareArrivalNotificationStartUrl mustEqual
              "http://localhost:10121/manage-transit-movements/arrival/movement-reference-number"
            config.arrivalFrontendRejectedUrl(ArrivalId(index)) mustEqual
              s"http://localhost:10121/manage-transit-movements/arrival/$index/arrival-rejection"

            config.declareUnloadingRemarksUrl(ArrivalId(index)) mustEqual
              s"http://localhost:10123/manage-transit-movements/unloading/$index"
            config.unloadingRemarksRejectedUrl(ArrivalId(index)) mustEqual
              s"http://localhost:10123/manage-transit-movements/unloading/$index/unloading-rejection"

            config.departureFrontendConfirmCancellationUrl(DepartureId(index)) mustEqual
              s"http://localhost:10122/manage-transit-movements/cancellation/$index"
        }
      }
    }
  }
}
