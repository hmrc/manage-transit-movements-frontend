/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.format.DateTimeFormatter

import base.SpecBase
import generators.ModelGenerators
import models.referenceData.Movement
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.MustMatchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.Format

class MovementSpec extends SpecBase with MustMatchers with ModelGenerators with ScalaCheckPropertyChecks with NunjucksSupport {

  "Movement" - {

    "must deserialize from Json" in {
      forAll(arbitrary[Movement]) {
        case movement @ Movement(date, time, movementReferenceNumber) => {

          val json = Json.obj(
            "messages" -> Json.arr(
              Json.obj(
                "date" -> Format.dateFormatted(date),
                "time" -> Format.timeFormatted(time)
              )),
            "movementReferenceNumber" -> movementReferenceNumber
          )

          json.as[Movement] mustEqual movement
        }
      }
    }
  }
}
