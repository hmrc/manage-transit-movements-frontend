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

package viewModels

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime}

import base.SpecBase
import generators.{Generators, ModelGenerators}
import models.Movement
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.MustMatchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import uk.gov.hmrc.viewmodels.NunjucksSupport

class ViewArrivalMovementsSpec
  extends SpecBase
    with MustMatchers
    with ModelGenerators
    with Generators
    with ScalaCheckPropertyChecks
    with NunjucksSupport {

  "apply groups Movements by dates and reformat date to 'd MMMM yyyy'" in {

    val localDateToday = LocalDate.now()
    val localDateYesterday = LocalDate.now().minusDays(1)
    val localTime = LocalTime.now()

    val movementsGen: LocalDate => Gen[Seq[Movement]] =
      date =>
        seqWithMaxLength(10) {
          Arbitrary {
            arbitrary[Movement].map(_.copy(date = date, time = localTime))
          }
        }

    forAll(movementsGen(localDateToday), movementsGen(localDateYesterday)) {
      (todayMovements: Seq[Movement], yesterdayMovements: Seq[Movement]) =>

        val result: ViewArrivalMovements = ViewArrivalMovements(todayMovements ++ yesterdayMovements)

        result.dataRows(formatter(localDateToday)) mustEqual todayMovements
        result.dataRows(formatter(localDateYesterday)) mustEqual yesterdayMovements
    }
  }

  "apply ordering to Movements by descending time in the same date" in {
    val localDateToday = LocalDate.now

    val localTime = LocalTime.now
    val localTimeMinus1 = LocalTime.now.minusHours(1)
    val localTimeMinus2 = LocalTime.now.minusHours(2)

    val movementsGen: LocalTime => Arbitrary[Movement] = {
      time =>
        Arbitrary {
          arbitrary[Movement].map(_.copy(date = localDateToday, time = time))
        }
    }

    forAll(movementsGen(localTime).arbitrary, movementsGen(localTimeMinus1).arbitrary, movementsGen(localTimeMinus2).arbitrary) {
      (movement, movementMinus1, movementMinus2) =>

        val movementsInWrongOrder: Seq[Movement] = Seq(movementMinus1, movementMinus2, movement)
        val result: ViewArrivalMovements = ViewArrivalMovements(movementsInWrongOrder)

        val expectedResult: Seq[Movement] = Seq(movementMinus2, movementMinus1, movement)

        result.dataRows(formatter(localDateToday)) mustEqual expectedResult
    }
  }

  def formatter(date: LocalDate): String = {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    date.format(formatter)
  }

}
