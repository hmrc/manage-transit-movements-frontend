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

import base.SpecBase
import play.api.libs.json.{JsArray, Json}

import java.time.LocalDateTime

class ArrivalsSpec extends SpecBase {

  "Arrivals" - {

    "must deserialize" in {

      val dateNow = LocalDateTime.now()

      val json = Json.obj(
        "retrievedArrivals" -> 1,
        "totalArrivals"     -> 2,
        "totalMatched"      -> 3,
        "arrivals" -> JsArray(
          Seq(
            Json.obj(
              "arrivalId"               -> 123,
              "created"                 -> dateNow,
              "updated"                 -> dateNow,
              "status"                  -> "GoodsReleased",
              "movementReferenceNumber" -> "mrn123"
            )
          )
        )
      )

      val expectedResult = Arrivals(
        1,
        2,
        Some(3),
        Seq(
          Arrival(
            ArrivalId(123),
            dateNow,
            dateNow,
            "GoodsReleased",
            "mrn123"
          )
        )
      )

      json.validate[Arrivals].asOpt.value mustEqual expectedResult
    }
  }
}
