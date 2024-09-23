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
import generators.Generators
import models.referenceData.FunctionalErrorWithDesc
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

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

}
