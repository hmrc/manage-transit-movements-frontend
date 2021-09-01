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

import models.Arrival
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.{JsObject, Json}
import viewModels.{ViewArrival, ViewArrivalMovements}
import views.behaviours.ViewMovementsBehaviours

import java.time.LocalDateTime

class ViewArrivalsSpec extends ViewMovementsBehaviours[ViewArrival]("viewArrivals.njk") {

  override val day6_1: LocalDateTime = LocalDateTime.parse("2020-08-11 01:01:01", dateTimeFormat)
  override val day6_2: LocalDateTime = LocalDateTime.parse("2020-08-11 01:00:00", dateTimeFormat)

  private val arrival1 = arbitrary[Arrival].sample.value.copy(updated = day1)
  private val arrival2 = arbitrary[Arrival].sample.value.copy(updated = day2)
  private val arrival3 = arbitrary[Arrival].sample.value.copy(updated = day3)
  private val arrival4 = arbitrary[Arrival].sample.value.copy(updated = day4)
  private val arrival5 = arbitrary[Arrival].sample.value.copy(updated = day5)
  private val arrival6 = arbitrary[Arrival].sample.value.copy(updated = day6_1)
  private val arrival7 = arbitrary[Arrival].sample.value.copy(updated = day6_2)

  private val arrivals = Seq(arrival1, arrival2, arrival3, arrival4, arrival5, arrival6, arrival7)

  override val viewMovements: Seq[ViewArrival] = arrivals.map(ViewArrival(_))

  override val formatToJson: JsObject = Json.toJsObject(ViewArrivalMovements.apply(viewMovements))(ViewArrivalMovements.writes(frontendAppConfig))

  override val messageKeyPrefix: String = "viewArrivalNotifications"

  override val refType: String = "mrn"

  behave like pageWithMovementsData()

}
