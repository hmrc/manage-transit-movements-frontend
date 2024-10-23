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
import play.api.libs.json.{JsValue, Json}

class IdentificationTypeSpec extends SpecBase {

  "IdentificationType" - {

    "serialize to JSON correctly" in {
      val identificationType = IdentificationType(
        `type` = "ID001",
        description = "Id"
      )

      val expectedJson: JsValue = Json.parse(
        """
          |{
          |  "type": "ID001",
          |  "description": "Id"
          |}
          |""".stripMargin
      )

      val json = Json.toJson(identificationType)
      json mustEqual expectedJson
    }

    "deserialize from JSON correctly" in {
      val json: JsValue = Json.parse(
        """
          |{
          |  "type": "ID001",
          |  "description": "Id"
          |}
          |""".stripMargin
      )

      val expectedIdentificationType = IdentificationType(
        `type` = "ID001",
        description = "Id"
      )

      val result = json.as[IdentificationType]
      result mustEqual expectedIdentificationType
    }

    "correctly apply custom toString" in {
      val identificationType = IdentificationType(
        `type` = "ID001",
        description = "Id"
      )

      identificationType.toString mustEqual "Id - ID001"
    }

    "order IdentificationType instances by description and type" in {
      val identificationType1 = IdentificationType("ID001", "Id")
      val identificationType2 = IdentificationType("ID002", "Ttt")
      val identificationType3 = IdentificationType("ID003", "Id")

      // Order by description first, then by type
      Order[IdentificationType].compare(identificationType1, identificationType2) < 0 must be(true)
      Order[IdentificationType].compare(identificationType2, identificationType1) > 0 must be(true)
      Order[IdentificationType].compare(identificationType1, identificationType3) < 0 must be(true) // "ID001" < "ID003" when descriptions are equal
      Order[IdentificationType].compare(identificationType1, identificationType1) mustEqual 0
    }
  }
}
