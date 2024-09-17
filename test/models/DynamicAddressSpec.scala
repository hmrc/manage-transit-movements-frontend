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

class DynamicAddressSpec extends SpecBase {

  "DynamicAddress" - {
    "serialize to JSON" in {
      val address = DynamicAddress("123 Main St", "London", Some("10001"))
      val expectedJson = Json.obj(
        "numberAndStreet" -> "123 Main St",
        "city"            -> "London",
        "postalCode"      -> "10001"
      )

      val json = Json.toJson(address)
      json mustBe expectedJson
    }

    "deserialize from JSON" in {
      val json = Json.obj(
        "numberAndStreet" -> "123 Main St",
        "city"            -> "London",
        "postalCode"      -> "10001"
      )

      val expectedAddress = DynamicAddress("123 Main St", "London", Some("10001"))

      json.validate[DynamicAddress] mustBe JsSuccess(expectedAddress)
    }

    "deserialize from JSON with missing postalCode" in {
      val json = Json.obj(
        "numberAndStreet" -> "123 Main St",
        "city"            -> "London"
      )

      val expectedAddress = DynamicAddress("123 Main St", "London", None)

      json.validate[DynamicAddress] mustBe JsSuccess(expectedAddress)
    }

    "return the correct string representation with postalCode" in {
      val address = DynamicAddress("123 Main St", "London", Some("10001"))

      address.toString mustBe "123 Main St<br>London<br>10001"
    }

    "return the correct string representation without postalCode" in {
      val address = DynamicAddress("123 Main St", "London", None)

      address.toString mustBe "123 Main St<br>London"
    }

  }

}
