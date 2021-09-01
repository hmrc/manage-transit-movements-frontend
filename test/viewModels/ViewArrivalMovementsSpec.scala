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

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalTime}

import base.SpecBase
import config.FrontendAppConfig
import generators.Generators
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.viewmodels.NunjucksSupport

class ViewArrivalMovementsSpec extends SpecBase with Generators with ScalaCheckPropertyChecks with NunjucksSupport {

  "apply groups Movements by dates and reformat date to 'd MMMM yyyy'" in {

    val localDateToday     = LocalDate.now()
    val localDateYesterday = LocalDate.now().minusDays(1)
    val localTime          = LocalTime.now()

    val movementsGen: LocalDate => Gen[Seq[ViewArrival]] =
      date =>
        seqWithMaxLength(1) {
          Arbitrary {
            arbitrary[ViewArrival].map(
              _.copy(date = date, time = localTime)
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
            _.copy(date = localDateToday, time = time)
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

  def formatter(date: LocalDate): String = {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    date.format(formatter)
  }

  "Json writes" - {

    "adds the declareArrivalNotificationUrl from FrontendAppConfig" in {
      val testUrl = "declareArrivalNotificationUrl"

      implicit val mockFrontendAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
      when(mockFrontendAppConfig.declareArrivalNotificationStartUrl).thenReturn(testUrl)

      forAll(arbitrary[ViewArrivalMovements]) {
        viewArrivalMovements =>
          val testJson: JsValue = Json.toJson(viewArrivalMovements)

          val result = (testJson \ "declareArrivalNotificationUrl").validate[String].asOpt.value

          result mustBe testUrl
      }
    }

    "adds the homePageUrl" in {

      implicit val mockFrontendAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
      when(mockFrontendAppConfig.declareArrivalNotificationStartUrl).thenReturn("")

      forAll(arbitrary[ViewArrivalMovements]) {
        viewArrivalMovements =>
          val testJson: JsValue = Json.toJson(viewArrivalMovements)

          val result = (testJson \ "homePageUrl").validate[String].asOpt.value

          result mustBe controllers.routes.WhatDoYouWantToDoController.onPageLoad().url
      }
    }

  }

}
