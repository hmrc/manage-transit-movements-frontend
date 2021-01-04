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

package views

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import base.ViewSpecBase
import generators.Generators
import models.Arrival
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsObject, Json}
import viewModels.{ViewArrivalMovements, ViewMovement}

class ViewArrivalsSpec extends ViewSpecBase with Generators with ScalaCheckPropertyChecks {

  "ViewArrivals" - {
    "generate list on correct order" in {
      val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

      val day1 = LocalDateTime.parse("2020-08-16 07:22:05", dateTimeFormat)
      val day2 = LocalDateTime.parse("2020-08-15 07:22:05", dateTimeFormat)
      val day3 = LocalDateTime.parse("2020-08-14 07:22:05", dateTimeFormat)
      val day4 = LocalDateTime.parse("2020-08-13 07:22:05", dateTimeFormat)
      val day5 = LocalDateTime.parse("2020-08-12 07:22:05", dateTimeFormat)
      val day6 = LocalDateTime.parse("2020-08-11 07:22:05", dateTimeFormat)

      val arrival1 = arbitrary[Arrival].sample.value.copy(updated = day1)
      val arrival2 = arbitrary[Arrival].sample.value.copy(updated = day2)
      val arrival3 = arbitrary[Arrival].sample.value.copy(updated = day3)
      val arrival4 = arbitrary[Arrival].sample.value.copy(updated = day4)
      val arrival5 = arbitrary[Arrival].sample.value.copy(updated = day5)
      val arrival6 = arbitrary[Arrival].sample.value.copy(updated = day6)

      val arrivals = Seq(arrival1, arrival2, arrival3, arrival4, arrival5, arrival6)

      val viewMovements: Seq[ViewMovement] = arrivals.map((arrival: Arrival) => ViewMovement(arrival)(messages, frontendAppConfig))

      val formatToJson: JsObject = Json.toJsObject(ViewArrivalMovements.apply(viewMovements))(ViewArrivalMovements.writes(frontendAppConfig))

      val doc: Document = renderDocument("viewArrivals.njk", formatToJson).futureValue

      doc.getElementsByClass("govuk-heading-m").size() mustEqual 6
      val ls: Elements = doc.getElementsByClass("govuk-heading-m")
      ls.eq(0).text() mustBe "16 August 2020"
      ls.eq(1).text() mustBe "15 August 2020"
      ls.eq(2).text() mustBe "14 August 2020"
      ls.eq(3).text() mustBe "13 August 2020"
      ls.eq(4).text() mustBe "12 August 2020"
      ls.eq(5).text() mustBe "11 August 2020"
    }
  }
}
