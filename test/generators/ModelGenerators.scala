/*
 * Copyright 2020 HM Revenue & Customs
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

package generators

import java.time.{Instant, LocalDate, LocalDateTime, LocalTime, ZoneOffset}

import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.listOfN
import org.scalacheck.{Arbitrary, Gen}
import viewModels.{ViewArrivalMovements, ViewMovement}

trait ModelGenerators {

  implicit val arbitrarylocalDate: Arbitrary[LocalDate] = {
    Arbitrary {
      for {
        day <- Gen.choose(1, 28)
        month <- Gen.chooseNum(
          LocalDate.MIN.getMonthValue,
          LocalDate.MAX.getMonthValue
        )
        year <- Gen.chooseNum(1111, 3000)
      } yield LocalDate.of(year, month, day)
    }
  }

  implicit val arbitraryLocalTime: Arbitrary[LocalTime] = Arbitrary {
    for {
      hours   <- Gen.chooseNum(0, 23)
      minutes <- Gen.chooseNum(0, 59)
    } yield LocalTime.of(hours, minutes)
  }

  def dateTimesBetween(min: LocalDateTime, max: LocalDateTime): Gen[LocalDateTime] = {
    def toMillis(date: LocalDateTime): Long =
      date.atZone(ZoneOffset.UTC).toInstant.toEpochMilli
    Gen.choose(toMillis(min), toMillis(max)).map {
      millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDateTime
    }
  }

  implicit lazy val arbitraryLocalDateTime: Arbitrary[LocalDateTime] = Arbitrary {
    dateTimesBetween(
      LocalDateTime.of(1900, 1, 1, 0, 0, 0),
      LocalDateTime.of(2100, 1, 1, 0, 0, 0)
    )
  }

  implicit val arbitraryArrival: Arbitrary[Arrival] = {
    Arbitrary {
      for {
        date   <- arbitrary[ArrivalDateTime]
        time   <- arbitrary[ArrivalDateTime]
        status <- arbitrary[String]
        mrn    <- arbitrary[String]
      } yield Arrival(date, time, status, mrn)
    }
  }

  implicit val arbitraryArrivalDateTime: Arbitrary[ArrivalDateTime] = {
    Arbitrary {
      for {
        dateTime <- arbitrary[LocalDateTime]
      } yield ArrivalDateTime(dateTime)
    }
  }

  implicit val arbitraryViewMovement: Arbitrary[ViewMovement] = {
    Arbitrary {
      for {
        date   <- arbitrary[LocalDate]
        time   <- arbitrary[LocalTime]
        status <- arbitrary[String]
        mrn    <- arbitrary[String]
      } yield ViewMovement(date, time, status, mrn)
    }
  }

  implicit val arbitraryViewArrivalMovements: Arbitrary[ViewArrivalMovements] =
    Arbitrary {
      for {
        seqOfViewMovements <- listOfN(10, arbitrary[ViewMovement])
      } yield ViewArrivalMovements(seqOfViewMovements)
    }
}
