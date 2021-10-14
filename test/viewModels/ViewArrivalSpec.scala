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
import models.Arrival
import models.arrival.ArrivalMessageMetaData
import models.arrival.ArrivalStatus.{ArrivalNotificationSubmitted, ArrivalRejection, GoodsReleased, UnloadingPermission, UnloadingRemarksSubmitted}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.Messages
import play.api.libs.json.Json

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ViewArrivalSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must serialise to Json" in {

    forAll(arbitrary[ViewArrival]) {
      viewMovement =>
        val formatTime =
          viewMovement.updatedTime.format(DateTimeFormatter.ofPattern("h:mma")).toLowerCase

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
        val updatedArrival: Arrival   = arrival.copy(messagesMetaData = Seq(ArrivalMessageMetaData(UnloadingPermission, LocalDateTime.now())))
        val viewMovement: ViewArrival = ViewArrival(updatedArrival)(frontendAppConfig)

        viewMovement.status mustBe Messages("movement.status.unloadingPermission")
        viewMovement.actions.head.href mustBe s"http://localhost:9488/manage-transit-movements-unloading-remarks/${arrival.arrivalId.index}"
    }
  }

  "must display rejection" in {
    forAll(arbitrary[Arrival]) {
      arrival =>
        val updatedArrival: Arrival   = arrival.copy(messagesMetaData = Seq(ArrivalMessageMetaData(ArrivalRejection, LocalDateTime.now())))
        val viewMovement: ViewArrival = ViewArrival(updatedArrival)(frontendAppConfig)

        viewMovement.status mustBe Messages("movement.status.arrivalRejected")
        viewMovement.actions.head.href mustBe s"http://localhost:9483/manage-transit-movements-arrivals/${arrival.arrivalId.index}/arrival-rejection"
    }
  }

  "must not display action when status is not unloading permission, rejection or negative acknowledgment" in {

    val genArrivalStatus = Gen.oneOf(Seq(ArrivalNotificationSubmitted, GoodsReleased, UnloadingRemarksSubmitted))

    forAll(arbitrary[Arrival], genArrivalStatus) {
      (arrival, arrivalStatus) =>
        val updatedArrival: Arrival   = arrival.copy(messagesMetaData = Seq(ArrivalMessageMetaData(arrivalStatus, LocalDateTime.now())))
        val viewMovement: ViewArrival = ViewArrival(updatedArrival)(frontendAppConfig)

        viewMovement.actions mustBe Nil
    }
  }
}
