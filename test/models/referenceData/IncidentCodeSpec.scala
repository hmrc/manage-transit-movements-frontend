/*
 * Copyright 2024 HM Revenue & Customs
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

package models.referenceData

import base.SpecBase
import cats.data.NonEmptySet
import config.FrontendAppConfig
import play.api.libs.json.{Json, Reads}
import play.api.test.Helpers.running

class IncidentCodeSpec extends SpecBase {

  "IncidentCode" - {

    "deserialize from JSON correctly" - {
      "when phase-6 enabled" in {
        running(_.configure("feature-flags.phase-6-enabled" -> true)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]

            val json = Json.parse(
              """
                |{
                |  "key": "IC001",
                |  "value": "Accident"
                |}
                |""".stripMargin
            )

            val expectedIncidentCode = IncidentCode(
              code = "IC001",
              description = "Accident"
            )

            implicit val reads: Reads[IncidentCode] = IncidentCode.reads(config)

            val result = json.as[IncidentCode]
            result mustEqual expectedIncidentCode
        }

      }
      "when phase-6-disabled" in {
        running(_.configure("feature-flags.phase-6-enabled" -> false)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]

            val json = Json.parse(
              """
                |{
                |  "code": "IC001",
                |  "description": "Accident"
                |}
                |""".stripMargin
            )

            val expectedIncidentCode = IncidentCode(
              code = "IC001",
              description = "Accident"
            )

            implicit val reads: Reads[IncidentCode] = IncidentCode.reads(config)

            val result = json.as[IncidentCode]
            result mustEqual expectedIncidentCode
        }

      }

    }

    "correctly apply custom toString" in {
      val incidentCode = IncidentCode(
        code = "IC001",
        description = "Accident"
      )

      incidentCode.toString mustEqual "IC001 - Accident"
    }

    "order IncidentCode instances by code" in {
      val unorderedIncidents = Seq(
        IncidentCode("IC002", "Breakdown"),
        IncidentCode("IC003", "Id"),
        IncidentCode("IC001", "Accident")
      )

      val orderedIncidents = Seq(
        IncidentCode("IC001", "Accident"),
        IncidentCode("IC002", "Breakdown"),
        IncidentCode("IC003", "Id")
      )

      val result = NonEmptySet
        .of(unorderedIncidents.head, unorderedIncidents.tail*)
        .toSortedSet
        .toList

      result.mustBe(orderedIncidents)
    }
  }
}
