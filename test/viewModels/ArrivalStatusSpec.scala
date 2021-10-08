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
import controllers.arrival.{routes => arrivalRoute}
import generators.Generators
import models.Arrival
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.Messages

class ArrivalStatusSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "Movement status" - {
    "display correct status" - {
      "When status is ArrivalSubmitted show correct message" in {

        forAll(arbitrary[Arrival]) {
          arrival =>
            val arr: Arrival = arrival.copy(status = "ArrivalSubmitted")
            ArrivalStatus(arr)(frontendAppConfig).status mustBe Messages("movement.status.arrivalSubmitted")
        }
      }
      "When status is ArrivalRejected show correct message" in {

        forAll(arbitrary[Arrival]) {
          arrival =>
            val arr: Arrival = arrival.copy(status = "ArrivalRejected")
            ArrivalStatus(arr)(frontendAppConfig).status mustBe Messages("movement.status.arrivalRejected")
        }
      }
      "When status is UnloadingRemarksSubmitted show correct message" in {

        forAll(arbitrary[Arrival]) {
          arrival =>
            val arr: Arrival = arrival.copy(status = "UnloadingRemarksSubmitted")
            ArrivalStatus(arr)(frontendAppConfig).status mustBe Messages("movement.status.unloadingRemarksSubmitted")
        }
      }
      "When status is UnloadingRemarksRejected show correct message" in {

        forAll(arbitrary[Arrival]) {
          arrival =>
            val arr: Arrival                  = arrival.copy(status = "UnloadingRemarksRejected")
            val movementStatus: ArrivalStatus = ArrivalStatus(arr)(frontendAppConfig)
            movementStatus.status mustBe Messages("movement.status.unloadingRemarksRejected")
            movementStatus.actions.head.href mustBe frontendAppConfig.unloadingRemarksRejectedUrl(arr.arrivalId)
        }
      }
      "When status is GoodsReleased show correct message" in {

        forAll(arbitrary[Arrival]) {
          arrival =>
            val arr: Arrival = arrival.copy(status = "GoodsReleased")
            ArrivalStatus(arr)(frontendAppConfig).status mustBe Messages("movement.status.goodsReleased")
        }
      }
      "When status is XMLSubmissionNegativeAcknowledgement show correct message" in {

        forAll(arbitrary[Arrival]) {
          arrival =>
            val arr: Arrival = arrival.copy(status = "ArrivalXMLSubmissionNegativeAcknowledgement")
            val expectedAction = ViewMovementAction(
              arrivalRoute.ArrivalXmlNegativeAcknowledgementController.onPageLoad(arrival.arrivalId).url,
              Messages("viewArrivalNotifications.table.action.viewErrors")
            )

            ArrivalStatus(arr)(frontendAppConfig).status mustBe Messages("movement.status.XMLSubmissionNegativeAcknowledgement")
            ArrivalStatus(arr)(frontendAppConfig).actions.headOption mustBe Some(expectedAction)
        }
      }

      "When status is UnloadingRemarksXMLSubmissionNegativeAcknowledgement show correct message" in {

        forAll(arbitrary[Arrival]) {
          arrival =>
            val arr: Arrival = arrival.copy(status = "UnloadingRemarksXMLSubmissionNegativeAcknowledgement")
            val expectedAction = ViewMovementAction(
              controllers.arrival.routes.UnloadingRemarksXmlNegativeAcknowledgementController.onPageLoad(arrival.arrivalId).url,
              Messages("viewArrivalNotifications.table.action.viewErrors")
            )

            ArrivalStatus(arr)(frontendAppConfig).status mustBe Messages("movement.status.UnloadingRemarksXMLSubmissionNegativeAcknowledgement")
            ArrivalStatus(arr)(frontendAppConfig).actions.headOption mustBe Some(expectedAction)
        }
      }
    }
  }

}
