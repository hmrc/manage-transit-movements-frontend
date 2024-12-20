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
import play.api.libs.json.{JsValue, Json}

class ControlTypeSpec extends SpecBase {

  "ControlType" - {

    "serialize to JSON correctly" in {
      val controlType = ControlType(
        code = "CT001",
        description = "Customs Check"
      )

      val expectedJson: JsValue = Json.parse(
        """
          |{
          |  "code": "CT001",
          |  "description": "Customs Check"
          |}
          |""".stripMargin
      )

      val json = Json.toJson(controlType)
      json mustEqual expectedJson
    }

    "deserialize from JSON correctly" in {
      val json: JsValue = Json.parse(
        """
          |{
          |  "code": "CT001",
          |  "description": "Customs Check"
          |}
          |""".stripMargin
      )

      val expectedControlType = ControlType(
        code = "CT001",
        description = "Customs Check"
      )

      val result = json.as[ControlType]
      result mustEqual expectedControlType
    }

    "correctly apply custom toString when description is non-empty" in {
      val controlType = ControlType(
        code = "CT001",
        description = "Customs Check"
      )

      controlType.toString mustEqual "CT001 - Customs Check"
    }

    "correctly apply custom toString when description is empty" in {
      val controlType = ControlType(
        code = "CT002",
        description = ""
      )

      controlType.toString mustEqual "CT002"
    }

    "order ControlType instances by code" in {
      val unorderedControls = Seq(
        ControlType("CT003", "Nature of Goods"),
        ControlType("CT001", "Customs Check"),
        ControlType("CT002", "Security Check")
      )

      val orderedControls = Seq(
        ControlType("CT001", "Customs Check"),
        ControlType("CT002", "Security Check"),
        ControlType("CT003", "Nature of Goods")
      )

      val result = NonEmptySet
        .of(unorderedControls.head, unorderedControls.tail*)
        .toSortedSet
        .toList

      result.mustBe(orderedControls)
    }
  }
}
