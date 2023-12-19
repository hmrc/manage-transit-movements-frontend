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

package models.referenceData

import base.SpecBase
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class CustomsOfficeSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "CustomsOffice" - {

    "nameAndCode must return correct string" in {
      CustomsOffice("GB00006", "BOSTON", None).nameAndCode mustBe "BOSTON (GB00006)"
    }

    "must deserialise" - {
      "when there is a customs office" - {
        "with all data" in {
          val json = Json.parse("""
              |    {
              |        "id": "GB00006",
              |        "name": "BOSTON",
              |        "phoneNumber": "01234567890"
              |    }
              |""".stripMargin)

          json.as[CustomsOffice] mustBe CustomsOffice("GB00006", "BOSTON", Some("01234567890"))
        }
        "without a phone number" in {
          val json = Json.parse("""
              |    {
              |        "id": "GB00006",
              |        "name": "BOSTON"
              |    }
              |""".stripMargin)

          json.as[CustomsOffice] mustBe CustomsOffice("GB00006", "BOSTON", None)
        }
      }
    }
  }
}
