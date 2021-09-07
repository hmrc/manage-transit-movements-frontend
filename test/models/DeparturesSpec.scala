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

package models

import java.time.LocalDateTime

import base.SpecBase
import models.departure.DepartureStatus.DepartureSubmitted
import play.api.libs.json.{JsArray, Json}
import viewModels.DepartureStatus

class DeparturesSpec extends SpecBase {

  "Departures" - {

    "must deserialize" in {

      val dateNow = LocalDateTime.now()

      val json = Json.obj(
        "retrievedDepartures" -> 1,
        "totalDepartures"     -> 2,
        "totalMatched"        -> 3,
        "departures" -> JsArray(
          Seq(
            Json.obj(
              "arrivalId"       -> 123,
              "updated"         -> dateNow,
              "referenceNumber" -> "lrn123",
              "status"          -> DepartureSubmitted.toString
            )
          )
        )
      )

      val expectedResult = Departures(
        1,
        2,
        Some(3),
        Seq(
          Departure(
            DepartureId(123),
            dateNow,
            LocalReferenceNumber("lrn123"),
            DepartureSubmitted
          )
        )
      )

      json.validate[Departures].asOpt.value mustEqual expectedResult
    }
  }
}
