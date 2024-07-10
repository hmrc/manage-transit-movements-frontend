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

package helper

import base.SpecBase
import generated.CC182CType
import generators.Generators
import org.scalacheck.Arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import utils.IncidentP5Helper
import viewModels.sections.Section.StaticSection

class IncidentP5HelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "IncidentAnswersHelper" - {

    val CC182CType = Arbitrary.arbitrary[CC182CType].sample.value

    "rows" - {
      "incidentCodeRow" - {
        "must return a row" in {
          val modifiedCC182CType = CC182CType.copy(TransitOperation = CC182CType.TransitOperation)

          val helper = new IncidentP5Helper(modifiedCC182CType, isMultipleIncidents = true)
          val result = helper.incidentCodeRow.value

          result.key.value mustBe "Incident code"
          result.value.value mustBe "code"
          result.actions must not be defined
        }
      }

      "descriptionRow" - {
        "must return a row" in {
          val modifiedCC182CType = CC182CType.copy(TransitOperation = CC182CType.TransitOperation)

          val helper = new IncidentP5Helper(modifiedCC182CType, isMultipleIncidents = true)
          val result = helper.descriptionRow.value

          result.key.value mustBe "Description"
          result.value.value mustBe "description"
          result.actions must not be defined
        }
      }

      "countryRow" - {
        "must return a row" in {
          val modifiedCC182CType = CC182CType.copy(TransitOperation = CC182CType.TransitOperation)

          val helper = new IncidentP5Helper(modifiedCC182CType, isMultipleIncidents = true)
          val result = helper.countryRow.value

          result.key.value mustBe "Country"
          result.value.value mustBe "GB"
          result.actions must not be defined
        }
      }

      "identifierTypeRow" - {
        "must return a row" in {
          val modifiedCC182CType = CC182CType.copy(TransitOperation = CC182CType.TransitOperation)

          val helper = new IncidentP5Helper(modifiedCC182CType, isMultipleIncidents = true)
          val result = helper.identifierTypeRow.value

          result.key.value mustBe "Identifier Type"
          result.value.value mustBe "identifierType"
          result.actions must not be defined
        }
      }

      "coordinatesRow" - {
        "must return a row" in {
          val modifiedCC182CType = CC182CType.copy(TransitOperation = CC182CType.TransitOperation)

          val helper = new IncidentP5Helper(modifiedCC182CType, isMultipleIncidents = true)
          val result = helper.coordinatesRow.value

          result.key.value mustBe "Coordinates"
          result.value.value mustBe "coordinates"
          result.actions must not be defined
        }
      }

      "endorsementDateRow" - {
        "must return a row" in {
          val modifiedCC182CType = CC182CType.copy(TransitOperation = CC182CType.TransitOperation)

          val helper = new IncidentP5Helper(modifiedCC182CType, isMultipleIncidents = true)
          val result = helper.endorsementDateRow.value

          result.key.value mustBe "Endorsement date"
          result.value.value mustBe "endorsement date"
          result.actions must not be defined
        }
      }

      "authorityRow" - {
        "must return a row" in {
          val modifiedCC182CType = CC182CType.copy(TransitOperation = CC182CType.TransitOperation)

          val helper = new IncidentP5Helper(modifiedCC182CType, isMultipleIncidents = true)
          val result = helper.authorityRow.value

          result.key.value mustBe "Authority"
          result.value.value mustBe "authority"
          result.actions must not be defined
        }
      }

      "endorsementCountryRow" - {
        "must return a row" in {
          val modifiedCC182CType = CC182CType.copy(TransitOperation = CC182CType.TransitOperation)

          val helper = new IncidentP5Helper(modifiedCC182CType, isMultipleIncidents = true)
          val result = helper.endorsementCountryRow.value

          result.key.value mustBe "Country"
          result.value.value mustBe "endorsementCountry"
          result.actions must not be defined
        }
      }

      "locationRow" - {
        "must return a row" in {
          val modifiedCC182CType = CC182CType.copy(TransitOperation = CC182CType.TransitOperation)

          val helper = new IncidentP5Helper(modifiedCC182CType, isMultipleIncidents = true)
          val result = helper.locationRow.value

          result.key.value mustBe "Location"
          result.value.value mustBe "location"
          result.actions must not be defined
        }
      }

    }

    "sections" - {
      "incidentInformationSection" - {
        "must return a static section" in {
          val helper = new IncidentP5Helper(CC182CType, isMultipleIncidents = true)
          val result = helper.incidentInformationSection

          result mustBe a[StaticSection]
          result.rows.size mustBe 5
        }
      }
    }
  }
}