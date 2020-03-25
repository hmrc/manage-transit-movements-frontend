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

import java.time.{LocalDate, LocalTime, Year}

import models._
import models.referenceData.Movement
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
        year <- Gen.chooseNum(Year.MIN_VALUE, Year.MAX_VALUE)
      } yield LocalDate.of(year, month, day)
    }
  }

  implicit val arbitraryLocalTime: Arbitrary[LocalTime] = Arbitrary {
    for {
      hours   <- Gen.chooseNum(0, 23)
      minutes <- Gen.chooseNum(0, 59)
      seconds <- Gen.chooseNum(0, 59)
    } yield LocalTime.of(hours, minutes, seconds)
  }

  implicit val arbitraryMovement: Arbitrary[Movement] = {
    Arbitrary {
      for {
        date       <- arbitrary[LocalDate]
        time       <- arbitrary[LocalTime]
        mrn        <- arbitrary[String]
        traderName <- arbitrary[String]
        office     <- arbitrary[String]
        procedure  <- arbitrary[String]
      } yield Movement(date, time, mrn, traderName, office, procedure)
    }
  }

  implicit val arbitraryViewMovement: Arbitrary[ViewMovement] = {
    Arbitrary {
      for {
        date <- arbitrary[LocalDate]
        time <- arbitrary[LocalTime]
        mrn  <- arbitrary[String]
      } yield ViewMovement(date, time, mrn)
    }
  }

  implicit val arbitraryViewArrivalMovements: Arbitrary[ViewArrivalMovements] =
    Arbitrary {
      for {
        seqOfViewMovements <- listOfN(10, arbitrary[ViewMovement])
      } yield ViewArrivalMovements(seqOfViewMovements)
    }
}
