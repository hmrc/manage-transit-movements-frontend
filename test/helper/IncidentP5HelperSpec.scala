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
import generated.GNSSType
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import scalaxb.XMLCalendar
import utils.IncidentP5Helper
import viewModels.sections.Section.StaticSection

class IncidentP5HelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "IncidentAnswersHelper" - {

    val incidentType03 = arbitraryIncidentType03.arbitrary.sample.value

    "rows" - {
      "incidentCodeRow" - {
        "must return a row" in {
          val helper = new IncidentP5Helper(incidentType03)
          val result = helper.incidentCodeRow.value

          result.key.value mustBe "Incident code"
          result.value.value mustBe "code"
          result.actions must not be defined
        }
      }

      "descriptionRow" - {
        "must return a row" in {

          val helper = new IncidentP5Helper(incidentType03)
          val result = helper.descriptionRow.value

          result.key.value mustBe "Description"
          result.value.value mustBe "description"
          result.actions must not be defined
        }
      }

      "countryRow" - {
        "must return a row" in {

          val helper = new IncidentP5Helper(incidentType03)
          val result = helper.countryRow.value

          result.key.value mustBe "Country"
          result.value.value mustBe incidentType03.Location.country
          result.actions must not be defined
        }
      }

      "identifierTypeRow" - {
        "must return a row" in {

          val helper = new IncidentP5Helper(incidentType03)
          val result = helper.identifierTypeRow.value

          result.key.value mustBe "Identifier Type"
          result.value.value mustBe incidentType03.Location.qualifierOfIdentification
          result.actions must not be defined
        }
      }

      "coordinatesRow" - {
        "must return a row" in {
          val locationType = arbitraryLocationType02.arbitrary.sample.value.copy(GNSS = Some(GNSSType("90.1", "90.2")))

          val helper = new IncidentP5Helper(incidentType03.copy(Location = locationType))
          val result = helper.coordinatesRow.value

          result.key.value mustBe "Coordinates"
          result.value.value mustBe "(90.1, 90.2)"
          result.actions must not be defined
        }
      }

      "unLocodeRow" - {
        "must return a row" in {

          val helper = new IncidentP5Helper(incidentType03)
          val result = helper.unLocodeRow.value

          result.key.value mustBe "UN/LOCODE"
          result.value.value mustBe incidentType03.Location.UNLocode.get
          result.actions must not be defined
        }
      }

      "addressRow" - {
        "must return a row" in {

          val helper = new IncidentP5Helper(incidentType03)
          val result = helper.addressRow.value

          result.key.value mustBe "Address"
          result.value.value mustBe "address"
          result.actions must not be defined
        }
      }

      "endorsementDateRow" - {
        "must return a row" in {
          val endorsement     = arbitraryEndorsement03.arbitrary.sample.value.copy(date = XMLCalendar("2022-07-15"))
          val updatedIncident = incidentType03.copy(Endorsement = Some(endorsement))

          val helper = new IncidentP5Helper(updatedIncident)
          val result = helper.endorsementDateRow.value

          result.key.value mustBe "Endorsement date"
          result.value.value mustBe "2022-07-15"
          result.actions must not be defined
        }
      }

      "authorityRow" - {
        "must return a row" in {
          val endorsement     = arbitraryEndorsement03.arbitrary.sample.value.copy(authority = "authority")
          val updatedIncident = arbitraryIncidentType03.arbitrary.sample.value.copy(Endorsement = Some(endorsement))

          val helper = new IncidentP5Helper(updatedIncident)
          val result = helper.authorityRow.value

          result.key.value mustBe "Authority"
          result.value.value mustBe "authority"
          result.actions must not be defined
        }
      }

      "endorsementCountryRow" - {
        "must return a row" in {
          val endorsement     = arbitraryEndorsement03.arbitrary.sample.value.copy(country = "GB")
          val updatedIncident = arbitraryIncidentType03.arbitrary.sample.value.copy(Endorsement = Some(endorsement))

          val helper = new IncidentP5Helper(updatedIncident)
          val result = helper.endorsementCountryRow.value

          result.key.value mustBe "Country"
          result.value.value mustBe "GB"
          result.actions must not be defined
        }
      }

      "locationRow" - {
        "must return a row" in {
          val endorsement     = arbitraryEndorsement03.arbitrary.sample.value.copy(place = "location")
          val updatedIncident = arbitraryIncidentType03.arbitrary.sample.value.copy(Endorsement = Some(endorsement))

          val helper = new IncidentP5Helper(updatedIncident)
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
          val helper = new IncidentP5Helper(incidentType03)
          val result = helper.incidentInformationSection

          result mustBe a[StaticSection]
          result.rows.size mustBe 5
        }

        "must contain coordinates row if the identifier type is W" in {
          val helper = new IncidentP5Helper(incidentType03.copy(Location = incidentType03.Location.copy(qualifierOfIdentification = "W")))
          val result = helper.incidentInformationSection

          result.rows.size mustBe 5
          result.rows.map(_.key.value) must contain("Coordinates")
        }

        "must contain UN/LOCODE row if the identifier type is U" in {
          val helper = new IncidentP5Helper(incidentType03.copy(Location = incidentType03.Location.copy(qualifierOfIdentification = "U")))
          val result = helper.incidentInformationSection

          result.rows.size mustBe 5
          result.rows.map(_.key.value) must contain("UN/LOCODE")
        }

        "must contain address row if the identifier type is Z" in {
          val helper = new IncidentP5Helper(incidentType03.copy(Location = incidentType03.Location.copy(qualifierOfIdentification = "Z")))
          val result = helper.incidentInformationSection

          result.rows.size mustBe 5
          result.rows.map(_.key.value) must contain("Address")
        }
      }
    }
  }
}
