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

class NationalitySpec extends SpecBase {

  "Nationality" - {

    "serialize to JSON correctly" in {
      val nationality = Nationality(
        code = "UK",
        description = "United Kingdom"
      )

      val expectedJson = Json.parse(
        """
          |{
          |  "code": "UK",
          |  "description": "United Kingdom"
          |}
          |""".stripMargin
      )

      val json = Json.toJson(nationality)
      json mustEqual expectedJson
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        """
          |{
          |  "code": "UK",
          |  "description": "United Kingdom"
          |}
          |""".stripMargin
      )

      val expectedNationality = Nationality(
        code = "UK",
        description = "United Kingdom"
      )

      val result = json.as[Nationality]
      result mustEqual expectedNationality
    }

    "correctly apply custom toString" in {
      val nationality = Nationality(
        code = "UK",
        description = "United Kingdom"
      )

      nationality.toString mustEqual "United Kingdom - UK"
    }

    "order Nationality instances by description first, then code" in {
      val nationality1 = Nationality("UK", "United Kingdom")
      val nationality2 = Nationality("FR", "France")
      val nationality3 = Nationality("UK", "France")

      // Order by description first, then by code
      Order[Nationality].compare(nationality1, nationality2) > 0 must be(true) // "United Kingdom" > "France" ;) :D :)
      Order[Nationality].compare(nationality2, nationality1) < 0 must be(true)
      Order[Nationality].compare(nationality2, nationality3) < 0 must be(true) // Same description, different code
    }
  }
}
