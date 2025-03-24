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

package viewModels.P5.arrival

import base.SpecBase
import generators.Generators
import models.referenceData.CustomsOffice
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.P5.arrival.UnloadingRemarkWithoutFunctionalErrorsP5ViewModel.UnloadingRemarkWithoutFunctionalErrorsP5ViewModelProvider

class UnloadingRemarkWithoutFunctionalErrorsP5ViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "UnloadingRemarkWithoutFunctionalErrorsP5ViewModel" - {

    val mrn                = "AB123"
    val customsReferenceId = "CD123"

    val viewModelProvider = new UnloadingRemarkWithoutFunctionalErrorsP5ViewModelProvider()

    def viewModel(customsOffice: CustomsOffice = fakeCustomsOffice): UnloadingRemarkWithoutFunctionalErrorsP5ViewModel =
      viewModelProvider.apply(mrn, customsOffice)

    "title" - {
      "must return correct message" in {
        viewModel().title `mustBe` "Unloading remarks errors"
      }
    }

    "heading" - {
      "must return correct message" in {
        viewModel().title `mustBe` "Unloading remarks errors"
      }
    }

    "paragraph1" - {
      "must return correct message" in {
        viewModel().paragraph1 `mustBe` s"There are one or more errors with the unloading remarks for this notification."
      }
    }

    "customsOfficeContent" - {

      "when customs office found with telephone number and name" - {
        "must return correct message" in {
          val customsOfficeName = "custName"
          val telephoneNo       = Some("123")
          val email             = Some("test123@email.com")
          val result            = viewModel(customsOffice = CustomsOffice(customsReferenceId, customsOfficeName, telephoneNo, email)).customsOfficeContent

          result `mustBe` s"Try making the unloading remarks again. Or for more information, contact Customs at $customsOfficeName on ${telephoneNo.get}."
        }
      }

      "when customs office found with name and no telephone number" - {
        "must return correct message" in {
          val customsOfficeName = "custName"
          val result            = viewModel(customsOffice = CustomsOffice(customsReferenceId, customsOfficeName, None, None)).customsOfficeContent

          result `mustBe` s"Try making the unloading remarks again. Or for more information, contact Customs at $customsOfficeName."
        }
      }

      "when customs office found with telephone number but empty name" - {
        "must return correct message" in {
          val customsOfficeName = ""
          val telephoneNo       = Some("123")
          val result            = viewModel(customsOffice = CustomsOffice(customsReferenceId, customsOfficeName, telephoneNo, None)).customsOfficeContent

          result `mustBe` s"Try making the unloading remarks again. Or for more information, contact Customs office $customsReferenceId on ${telephoneNo.get}."
        }
      }

      "when customs office found with no telephone number and empty name" - {
        "must return correct message" in {
          val customsOfficeName = ""
          val result            = viewModel(customsOffice = CustomsOffice(customsReferenceId, customsOfficeName, None, None)).customsOfficeContent

          result `mustBe` s"Try making the unloading remarks again. Or for more information, contact Customs office $customsReferenceId."
        }
      }
    }

    "hyperlink" - {
      "must return correct message" in {
        viewModel().hyperlink `mustBe` "View arrival notifications"
      }
    }
  }

}
