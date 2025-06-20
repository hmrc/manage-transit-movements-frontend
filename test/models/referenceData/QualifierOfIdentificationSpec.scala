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
import play.api.libs.json.{JsValue, Json, Reads}
import play.api.test.Helpers.running

class QualifierOfIdentificationSpec extends SpecBase {

  "QualifierOfIdentification" - {

    "deserialize from JSON correctly" - {

      "when phase-6 enabled" in {
        running(_.configure("feature-flags.phase-6-enabled" -> true)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]
            val json: JsValue = Json.parse(
              """
                |{
                |  "key": "Q1",
                |  "value": "Primary Qualifier"
                |}
                |""".stripMargin
            )

            val expectedQualifier = QualifierOfIdentification(
              qualifier = "Q1",
              description = "Primary Qualifier"
            )

            implicit val reads: Reads[QualifierOfIdentification] = QualifierOfIdentification.reads(config)

            val result = json.as[QualifierOfIdentification]
            result mustEqual expectedQualifier
        }
      }
      "when phase-6 disabled" in {
        running(_.configure("feature-flags.phase-6-enabled" -> false)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]
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

            implicit val reads: Reads[QualifierOfIdentification] = QualifierOfIdentification.reads(config)

            val result = json.as[QualifierOfIdentification]
            result mustEqual expectedQualifier
        }
      }

    }

    "correctly apply custom toString" in {
      val qualifier = QualifierOfIdentification(
        qualifier = "Q1",
        description = "Primary Qualifier"
      )

      qualifier.toString mustEqual "Primary Qualifier"
    }

    "order QualifierOfIdentification instances by qualifier" in {
      val unorderedQualifiers = Seq(
        QualifierOfIdentification("Q3", "Different Qualifier"),
        QualifierOfIdentification("Q1", "First Qualifier"),
        QualifierOfIdentification("Q2", "Second Qualifier")
      )

      val orderedQualifiers = Seq(
        QualifierOfIdentification("Q1", "First Qualifier"),
        QualifierOfIdentification("Q2", "Second Qualifier"),
        QualifierOfIdentification("Q3", "Different Qualifier")
      )

      val result = NonEmptySet
        .of(unorderedQualifiers.head, unorderedQualifiers.tail*)
        .toSortedSet
        .toList

      result.mustBe(orderedQualifiers)
    }
  }
}
