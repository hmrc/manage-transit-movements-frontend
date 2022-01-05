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
import models.Departure
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDateTime, ZoneId}

class ViewDepartureSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must serialise to Json" in {
    forAll(arbitrary[ViewDeparture]) {
      viewDeparture =>
        val expectedJson = Json.obj(
          "updated" -> viewDeparture.updatedTime
            .format(DateTimeFormatter.ofPattern("h:mma"))
            .toLowerCase,
          "referenceNumber" -> viewDeparture.localReferenceNumber,
          "status"          -> viewDeparture.status,
          "actions"         -> viewDeparture.actions
        )

        Json.toJson(viewDeparture) mustBe expectedJson
    }
  }

  "must convert incoming UTC time to system time" - {

    val utcDateTime = LocalDateTime.of(2020, 2, 3, 21, 0, 0)

    // user in London updates movement at 9pm. UTC time is also 9pm.
    "when system time is UTC" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val utcDeparture          = departure.copy(updated = utcDateTime)
          implicit val clock: Clock = Clock.systemUTC()
          val viewDeparture         = ViewDeparture(utcDeparture)
          viewDeparture.updatedDate mustEqual utcDeparture.updated.toLocalDate
          viewDeparture.updatedTime mustEqual utcDeparture.updated.toLocalTime
      }
    }

    // user in New York updates movement in mid-afternoon. UTC time is 9pm.
    "when time changes and date stays the same" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val utcDeparture          = departure.copy(updated = utcDateTime)
          implicit val clock: Clock = Clock.system(ZoneId.of("America/New_York"))
          val viewDeparture         = ViewDeparture(utcDeparture)
          viewDeparture.updatedDate mustEqual utcDeparture.updated.toLocalDate
          viewDeparture.updatedTime mustNot equal(utcDeparture.updated.toLocalTime)
      }
    }

    // user in Australia updates movement in the morning. UTC time is 9pm the previous night.
    "when time changes such that date changes to following day" in {
      forAll(arbitrary[Departure]) {
        departure =>
          val utcDeparture          = departure.copy(updated = utcDateTime)
          implicit val clock: Clock = Clock.system(ZoneId.of("Australia/Darwin"))
          val viewDeparture         = ViewDeparture(utcDeparture)
          viewDeparture.updatedDate.isAfter(utcDeparture.updated.toLocalDate) mustBe true
          viewDeparture.updatedTime mustNot equal(utcDeparture.updated.toLocalTime)
      }
    }

    // user in New York updates movement late at night. UTC time is 2am the following morning.
    "when time changes such that date changes to previous day" in {
      val utcDateTime = LocalDateTime.of(2020, 2, 3, 2, 0, 0)
      forAll(arbitrary[Departure]) {
        departure =>
          val utcDeparture          = departure.copy(updated = utcDateTime)
          implicit val clock: Clock = Clock.system(ZoneId.of("America/New_York"))
          val viewDeparture         = ViewDeparture(utcDeparture)
          viewDeparture.updatedDate.isBefore(utcDeparture.updated.toLocalDate) mustBe true
          viewDeparture.updatedTime mustNot equal(utcDeparture.updated.toLocalTime)
      }
    }
  }
}
