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

package models

import base.SpecBase
import cats.Order
import play.api.libs.json.Json

class IncidentCodeSpec extends SpecBase {

  "IncidentCode" - {

    "serialize to JSON correctly" in {
      val incidentCode = IncidentCode(
        code = "IC001",
        description = "Accident"
      )

      val expectedJson = Json.parse(
        """
          |{
          |  "code": "IC001",
          |  "description": "Accident"
          |}
          |""".stripMargin
      )

      val json = Json.toJson(incidentCode)
      json mustEqual expectedJson
    }

    "deserialize from JSON correctly" in {
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

      val result = json.as[IncidentCode]
      result mustEqual expectedIncidentCode
    }

    "correctly apply custom toString" in {
      val incidentCode = IncidentCode(
        code = "IC001",
        description = "Accident"
      )

      incidentCode.toString mustEqual "IC001 - Accident"
    }

    "order IncidentCode instances by code" in {
      val incidentCode1 = IncidentCode("IC001", "Accident")
      val incidentCode2 = IncidentCode("IC002", "Breakdown")

      Order[IncidentCode].compare(incidentCode1, incidentCode2) must be(-1)
      Order[IncidentCode].compare(incidentCode2, incidentCode1) must be(1)
      Order[IncidentCode].compare(incidentCode1, incidentCode1) mustEqual 0
    }
  }
}
