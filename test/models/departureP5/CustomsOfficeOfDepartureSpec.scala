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

package models.departureP5

import base.SpecBase
import play.api.libs.json.{JsSuccess, Json}

class CustomsOfficeOfDepartureSpec extends SpecBase {

  "CustomsOfficeOfDeparture" - {

    "serialize to JSON" in {
      val customsOffice = CustomsOfficeOfDeparture("ABC123")
      val expectedJson = Json.obj(
        "referenceNumber" -> "ABC123"
      )

      val json = Json.toJson(customsOffice)
      json mustEqual expectedJson
    }

    "deserialize from JSON" in {
      val json = Json.obj(
        "referenceNumber" -> "ABC123"
      )

      val expectedCustomsOffice = CustomsOfficeOfDeparture("ABC123")

      json.validate[CustomsOfficeOfDeparture] mustEqual JsSuccess(expectedCustomsOffice)
    }

    "handle missing referenceNumber during deserialization" in {
      val json = Json.obj()

      // Since referenceNumber is required, this should result in an error
      val result = json.validate[CustomsOfficeOfDeparture]
      result.isError mustEqual true
    }

  }
}
