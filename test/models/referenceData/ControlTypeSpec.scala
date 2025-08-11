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
import org.mockito.Mockito.when
import play.api.libs.json.{JsValue, Json, Reads}

class ControlTypeSpec extends SpecBase {

  private val mockFrontendAppConfig = mock[FrontendAppConfig]

  "ControlType" - {

    "deserialize from JSON correctly" - {
      "when phase-6 " in {
        when(mockFrontendAppConfig.phase6Enabled).thenReturn(true)
        val json: JsValue = Json.parse(
          """
                |{
                |  "key": "CT001",
                |  "value": "Customs Check"
                |}
                |""".stripMargin
        )

        val expectedControlType = ControlType(
          code = "CT001",
          description = "Customs Check"
        )

        implicit val reads: Reads[ControlType] = ControlType.reads(mockFrontendAppConfig)

        val result = json.as[ControlType]
        result mustEqual expectedControlType
      }

      "when phase-5" in {
        when(mockFrontendAppConfig.phase6Enabled).thenReturn(false)
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

        implicit val reads: Reads[ControlType] = ControlType.reads(mockFrontendAppConfig)

        val result = json.as[ControlType]
        result mustEqual expectedControlType
      }
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

      result mustEqual orderedControls
    }
  }
}
