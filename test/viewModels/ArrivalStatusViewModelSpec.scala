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
import controllers.arrival.{routes => arrivalRoute}
import generators.Generators
import models.Arrival
import models.arrival.ArrivalStatus._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ArrivalStatusViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "Movement status" - {
    "display correct status" - {
      "When status is ArrivalSubmitted show correct message" in {

        forAll(arbitrary[Arrival]) {
          arrival =>
            val updatedArrival: Arrival = arrival.copy(status = ArrivalSubmitted)
            ArrivalStatusViewModel(updatedArrival)(frontendAppConfig).status mustBe "movement.status.arrivalSubmitted"
        }
      }
      "When status is ArrivalRejected show correct message" in {

        forAll(arbitrary[Arrival]) {
          arrival =>
            val updatedArrival: Arrival = arrival.copy(status = ArrivalRejected)
            ArrivalStatusViewModel(updatedArrival)(frontendAppConfig).status mustBe "movement.status.arrivalRejected"
        }
      }
      "When status is UnloadingRemarksSubmitted show correct message" in {

        forAll(arbitrary[Arrival]) {
          arrival =>
            val updatedArrival: Arrival = arrival.copy(status = UnloadingRemarksSubmitted)
            ArrivalStatusViewModel(updatedArrival)(frontendAppConfig).status mustBe "movement.status.unloadingRemarksSubmitted"
        }
      }
      "When status is UnloadingRemarksRejected show correct message" in {

        forAll(arbitrary[Arrival]) {
          arrival =>
            val updatedArrival: Arrival                = arrival.copy(status = UnloadingRemarksRejected)
            val movementStatus: ArrivalStatusViewModel = ArrivalStatusViewModel(updatedArrival)(frontendAppConfig)
            movementStatus.status mustBe "movement.status.unloadingRemarksRejected"
            movementStatus.actions.head.href mustBe frontendAppConfig.unloadingRemarksRejectedUrl(updatedArrival.arrivalId)
        }
      }
      "When status is GoodsReleased show correct message" in {

        forAll(arbitrary[Arrival]) {
          arrival =>
            val updatedArrival: Arrival = arrival.copy(status = GoodsReleased)
            ArrivalStatusViewModel(updatedArrival)(frontendAppConfig).status mustBe "movement.status.goodsReleased"
        }
      }
      "When status is ArrivalSubmittedNegativeAcknowledgement show correct message" in {

        forAll(arbitrary[Arrival]) {
          arrival =>
            val updatedArrival: Arrival = arrival.copy(status = ArrivalSubmittedNegativeAcknowledgement)
            val expectedAction = ViewMovementAction(
              arrivalRoute.ArrivalXmlNegativeAcknowledgementController.onPageLoad(arrival.arrivalId).url,
              "viewArrivalNotifications.table.action.viewErrors"
            )

            ArrivalStatusViewModel(updatedArrival)(frontendAppConfig).status mustBe "movement.status.ArrivalSubmittedNegativeAcknowledgement"
            ArrivalStatusViewModel(updatedArrival)(frontendAppConfig).actions.headOption mustBe Some(expectedAction)
        }
      }

      "When status is UnloadingRemarksXMLSubmissionNegativeAcknowledgement show correct message" in {

        forAll(arbitrary[Arrival]) {
          arrival =>
            val updatedArrival: Arrival = arrival.copy(status = UnloadingRemarksSubmittedNegativeAcknowledgement)
            val expectedAction = ViewMovementAction(
              controllers.arrival.routes.UnloadingRemarksXmlNegativeAcknowledgementController.onPageLoad(arrival.arrivalId).url,
              "viewArrivalNotifications.table.action.viewErrors"
            )

            ArrivalStatusViewModel(updatedArrival)(frontendAppConfig).status mustBe "movement.status.UnloadingRemarksXMLSubmissionNegativeAcknowledgement"
            ArrivalStatusViewModel(updatedArrival)(frontendAppConfig).actions.headOption mustBe Some(expectedAction)
        }
      }
    }
  }

}
