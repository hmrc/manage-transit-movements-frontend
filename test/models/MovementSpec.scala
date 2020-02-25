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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.MustMatchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.viewmodels.NunjucksSupport

class MovementSpec
    extends SpecBase
    with MustMatchers
    with ModelGenerators
    with ScalaCheckPropertyChecks
    with NunjucksSupport {

  "Movement" - {

    "must serialise to Json" in {

      forAll(arbitrary[Movement]) { movement =>
        val time  = movement.time.format(DateTimeFormatter.ofPattern("h:mma")).toLowerCase
        val expectedJson = Json.obj(
          "updated" -> time,
          "mrn" -> movement.movementReferenceNumber,
          "traderName" -> movement.traderName,
          "office" -> s"${movement.presentationOfficeName} (${movement.presentationOfficeId})",
          "procedure" -> movement.procedure,
          "actions" -> Seq("history"),
          "status" -> "Arrival notification sent"
        )

        Json.toJson(movement) mustBe expectedJson
      }
    }

    "must deserialize from Json" in {
      forAll(arbitrary[Movement]) {
        case movement @ Movement(
              date,
              time,
              movementReferenceNumber,
              traderName,
              presentationOfficeId,
              presentationOfficeName,
              procedure
            ) => {

          val json = Json.obj(
            "date" -> date,
            "time" -> time,
            "message" -> Json.obj(
              "movementReferenceNumber" -> movementReferenceNumber,
              "trader" -> Json.obj("name" -> traderName),
              "presentationOfficeId" -> presentationOfficeId,
              "presentationOfficeName" -> presentationOfficeName,
              "procedure" -> procedure
            )
          )

          json.asOpt[Movement].value mustEqual movement
        }
      }
    }
  }
}
