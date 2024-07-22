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
import generated.AddressType18
import generators.Generators
import models.{Country, IncidentCode, RichAddressType18}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import scalaxb.XMLCalendar
import services.ReferenceDataService
import utils.IncidentP5Helper
import viewModels.sections.Section.StaticSection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IncidentP5HelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val refDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ReferenceDataService].toInstance(refDataService))

  "IncidentAnswersHelper" - {

    val incidentType03 = arbitraryIncidentType03.arbitrary.sample.value

    "rows" - {

      "incidentCodeRow" - {
        "must return a row" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val incidentCode = IncidentCode(
                "1",
                "The carrier is obliged to deviate from the itinerary prescribed in accordance with Article 298 of UCC/IA Regulation due to circumstances beyond his control."
              )

              val incidentType = incidentType03.copy(code = value)

              when(refDataService.getIncidentCode(any())(any(), any()))
                .thenReturn(Future.successful(incidentCode))

              val helper = new IncidentP5Helper(incidentType, refDataService)
              val result = helper.incidentCodeRow.futureValue.value

              result.key.value mustBe "Incident code"
              result.value.value mustBe
                "1 - The carrier is obliged to deviate from the itinerary prescribed in accordance with Article 298 of UCC/IA Regulation due to circumstances beyond his control."
              result.actions must not be defined
          }
        }
      }

      "incidentDescriptionRow" - {
        "must return a row" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val incidentType = incidentType03.copy(text = value)

              val helper = new IncidentP5Helper(incidentType, refDataService)
              val result = helper.incidentDescriptionRow.value

              result.key.value mustBe "Description"
              result.value.value mustBe value
              result.actions must not be defined
          }
        }
      }

      "countryRow" - {
        "must return a row with description when ref data look up is successful" in {
          when(refDataService.getCountry(any())(any(), any()))
            .thenReturn(Future.successful(Right(Country(incidentType03.Location.country, "description"))))

          val helper = new IncidentP5Helper(incidentType03, refDataService)
          val result = helper.countryRow.futureValue.value

          result.key.value mustBe "Country"
          result.value.value mustBe "description"
          result.actions must not be defined
        }
      }

      "must return a row with description when ref data look up cannot find description" in {
        when(refDataService.getCountry(any())(any(), any()))
          .thenReturn(Future.successful(Left(incidentType03.Location.country)))

        val helper = new IncidentP5Helper(incidentType03, refDataService)
        val result = helper.countryRow.futureValue.value

        result.key.value mustBe "Country"
        result.value.value mustBe incidentType03.Location.country
        result.actions must not be defined
      }

      "identifierTypeRow" - {
        "must return a row" in {

          val helper = new IncidentP5Helper(incidentType03, refDataService)
          val result = helper.identifierTypeRow.value

          result.key.value mustBe "Identifier Type"
          result.value.value mustBe incidentType03.Location.qualifierOfIdentification
          result.actions must not be defined
        }
      }

      "coordinatesRow" - {
        "must return a row" in {

          val helper = new IncidentP5Helper(incidentType03, refDataService)
          val result = helper.coordinatesRow.value

          result.key.value mustBe "Coordinates"
          result.value.value mustBe "coordinates"
          result.actions must not be defined
        }
      }

      "addressRow" - {
        "must return a row" in {
          forAll(arbitrary[AddressType18]) {
            address =>
              val modifiedIncidentType03 = incidentType03.copy(Location = incidentType03.Location.copy(Address = Some(address)))
              val helper                 = new IncidentP5Helper(modifiedIncidentType03, refDataService)
              val result                 = helper.addressRow.value

              result.key.value mustBe "Address"
              result.value.value mustBe address.toDynamicAddress.toString
              result.actions must not be defined
          }
        }
      }

      "unLocodeRow" - {
        "must return a row" in {

          val incidentType = incidentType03.copy(
            Location = incidentType03.Location.copy(
              UNLocode = Some("UNLocode")
            )
          )

          val helper = new IncidentP5Helper(incidentType03, refDataService)
          val result = helper.unLocodeRow.value

          result.key.value mustBe "UN/LOCODE"
          result.value.value mustBe "UNLocode"
          result.actions must not be defined
        }
      }

      "endorsementDateRow" - {
        "must return a row" in {
          val endorsement     = arbitraryEndorsement03.arbitrary.sample.value.copy(date = XMLCalendar("2022-07-15"))
          val updatedIncident = incidentType03.copy(Endorsement = Some(endorsement))

          val helper = new IncidentP5Helper(updatedIncident, refDataService)
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

          val helper = new IncidentP5Helper(updatedIncident, refDataService)
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

          val helper = new IncidentP5Helper(updatedIncident, refDataService)
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

          val helper = new IncidentP5Helper(updatedIncident, refDataService)
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

          val country  = Country("code", "description")
          val incident = IncidentCode("code", "text")

          when(refDataService.getCountry(any())(any(), any()))
            .thenReturn(Future.successful(Right(country)))
          when(refDataService.getIncidentCode(any())(any(), any()))
            .thenReturn(Future.successful(incident))

          val incidentType = incidentType03.copy(
            Location = incidentType03.Location.copy(
              Address = Some(arbitraryAddressType18.arbitrary.sample.value),
              UNLocode = Some("UNLocode")
            )
          )

          val helper = new IncidentP5Helper(incidentType, refDataService)
          val result = helper.incidentInformationSection.futureValue

          result mustBe a[StaticSection]
          result.rows.size mustBe 7
        }
      }
    }
  }
}
