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
import generators.Generators
import models.Arrival
import models.arrival.ArrivalStatus.{ArrivalRejected, ArrivalSubmitted, GoodsReleased, UnloadingPermission, UnloadingRemarksSubmitted}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.{Clock, LocalDateTime, ZoneId}

class ViewArrivalSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must display unloading permission" in {
    forAll(arbitrary[Arrival]) {
      arrival =>
        val updatedArrival: Arrival   = arrival.copy(status = UnloadingPermission)
        val viewMovement: ViewArrival = ViewArrival(updatedArrival)

        viewMovement.status mustBe "movement.status.unloadingPermission"
        viewMovement.actions.head.href mustBe s"http://localhost:9488/manage-transit-movements-unloading-remarks/${arrival.arrivalId.index}"
    }
  }

  "must display rejection" in {
    forAll(arbitrary[Arrival]) {
      arrival =>
        val updatedArrival: Arrival   = arrival.copy(status = ArrivalRejected)
        val viewMovement: ViewArrival = ViewArrival(updatedArrival)

        viewMovement.status mustBe "movement.status.arrivalRejected"
        viewMovement.actions.head.href mustBe s"http://localhost:9483/manage-transit-movements-arrivals/${arrival.arrivalId.index}/arrival-rejection"
    }
  }

  "must not display action when status is not unloading permission, rejection or negative acknowledgment" in {

    val genArrivalStatus = Gen.oneOf(Seq(ArrivalSubmitted, GoodsReleased, UnloadingRemarksSubmitted))

    forAll(arbitrary[Arrival], genArrivalStatus) {
      (arrival, arrivalStatus) =>
        val updatedArrival: Arrival   = arrival.copy(status = arrivalStatus)
        val viewMovement: ViewArrival = ViewArrival(updatedArrival)

        viewMovement.actions mustBe Nil
    }
  }

  "must convert incoming UTC time to system time" - {

    val utcDateTime = LocalDateTime.of(2020, 2, 3, 21, 0, 0)

    // user in London updates movement at 9pm. UTC time is also 9pm.
    "when system time is UTC" in {
      forAll(arbitrary[Arrival]) {
        arrival =>
          val utcArrival            = arrival.copy(updated = utcDateTime)
          implicit val clock: Clock = Clock.systemUTC()
          val viewArrival           = ViewArrival(utcArrival)
          viewArrival.updatedDate mustEqual utcArrival.updated.toLocalDate
          viewArrival.updatedTime mustEqual utcArrival.updated.toLocalTime
      }
    }

    // user in New York updates movement in mid-afternoon. UTC time is 9pm.
    "when time changes and date stays the same" in {
      forAll(arbitrary[Arrival]) {
        arrival =>
          val utcArrival            = arrival.copy(updated = utcDateTime)
          implicit val clock: Clock = Clock.system(ZoneId.of("America/New_York"))
          val viewArrival           = ViewArrival(utcArrival)
          viewArrival.updatedDate mustEqual utcArrival.updated.toLocalDate
          viewArrival.updatedTime mustNot equal(utcArrival.updated.toLocalTime)
      }
    }

    // user in Australia updates movement in the morning. UTC time is 9pm the previous night.
    "when time changes such that date changes to following day" in {
      forAll(arbitrary[Arrival]) {
        arrival =>
          val utcArrival            = arrival.copy(updated = utcDateTime)
          implicit val clock: Clock = Clock.system(ZoneId.of("Australia/Darwin"))
          val viewArrival           = ViewArrival(utcArrival)
          viewArrival.updatedDate.isAfter(utcArrival.updated.toLocalDate) mustBe true
          viewArrival.updatedTime mustNot equal(utcArrival.updated.toLocalTime)
      }
    }

    // user in New York updates movement late at night. UTC time is 2am the following morning.
    "when time changes such that date changes to previous day" in {
      val utcDateTime = LocalDateTime.of(2020, 2, 3, 2, 0, 0)
      forAll(arbitrary[Arrival]) {
        arrival =>
          val utcArrival            = arrival.copy(updated = utcDateTime)
          implicit val clock: Clock = Clock.system(ZoneId.of("America/New_York"))
          val viewArrival           = ViewArrival(utcArrival)
          viewArrival.updatedDate.isBefore(utcArrival.updated.toLocalDate) mustBe true
          viewArrival.updatedTime mustNot equal(utcArrival.updated.toLocalTime)
      }
    }
  }
}
