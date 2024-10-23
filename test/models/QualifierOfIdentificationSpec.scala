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

class QualifierOfIdentificationSpec extends SpecBase {

  "QualifierOfIdentification" - {

    "serialize to JSON correctly" in {
      val qualifier = QualifierOfIdentification(
        qualifier = "Q1",
        description = "Primary Qualifier"
      )

      val expectedJson: JsValue = Json.parse(
        """
          |{
          |  "qualifier": "Q1",
          |  "description": "Primary Qualifier"
          |}
          |""".stripMargin
      )

      val json = Json.toJson(qualifier)
      json mustEqual expectedJson
    }

    "deserialize from JSON correctly" in {
      val json: JsValue = Json.parse(
        """
          |{
          |  "qualifier": "Q1",
          |  "description": "Primary Qualifier"
          |}
          |""".stripMargin
      )

      val expectedQualifier = QualifierOfIdentification(
        qualifier = "Q1",
        description = "Primary Qualifier"
      )

      val result = json.as[QualifierOfIdentification]
      result mustEqual expectedQualifier
    }

    "correctly apply custom toString" in {
      val qualifier = QualifierOfIdentification(
        qualifier = "Q1",
        description = "Primary Qualifier"
      )

      qualifier.toString mustEqual "Primary Qualifier"
    }

    "order QualifierOfIdentification instances by qualifier" in {
      val qualifier1 = QualifierOfIdentification("Q1", "First Qualifier")
      val qualifier2 = QualifierOfIdentification("Q2", "Second Qualifier")
      val qualifier3 = QualifierOfIdentification("Q1", "Different Qualifier")

      Order[QualifierOfIdentification].compare(qualifier1, qualifier2) < 0 must be(true) // "Q1" < "Q2"
      Order[QualifierOfIdentification].compare(qualifier2, qualifier1) > 0 must be(true)
      Order[QualifierOfIdentification].compare(qualifier1, qualifier3) mustEqual 0 // Same qualifier, different description
    }
  }
}
