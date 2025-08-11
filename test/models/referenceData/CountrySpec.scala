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
import config.FrontendAppConfig
import org.mockito.Mockito.when
import play.api.libs.json.{JsSuccess, Json, Reads}

class CountrySpec extends SpecBase {

  private val mockFrontendAppConfig = mock[FrontendAppConfig]

  "Country" - {

    "deserialize from JSON" - {
      "when phase 5" in {
        when(mockFrontendAppConfig.phase6Enabled).thenReturn(false)
        val json = Json.obj(
          "code"        -> "UK",
          "description" -> "United Kingdom"
        )

        implicit val reads: Reads[Country] = Country.reads(mockFrontendAppConfig)

        val expectedCountry = Country("UK", "United Kingdom")
        json.validate[Country] mustEqual JsSuccess(expectedCountry)
      }

      "when phase 6" in {
        when(mockFrontendAppConfig.phase6Enabled).thenReturn(true)
        val json = Json.obj(
          "key"   -> "UK",
          "value" -> "United Kingdom"
        )

        implicit val reads: Reads[Country] = Country.reads(mockFrontendAppConfig)

        val expectedCountry = Country("UK", "United Kingdom")
        json.validate[Country] mustEqual JsSuccess(expectedCountry)
      }
    }

    "return the correct string representation" in {
      val country = Country("UK", "United Kingdom")
      country.toString mustEqual "United Kingdom - UK"
    }
  }
}
