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

class NationalitySpec extends SpecBase {

  "Nationality" - {
    "deserialize from JSON correctly" - {
      "when phase-6 enabled" in {
        running(_.configure("feature-flags.phase-6-enabled" -> true)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]
            val json = Json.parse(
              """
                |{
                |  "key": "UK",
                |  "value": "United Kingdom"
                |}
                |""".stripMargin
            )

            val expectedNationality = Nationality(
              code = "UK",
              description = "United Kingdom"
            )

            implicit val reads: Reads[Nationality] = Nationality.reads(config)

            val result = json.as[Nationality]
            result mustEqual expectedNationality
        }
      }
      "when phase-6-disabled" in {
        running(_.configure("feature-flags.phase-6-enabled" -> false)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]
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

            implicit val reads: Reads[Nationality] = Nationality.reads(config)

            val result = json.as[Nationality]
            result mustEqual expectedNationality
        }
      }

    }

    "correctly apply custom toString" in {
      val nationality = Nationality(
        code = "UK",
        description = "United Kingdom"
      )

      nationality.toString mustEqual "United Kingdom - UK"
    }

    "order Nationality instances by description first, then code" in {
      val unorderedNationalities = Seq(
        Nationality("UK", "United Kingdom"),
        Nationality("FR", "France"),
        Nationality("UK", "France")
      )

      val orderedNationalities = Seq(
        Nationality("FR", "France"),
        Nationality("UK", "France"),
        Nationality("UK", "United Kingdom")
      )

      val result = NonEmptySet
        .of(unorderedNationalities.head, unorderedNationalities.tail*)
        .toSortedSet
        .toList

      result.mustBe(orderedNationalities)
    }
  }
}
