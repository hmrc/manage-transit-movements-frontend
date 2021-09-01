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

import base.{FakeFrontendAppConfig, SpecBase}
import generators.Generators
import models.Arrival
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.Messages
import play.api.libs.json.Json

import java.time.format.DateTimeFormatter

class ViewArrivalSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  val frontendAppConfig = FakeFrontendAppConfig()

  "must serialise to Json" in {

    forAll(arbitrary[ViewArrival]) {
      viewMovement =>
        val formatTime =
          viewMovement.time.format(DateTimeFormatter.ofPattern("h:mma")).toLowerCase

        val expectedJson = Json.obj(
          "updated"         -> formatTime,
          "referenceNumber" -> viewMovement.movementReferenceNumber,
          "status"          -> viewMovement.status,
          "actions"         -> viewMovement.actions
        )

        Json.toJson(viewMovement) mustBe expectedJson
    }
  }

  "must display unloading permission" in {
    forAll(arbitrary[Arrival]) {
      arrival =>
        val unloadingArrival: Arrival = arrival.copy(status = "UnloadingPermission")
        val viewMovement: ViewArrival = ViewArrival(unloadingArrival)(messages, frontendAppConfig)

        viewMovement.status mustBe Messages("movement.status.unloadingPermission")
        viewMovement.actions.head.href mustBe s"http://localhost:9488/manage-transit-movements-unloading-remarks/${arrival.arrivalId.index}"
    }
  }

  "must display rejection" in {
    forAll(arbitrary[Arrival]) {
      arrival =>
        val unloadingArrival: Arrival = arrival.copy(status = "ArrivalRejected")
        val viewMovement: ViewArrival = ViewArrival(unloadingArrival)(messages, frontendAppConfig)

        viewMovement.status mustBe Messages("movement.status.arrivalRejected")
        viewMovement.actions.head.href mustBe s"http://localhost:9483/manage-transit-movements-arrivals/${arrival.arrivalId.index}/arrival-rejection"
    }
  }

  "must display correct status" in {

    forAll(arbitrary[Arrival]) {
      arrival =>
        val unloadingArrival: Arrival = arrival.copy(status = "")
        val viewMovement: ViewArrival = ViewArrival(unloadingArrival)(messages, frontendAppConfig)

        viewMovement.status mustBe unloadingArrival.status
    }

  }

  "must not display action when status is not unloading permission or rejection" in {

    forAll(arbitrary[Arrival]) {
      arrival =>
        val unloadingArrival: Arrival = arrival.copy(status = "")
        val viewMovement: ViewArrival = ViewArrival(unloadingArrival)(messages, frontendAppConfig)

        viewMovement.actions mustBe Nil
    }
  }
}
