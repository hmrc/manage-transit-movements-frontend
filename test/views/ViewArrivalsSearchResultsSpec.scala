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

package views

import java.time.LocalDateTime

import generators.Generators
import models.Arrival
import org.jsoup.nodes.Document
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsObject, Json}
import viewModels.{ViewArrival, ViewArrivalMovements}
import views.behaviours.MovementsTableViewBehaviours

class ViewArrivalsSearchResultsSpec extends MovementsTableViewBehaviours("viewArrivalsSearchResults.njk") with Generators with ScalaCheckPropertyChecks {

  private val messageKeyPrefix: String = "viewArrivalNotifications"

  private val day1: LocalDateTime   = LocalDateTime.parse("2020-08-16 06:06:06", dateTimeFormat)
  private val day2: LocalDateTime   = LocalDateTime.parse("2020-08-15 05:05:05", dateTimeFormat)
  private val day3: LocalDateTime   = LocalDateTime.parse("2020-08-14 04:04:04", dateTimeFormat)
  private val day4: LocalDateTime   = LocalDateTime.parse("2020-08-13 03:03:03", dateTimeFormat)
  private val day5: LocalDateTime   = LocalDateTime.parse("2020-08-12 02:02:02", dateTimeFormat)
  private val day6_1: LocalDateTime = LocalDateTime.parse("2020-08-11 01:01:01", dateTimeFormat)
  private val day6_2: LocalDateTime = LocalDateTime.parse("2020-08-11 01:00:00", dateTimeFormat)

  private val arrival1 = arbitrary[Arrival].sample.value.copy(updated = day1)
  private val arrival2 = arbitrary[Arrival].sample.value.copy(updated = day2)
  private val arrival3 = arbitrary[Arrival].sample.value.copy(updated = day3)
  private val arrival4 = arbitrary[Arrival].sample.value.copy(updated = day4)
  private val arrival5 = arbitrary[Arrival].sample.value.copy(updated = day5)
  private val arrival6 = arbitrary[Arrival].sample.value.copy(updated = day6_1)
  private val arrival7 = arbitrary[Arrival].sample.value.copy(updated = day6_2)

  private val arrivals = Seq(arrival1, arrival2, arrival3, arrival4, arrival5, arrival6, arrival7)

  private val viewMovements: Seq[ViewArrival] = arrivals.map(ViewArrival(_))

  private val formatToJson: JsObject = Json.toJsObject(ViewArrivalMovements.apply(viewMovements))(ViewArrivalMovements.writes(frontendAppConfig))

  private val doc: Document = renderDocument(formatToJson).futureValue

  behave like pageWithHeading(doc, messageKeyPrefix)

  behave like pageWithMovementSearch(
    doc = doc,
    id = "mrn",
    expectedText = "movement.search.title"
  )

  behave like pageWithMovementsData[ViewArrival](
    doc = doc,
    viewMovements = viewMovements,
    messageKeyPrefix = messageKeyPrefix,
    refType = "mrn"
  )

  behave like pageWithLink(
    doc = doc,
    id = "go-to-view-all-movements",
    expectedText = "viewAllMovements.link.title",
    expectedHref = controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url
  )

}
