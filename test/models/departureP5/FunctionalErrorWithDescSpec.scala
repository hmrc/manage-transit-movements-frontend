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
import models.referenceData.FunctionalErrorWithDesc
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{Json, Reads}
import play.api.test.Helpers.running

class FunctionalErrorWithDescSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "FunctionalError" - {

    "must deserialise" - {
      "when phase-6 enabled" in {
        running(_.configure("feature-flags.phase-6-enabled" -> true)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]
            val json = Json.parse("""
                |    {
                |        "key": "12",
                |        "value": "Invalid MRN"
                |    }
                |""".stripMargin)

            implicit val reads: Reads[FunctionalErrorWithDesc] = FunctionalErrorWithDesc.reads(config)

            json.as[FunctionalErrorWithDesc] mustEqual FunctionalErrorWithDesc("12", "Invalid MRN")
        }

      }
      "when phase-6-disabled" in {
        running(_.configure("feature-flags.phase-6-enabled" -> false)) {
          app =>
            val config = app.injector.instanceOf[FrontendAppConfig]
            val json = Json.parse("""
                |    {
                |        "code": "12",
                |        "description": "Invalid MRN"
                |    }
                |""".stripMargin)

            implicit val reads: Reads[FunctionalErrorWithDesc] = FunctionalErrorWithDesc.reads(config)

            json.as[FunctionalErrorWithDesc] mustEqual FunctionalErrorWithDesc("12", "Invalid MRN")
        }

      }

    }
  }

  "correctly apply custom toString when description is non-empty" in {
    val functionalError = FunctionalErrorWithDesc(
      code = "ERR001",
      description = "Invalid data format"
    )

    functionalError.toString mustEqual "ERR001 - Invalid data format"
  }

  "correctly apply custom toString when description is empty" in {
    val functionalError = FunctionalErrorWithDesc(
      code = "ERR002",
      description = ""
    )

    functionalError.toString mustEqual "ERR002"
  }

  "order FunctionalErrorWithDesc instances by code" in {
    val unorderedErrors = Seq(
      FunctionalErrorWithDesc("ERR003", "Invalid field"),
      FunctionalErrorWithDesc("ERR001", "Invalid data format"),
      FunctionalErrorWithDesc("ERR002", "Missing field")
    )

    val orderedErrors = Seq(
      FunctionalErrorWithDesc("ERR001", "Invalid data format"),
      FunctionalErrorWithDesc("ERR002", "Missing field"),
      FunctionalErrorWithDesc("ERR003", "Invalid field")
    )

    val result = NonEmptySet
      .of(unorderedErrors.head, unorderedErrors.tail*)
      .toSortedSet
      .toList

    result.mustEqual(orderedErrors)
  }

}
