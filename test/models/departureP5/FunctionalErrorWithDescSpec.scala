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
import cats.Order
import generators.Generators
import models.referenceData.FunctionalErrorWithDesc
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsValue, Json}

class FunctionalErrorWithDescSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "FunctionalError" - {

    "must deserialise" in {
      val json = Json.parse("""
            |    {
            |        "code": "12",
            |        "description": "Invalid MRN"
            |    }
            |""".stripMargin)

      json.as[FunctionalErrorWithDesc] `mustBe` FunctionalErrorWithDesc("12", "Invalid MRN")
    }
  }

  "serialize to JSON correctly" in {
    val functionalError = FunctionalErrorWithDesc(
      code = "ERR001",
      description = "Invalid data format"
    )

    val expectedJson: JsValue = Json.parse(
      """
        |{
        |  "code": "ERR001",
        |  "description": "Invalid data format"
        |}
        |""".stripMargin
    )

    val json = Json.toJson(functionalError)
    json mustEqual expectedJson
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
    val error1 = FunctionalErrorWithDesc("ERR001", "Invalid data format")
    val error2 = FunctionalErrorWithDesc("ERR002", "Missing field")

    Order[FunctionalErrorWithDesc].compare(error1, error2) must be(-1)
    Order[FunctionalErrorWithDesc].compare(error2, error1) must be(1)
    Order[FunctionalErrorWithDesc].compare(error1, error1) mustEqual 0
  }

}
