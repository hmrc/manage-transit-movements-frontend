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

package viewModels.P5

import base.SpecBase
import generators.Generators
import models.LocalReferenceNumber
import models.departureP5.DepartureReferenceNumbers
import models.referenceData.CustomsOffice
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.P5.departure.IncidentsDuringTransitP5ViewModel
import viewModels.P5.departure.IncidentsDuringTransitP5ViewModel.IncidentsDuringTransitP5ViewModelProvider

class IncidentsDuringTransitP5ViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "IncidentsDuringTransitP5ViewModel" - {

    val mrn                = "AB123"
    val lrn                = "LRN123"
    val customsReferenceId = "CD123"

    val departureReferenceNumbers = DepartureReferenceNumbers(LocalReferenceNumber(lrn), Some(mrn))

    val viewModelProvider = new IncidentsDuringTransitP5ViewModelProvider()

    def viewModel(
      customsOffice: Either[String, CustomsOffice] = Left(customsReferenceId),
      isMultipleIncidents: Boolean = true
    ): IncidentsDuringTransitP5ViewModel =
      viewModelProvider.apply(departureReferenceNumbers, customsOffice, isMultipleIncidents)

    "when multiple incident" - {
      "title" - {
        "must return correct message" in {
          viewModel().title mustBe "Incidents during transit"
        }
      }

      "heading" - {
        "must return correct message" in {
          viewModel().title mustBe "Incidents during transit"
        }
      }

      "paragraph1" - {
        "must return correct message" in {
          viewModel().paragraph1 mustBe
            "Multiple incidents have been reported by the customs office of incident. Review the incident details and contact the carrier for more information."
        }
      }

      "paragraph2" - {
        "must return correct message" in {
          viewModel().paragraph2HyperLink mustBe "Check your departure declarations"
          viewModel().paragraph2End mustBe "for further updates."
        }
      }
    }

    "when 1 incident" - {
      "title" - {
        "must return correct message" in {
          viewModel(isMultipleIncidents = false).title mustBe "Incident during transit"
        }
      }

      "heading" - {
        "must return correct message" in {
          viewModel(isMultipleIncidents = false).title mustBe "Incident during transit"
        }
      }

      "paragraph1" - {
        "must return correct message" in {
          viewModel(isMultipleIncidents = false).paragraph1 mustBe
            "An incident has been reported by the customs office of incident. Review the incident details and contact the carrier for more information."
        }
      }

      "paragraph2" - {
        "must return correct message" in {
          viewModel(isMultipleIncidents = false).paragraph2HyperLink mustBe "Check your departure declarations"
          viewModel(isMultipleIncidents = false).paragraph2End mustBe "for further updates."
        }
      }
    }

    "customsOfficeContent" - {

      "when no customs office found" - {
        "must return correct message" in {
          viewModel().customsOfficeContent mustBe
            s"For further help, contact Customs office $customsReferenceId."
        }
      }

      "when customs office found with telephone number and name" - {
        "must return correct message" in {
          val customsOfficeName = "custName"
          val telephoneNo       = Some("123")
          val result            = viewModel(customsOffice = Right(CustomsOffice(customsReferenceId, customsOfficeName, telephoneNo))).customsOfficeContent

          result mustBe s"For further help, contact the carrier or Customs at $customsOfficeName on ${telephoneNo.get}."
        }
      }

      "when customs office found with name and no telephone number" - {
        "must return correct message" in {
          val customsOfficeName = "custName"
          val result            = viewModel(customsOffice = Right(CustomsOffice(customsReferenceId, customsOfficeName, None))).customsOfficeContent

          result mustBe s"For further help, contact Customs at $customsOfficeName."
        }
      }

      "when customs office found with telephone number but empty name" - {
        "must return correct message" in {
          val customsOfficeName = ""
          val telephoneNo       = Some("123")
          val result            = viewModel(customsOffice = Right(CustomsOffice(customsReferenceId, customsOfficeName, telephoneNo))).customsOfficeContent

          result mustBe s"For further help, contact Customs office $customsReferenceId on ${telephoneNo.get}."
        }
      }

      "when customs office found with no telephone number and empty name" - {
        "must return correct message" in {
          val customsOfficeName = ""
          val result            = viewModel(customsOffice = Right(CustomsOffice(customsReferenceId, customsOfficeName, None))).customsOfficeContent

          result mustBe s"For further help, contact Customs office $customsReferenceId."
        }
      }
    }
  }
}
