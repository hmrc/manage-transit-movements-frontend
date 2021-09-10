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

import controllers.departure.routes
import generators.Generators
import models.Departure
import org.jsoup.nodes.Document
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsObject, Json}
import viewModels.{ViewDeparture, ViewDepartureMovements}
import views.behaviours.MovementsTableViewBehaviours

class ViewAllDeparturesViewSpec extends MovementsTableViewBehaviours("viewAllDepartures.njk") with Generators with ScalaCheckPropertyChecks {

  private val messageKeyPrefix: String = "viewDepartures"

  private val day1: LocalDateTime   = LocalDateTime.parse("2020-08-16 06:06:06", dateTimeFormat)
  private val day2: LocalDateTime   = LocalDateTime.parse("2020-08-15 05:05:05", dateTimeFormat)
  private val day3: LocalDateTime   = LocalDateTime.parse("2020-08-14 04:04:04", dateTimeFormat)
  private val day4: LocalDateTime   = LocalDateTime.parse("2020-08-13 03:03:03", dateTimeFormat)
  private val day5: LocalDateTime   = LocalDateTime.parse("2020-08-12 02:02:02", dateTimeFormat)
  private val day6_1: LocalDateTime = LocalDateTime.parse("2020-08-11 01:00:00", dateTimeFormat)
  private val day6_2: LocalDateTime = LocalDateTime.parse("2020-08-11 01:01:01", dateTimeFormat)

  private val departure1 = arbitrary[Departure].sample.value.copy(updated = day1)
  private val departure2 = arbitrary[Departure].sample.value.copy(updated = day2)
  private val departure3 = arbitrary[Departure].sample.value.copy(updated = day3)
  private val departure4 = arbitrary[Departure].sample.value.copy(updated = day4)
  private val departure5 = arbitrary[Departure].sample.value.copy(updated = day5)
  private val departure6 = arbitrary[Departure].sample.value.copy(updated = day6_1)
  private val departure7 = arbitrary[Departure].sample.value.copy(updated = day6_2)

  private val departures = Seq(departure1, departure2, departure3, departure4, departure5, departure6, departure7)

  private val viewMovements: Seq[ViewDeparture] = departures.map(ViewDeparture(_))

  private val formatToJson: JsObject = Json.toJsObject(ViewDepartureMovements.apply(viewMovements))(ViewDepartureMovements.writes(frontendAppConfig))

  private val doc: Document = renderDocument(formatToJson).futureValue

  behave like pageWithHeading(doc, messageKeyPrefix)

  behave like pageWithMovementSearch(
    doc = doc,
    id = "lrn",
    expectedText = "movement.search.departure.title"
  )

  behave like pageWithPagination(routes.ViewAllDeparturesController.onPageLoad(None).url)

  behave like pageWithMovementsData[ViewDeparture](
    doc = doc,
    viewMovements = viewMovements,
    messageKeyPrefix = messageKeyPrefix,
    refType = "lrn"
  )

  behave like pageWithLink(
    doc = doc,
    id = "make-departure-notification",
    expectedText = s"$messageKeyPrefix.makeDepartureNotification",
    expectedHref = frontendAppConfig.declareDepartureStartWithLRNUrl
  )

  behave like pageWithLink(
    doc = doc,
    id = "go-to-manage-transit-movements",
    expectedText = s"$messageKeyPrefix.goToManageTransitMovements",
    expectedHref = controllers.routes.WhatDoYouWantToDoController.onPageLoad().url
  )
}
