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

package viewModels

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.referenceData.CustomsOffice
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.P5.departure.CustomsOfficeContactViewModel

class CustomsOfficeContactViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "CustomsOfficeContactViewModel" - {

    "must render correct paragraph" - {

      "When Customs office name and telephone exists" in {

        val customsOffice     = CustomsOffice("ID001", "Dover", Some("00443243543"))
        val viewModelProvider = CustomsOfficeContactViewModel("GB000060", Some(customsOffice))

        val result: String = viewModelProvider.customsOfficeContent

        result mustBe "You must share the requested documentation with the office of destination. Contact Customs at Dover on 00443243543."
      }
      "When Customs Office name not available and telephone exists" in {

        val customsOffice     = CustomsOffice("ID001", "", Some("00443243543"))
        val viewModelProvider = CustomsOfficeContactViewModel("GB000060", Some(customsOffice))

        val result: String = viewModelProvider.customsOfficeContent

        result mustBe "You must share the requested documentation with the office of destination. Contact Customs office ID001 on 00443243543."
      }
      "When Customs Office name available and telephone does not exist" in {

        val customsOffice     = CustomsOffice("ID001", "Dover", Some(""))
        val viewModelProvider = CustomsOfficeContactViewModel("GB000060", Some(customsOffice))

        val result: String = viewModelProvider.customsOfficeContent

        result mustBe "You must share the requested documentation with the office of destination. Contact Customs at Dover."
      }
      "When Customs Office name available and telephone is None" in {

        val customsOffice     = CustomsOffice("ID001", "Dover", None)
        val viewModelProvider = CustomsOfficeContactViewModel("GB000060", Some(customsOffice))

        val result: String = viewModelProvider.customsOfficeContent

        result mustBe "You must share the requested documentation with the office of destination. Contact Customs at Dover."
      }
      "When Customs Office name not available and telephone does not exist" in {

        val customsOffice     = CustomsOffice("ID001", "", Some(""))
        val viewModelProvider = CustomsOfficeContactViewModel("GB000060", Some(customsOffice))

        val result: String = viewModelProvider.customsOfficeContent

        result mustBe "You must share the requested documentation with the office of destination. Contact Customs office ID001."
      }
      "When Customs Office name not available and telephone is None" in {

        val customsOffice     = CustomsOffice("ID001", "", None)
        val viewModelProvider = CustomsOfficeContactViewModel("GB000060", Some(customsOffice))

        val result: String = viewModelProvider.customsOfficeContent

        result mustBe "You must share the requested documentation with the office of destination. Contact Customs office ID001."
      }
      "When Customs Office not fetched from reference data service" in {

        val viewModelProvider = CustomsOfficeContactViewModel("GB000060", None)

        val result: String = viewModelProvider.customsOfficeContent

        result mustBe "You must share the requested documentation with the office of destination. Contact Customs office GB000060."
      }

    }

  }
}
