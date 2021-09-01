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

import models.Departure
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.{JsObject, Json}
import viewModels.{ViewDeparture, ViewDepartureMovements}
import views.behaviours.ViewMovementsBehaviours

import java.time.LocalDateTime

class ViewDeparturesSpec extends ViewMovementsBehaviours[ViewDeparture]("viewDepartures.njk") {

  override val day6_1: LocalDateTime = LocalDateTime.parse("2020-08-11 01:00:00", dateTimeFormat)
  override val day6_2: LocalDateTime = LocalDateTime.parse("2020-08-11 01:01:01", dateTimeFormat)

  private val departure1 = arbitrary[Departure].sample.value.copy(updated = day1)
  private val departure2 = arbitrary[Departure].sample.value.copy(updated = day2)
  private val departure3 = arbitrary[Departure].sample.value.copy(updated = day3)
  private val departure4 = arbitrary[Departure].sample.value.copy(updated = day4)
  private val departure5 = arbitrary[Departure].sample.value.copy(updated = day5)
  private val departure6 = arbitrary[Departure].sample.value.copy(updated = day6_1)
  private val departure7 = arbitrary[Departure].sample.value.copy(updated = day6_2)

  private val departures = Seq(departure1, departure2, departure3, departure4, departure5, departure6, departure7)

  override val viewMovements: Seq[ViewDeparture] = departures.map(ViewDeparture(_))

  override val formatToJson: JsObject = Json.toJsObject(ViewDepartureMovements.apply(viewMovements))(ViewDepartureMovements.writes(frontendAppConfig))

  override val messageKeyPrefix: String = "viewDepartures"

  override val refType: String = "lrn"

  behave like pageWithMovementsData()

}
