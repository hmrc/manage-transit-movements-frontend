/*
 * Copyright 2023 HM Revenue & Customs
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

package models.departureP5

import base.SpecBase
import cats.data.NonEmptySet
import config.FrontendAppConfig
import generators.Generators
import models.referenceData.InvalidGuaranteeReason
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{Json, Reads}
import play.api.test.Helpers.running

class InvalidGuaranteeReasonSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "InvalidGuaranteeReason" - {

    "must deserialise" - {
      "when phase-6 enabled" in {
        running(_.configure("feature-flags.phase-6-enabled" -> true)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]
            val json = Json.parse("""
                |    {
                |        "key": "G02",
                |        "value": "Guarantee exists, but not valid"
                |    }
                |""".stripMargin)

            implicit val reads: Reads[InvalidGuaranteeReason] = InvalidGuaranteeReason.reads(config)

            json.as[InvalidGuaranteeReason] mustEqual InvalidGuaranteeReason("G02", "Guarantee exists, but not valid")
        }

      }
      "when phase-6-disabled" in {
        running(_.configure("feature-flags.phase-6-enabled" -> false)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]
            val json = Json.parse("""
                |    {
                |        "code": "G02",
                |        "description": "Guarantee exists, but not valid"
                |    }
                |""".stripMargin)

            implicit val reads: Reads[InvalidGuaranteeReason] = InvalidGuaranteeReason.reads(config)

            json.as[InvalidGuaranteeReason] mustEqual InvalidGuaranteeReason("G02", "Guarantee exists, but not valid")
        }

      }
    }

    "correctly apply custom toString when description is non-empty" in {
      val invalidGuaranteeReason = InvalidGuaranteeReason(
        code = "G002",
        description = "Insufficient funds"
      )

      invalidGuaranteeReason.toString mustEqual "G002 - Insufficient funds"
    }

    "correctly apply custom toString when description is empty" in {
      val invalidGuaranteeReason = InvalidGuaranteeReason(
        code = "G002",
        description = ""
      )

      invalidGuaranteeReason.toString mustEqual "G002"
    }

    "order InvalidGuaranteeReason instances by code" in {
      val unorderedReasons = Seq(
        InvalidGuaranteeReason("G003", "Invalid guarantee"),
        InvalidGuaranteeReason("G001", "Insufficient funds"),
        InvalidGuaranteeReason("G002", "Guarantee expired")
      )

      val orderedReasons = Seq(
        InvalidGuaranteeReason("G001", "Insufficient funds"),
        InvalidGuaranteeReason("G002", "Guarantee expired"),
        InvalidGuaranteeReason("G003", "Invalid guarantee")
      )

      val result = NonEmptySet
        .of(unorderedReasons.head, unorderedReasons.tail*)
        .toSortedSet
        .toList

      result.mustEqual(orderedReasons)
    }

  }

}
