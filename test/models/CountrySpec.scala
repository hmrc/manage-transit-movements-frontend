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
import play.api.libs.json.{JsSuccess, Json}

class CountrySpec extends SpecBase {

  "Country" - {

    "serialize to JSON" in {
      val country = Country("UK", "United Kingdom")
      val expectedJson = Json.obj(
        "code"        -> "UK",
        "description" -> "United Kingdom"
      )

      val json = Json.toJson(country)
      json mustBe expectedJson
    }

    "deserialize from JSON" in {
      val json = Json.obj(
        "code"        -> "UK",
        "description" -> "United Kingdom"
      )

      val expectedCountry = Country("UK", "United Kingdom")
      json.validate[Country] mustBe JsSuccess(expectedCountry)
    }

    "return the correct string representation" in {
      val country = Country("UK", "United Kingdom")
      country.toString mustBe "United Kingdom - UK"
    }
  }
}
