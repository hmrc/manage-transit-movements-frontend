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

import base.{FakeFrontendAppConfig, SingleViewSpec}
import generators.Generators
import models.Arrival
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsObject, Json}
import viewModels.{ViewArrival, ViewArrivalMovements}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.convert.ImplicitConversions._

class ViewArrivalsSpec extends SingleViewSpec("viewArrivals.njk") with Generators with ScalaCheckPropertyChecks {

  val frontendAppConfig = FakeFrontendAppConfig()

  val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  val day1   = LocalDateTime.parse("2020-08-16 06:06:06", dateTimeFormat)
  val day2   = LocalDateTime.parse("2020-08-15 05:05:05", dateTimeFormat)
  val day3   = LocalDateTime.parse("2020-08-14 04:04:04", dateTimeFormat)
  val day4   = LocalDateTime.parse("2020-08-13 03:03:03", dateTimeFormat)
  val day5   = LocalDateTime.parse("2020-08-12 02:02:02", dateTimeFormat)
  val day6_1 = LocalDateTime.parse("2020-08-11 01:00:00", dateTimeFormat)
  val day6_2 = LocalDateTime.parse("2020-08-11 01:01:01", dateTimeFormat)

  val arrival1 = arbitrary[Arrival].sample.value.copy(updated = day1)
  val arrival2 = arbitrary[Arrival].sample.value.copy(updated = day2)
  val arrival3 = arbitrary[Arrival].sample.value.copy(updated = day3)
  val arrival4 = arbitrary[Arrival].sample.value.copy(updated = day4)
  val arrival5 = arbitrary[Arrival].sample.value.copy(updated = day5)
  val arrival6 = arbitrary[Arrival].sample.value.copy(updated = day6_1)
  val arrival7 = arbitrary[Arrival].sample.value.copy(updated = day6_2)

  val arrivals       = Seq(arrival1, arrival2, arrival3, arrival4, arrival5, arrival6, arrival7)
  val sortedArrivals = Seq(arrival1, arrival2, arrival3, arrival4, arrival5, arrival7, arrival6)

  val viewMovements: Seq[ViewArrival] = sortedArrivals.map(
    (arrival: Arrival) => ViewArrival(arrival)(messages, frontendAppConfig)
  )

  val formatToJson: JsObject = Json.toJsObject(ViewArrivalMovements.apply(viewMovements))(ViewArrivalMovements.writes(frontendAppConfig))

  val doc: Document = renderDocument(formatToJson).futureValue

  "generate a heading for each unique day" in {

    val ls: Elements = doc.getElementsByAttributeValue("data-testrole", "movements-list_group-heading")

    ls.size() mustEqual 6

    ls.eq(0).text() mustBe "16 August 2020"
    ls.eq(1).text() mustBe "15 August 2020"
    ls.eq(2).text() mustBe "14 August 2020"
    ls.eq(3).text() mustBe "13 August 2020"
    ls.eq(4).text() mustBe "12 August 2020"
    ls.eq(5).text() mustBe "11 August 2020"
  }

  val rows: Elements = doc.getElementsByAttributeValue("role", "row")

  "generate a row for each departure" in {
    rows.size() mustEqual 7
  }

  "display rows in correct (sorted) order" in {
    rows.toList.zipWithIndex.forEach {
      x =>
        val cells: Elements = x._1.getElementsByAttributeValue("role", "cell")

        cells.get(0).text() mustBe "viewArrivalNotifications.table.updated " + (x._2 match {
          case 0 => "6:06am"
          case 1 => "5:05am"
          case 2 => "4:04am"
          case 3 => "3:03am"
          case 4 => "2:02am"
          case 5 => "1:01am"
          case 6 => "1:00am"
        })
    }
  }

  "display correct data in each row" in {
    rows.toList.zipWithIndex.forEach {
      x =>
        val cells: Elements = x._1.getElementsByAttributeValue("role", "cell")

        cells.get(1).text() mustBe s"viewArrivalNotifications.table.mrn ${viewMovements(x._2).movementReferenceNumber}"
        cells.get(2).text() mustBe s"viewArrivalNotifications.table.status ${viewMovements(x._2).status}"

        val actions = cells.get(3).getElementsByTag("a")
        actions.toList.zipWithIndex.forEach {
          y =>
            y._1.attr("id") mustBe s"${viewMovements(x._2).action(y._2).key}-${viewMovements(x._2).movementReferenceNumber}"
            y._1.attr("href") mustBe viewMovements(x._2).action(y._2).href
        }
    }
  }

}
