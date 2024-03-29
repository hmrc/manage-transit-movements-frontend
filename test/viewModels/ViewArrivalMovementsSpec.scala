/*
 * Copyright 2023 HM Revenue & Customs
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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.{LocalDate, LocalTime}

class ViewArrivalMovementsSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "apply groups Movements by dates and reformat date to 'd MMMM yyyy'" in {

    val localDateToday     = LocalDate.now()
    val localDateYesterday = LocalDate.now().minusDays(1)
    val localTime          = LocalTime.now()

    val movementsGen: LocalDate => Gen[Seq[ViewArrival]] =
      date =>
        seqWithMaxLength(1) {
          Arbitrary {
            arbitrary[ViewArrival].map(
              _.copy(updatedDate = date, updatedTime = localTime)
            )
          }
        }

    forAll(movementsGen(localDateToday).suchThat(_.nonEmpty), movementsGen(localDateYesterday).suchThat(_.nonEmpty)) {
      (todayMovements: Seq[ViewArrival], yesterdayMovements: Seq[ViewArrival]) =>
        val result: ViewArrivalMovements =
          ViewArrivalMovements(todayMovements ++ yesterdayMovements)

        result.dataRows(0)._2 mustEqual todayMovements
        result.dataRows(1)._2 mustEqual yesterdayMovements
    }
  }

  "apply ordering to Movements by ascending time in the same date" in {
    val localDateToday = LocalDate.now

    val localTime       = LocalTime.now
    val localTimeMinus1 = LocalTime.now.minusHours(1)
    val localTimeMinus2 = LocalTime.now.minusHours(2)

    val movementsGen: LocalTime => Arbitrary[ViewArrival] = {
      time =>
        Arbitrary {
          arbitrary[ViewArrival].map(
            _.copy(updatedDate = localDateToday, updatedTime = time)
          )
        }
    }

    forAll(
      movementsGen(localTime).arbitrary,
      movementsGen(localTimeMinus1).arbitrary,
      movementsGen(localTimeMinus2).arbitrary
    ) {
      (movement, movementMinus1, movementMinus2) =>
        val movementsInWrongOrder: Seq[ViewArrival] =
          Seq(movementMinus1, movementMinus2, movement)
        val result: ViewArrivalMovements =
          ViewArrivalMovements(movementsInWrongOrder)

        val expectedResult: Seq[ViewArrival] =
          Seq(movement, movementMinus1, movementMinus2)
        result.dataRows.head._2 mustEqual expectedResult
    }
  }

}
