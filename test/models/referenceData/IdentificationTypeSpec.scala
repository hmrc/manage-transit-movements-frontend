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

class IdentificationTypeSpec extends SpecBase {

  "IdentificationType" - {
    "deserialize from JSON correctly " - {
      "when phase-6 enabled" in {
        running(_.configure("feature-flags.phase-6-enabled" -> true)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]

            val json: JsValue = Json.parse(
              """
                |{
                |  "key": "ID001",
                |  "value": "Id"
                |}
                |""".stripMargin
            )

            val expectedIdentificationType = IdentificationType(
              `type` = "ID001",
              description = "Id"
            )

            implicit val reads: Reads[IdentificationType] = IdentificationType.reads(config)

            val result = json.as[IdentificationType]
            result mustEqual expectedIdentificationType

        }

      }
      "when phase-6 disabled" in {
        running(_.configure("feature-flags.phase-6-enabled" -> false)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]

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

            implicit val reads: Reads[IdentificationType] = IdentificationType.reads(config)

            val result = json.as[IdentificationType]
            result mustEqual expectedIdentificationType

        }

      }

    }

    "correctly apply custom toString" in {
      val identificationType = IdentificationType(
        `type` = "ID001",
        description = "Id"
      )

      identificationType.toString mustEqual "Id - ID001"
    }

    "order IdentificationType instances by description and type" in {
      val unorderedIdentifications = Seq(
        IdentificationType("ID001", "Id"),
        IdentificationType("ID002", "Ttt"),
        IdentificationType("ID003", "Id")
      )

      val orderedIdentifications = Seq(
        IdentificationType("ID001", "Id"),
        IdentificationType("ID003", "Id"),
        IdentificationType("ID002", "Ttt")
      )

      val result = NonEmptySet
        .of(unorderedIdentifications.head, unorderedIdentifications.tail*)
        .toSortedSet
        .toList

      result.mustBe(orderedIdentifications)
    }
  }
}
