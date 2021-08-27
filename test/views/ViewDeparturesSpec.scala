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

import base.SingleViewSpec
import base.FakeFrontendAppConfig
import generators.Generators
import models.Departure
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsObject, Json}
import viewModels.{ViewDeparture, ViewDepartureMovements}

class ViewDeparturesSpec extends SingleViewSpec("viewDepartures.njk") with Generators with ScalaCheckPropertyChecks {

  val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  val day1   = LocalDateTime.parse("2020-08-16 06:06:06", dateTimeFormat)
  val day2   = LocalDateTime.parse("2020-08-15 05:05:05", dateTimeFormat)
  val day3   = LocalDateTime.parse("2020-08-14 04:04:04", dateTimeFormat)
  val day4   = LocalDateTime.parse("2020-08-13 03:03:03", dateTimeFormat)
  val day5   = LocalDateTime.parse("2020-08-12 02:02:02", dateTimeFormat)
  val day6_1 = LocalDateTime.parse("2020-08-11 01:01:01", dateTimeFormat)
  val day6_2 = LocalDateTime.parse("2020-08-11 01:00:00", dateTimeFormat)

  val departure1 = arbitrary[Departure].sample.value.copy(updated = day1)
  val departure2 = arbitrary[Departure].sample.value.copy(updated = day2)
  val departure3 = arbitrary[Departure].sample.value.copy(updated = day3)
  val departure4 = arbitrary[Departure].sample.value.copy(updated = day4)
  val departure5 = arbitrary[Departure].sample.value.copy(updated = day5)
  val departure6 = arbitrary[Departure].sample.value.copy(updated = day6_1)
  val departure7 = arbitrary[Departure].sample.value.copy(updated = day6_2)

  val departures = Seq(departure1, departure2, departure3, departure4, departure5, departure6, departure7)

  val frontendAppConfig = FakeFrontendAppConfig()

  val viewMovements: Seq[ViewDeparture] = departures.map(
    (departure: Departure) => ViewDeparture(departure, frontendAppConfig)
  )

  val formatToJson: JsObject = Json.toJsObject(ViewDepartureMovements.apply(viewMovements))(ViewDepartureMovements.writes(frontendAppConfig))

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

  "generate a row for each departure" in {
    val ls: Elements = doc.getElementsByAttributeValue("role", "row")

    ls.size() mustEqual 7
  }

}
