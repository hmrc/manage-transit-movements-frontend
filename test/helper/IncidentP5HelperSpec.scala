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
import generated.{AddressType18, GNSSType}
import generators.Generators
import models.{Country, IncidentCode, QualifierOfIdentification, RichAddressType18}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import utils.IncidentP5Helper
import viewModels.sections.Section.{AccordionSection, StaticSection}

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

        "must return a row with description when ref data look up cannot find description" in {
          when(refDataService.getCountry(any())(any(), any()))
            .thenReturn(Future.successful(Left(incidentType03.Location.country)))

          val helper = new IncidentP5Helper(incidentType03, refDataService)
          val result = helper.countryRow.futureValue.value

          result.key.value mustBe "Country"
          result.value.value mustBe incidentType03.Location.country
          result.actions must not be defined
        }
      }

      "identifierTypeRow" - {
        "must return a row with description when ref data look up is successful" in {
          when(refDataService.getQualifierOfIdentification(any())(any(), any()))
            .thenReturn(Future.successful(Right(QualifierOfIdentification(incidentType03.Location.qualifierOfIdentification, "description"))))

          val helper = new IncidentP5Helper(incidentType03, refDataService)
          val result = helper.identifierTypeRow.futureValue.value

          result.key.value mustBe "Identifier type"
          result.value.value mustBe "description"
          result.actions must not be defined
        }

        "must return a row with description when ref data look up cannot find description" in {
          when(refDataService.getQualifierOfIdentification(any())(any(), any()))
            .thenReturn(Future.successful(Left(incidentType03.Location.qualifierOfIdentification)))

          val helper = new IncidentP5Helper(incidentType03, refDataService)
          val result = helper.identifierTypeRow.futureValue.value

          result.key.value mustBe "Identifier type"
          result.value.value mustBe incidentType03.Location.qualifierOfIdentification
          result.actions must not be defined
        }
      }

      "coordinatesRow" - {
        "must return a row" in {
          val locationType = arbitraryLocationType02.arbitrary.sample.value.copy(GNSS = Some(GNSSType("90.1", "90.2")))

          val helper = new IncidentP5Helper(incidentType03.copy(Location = locationType), refDataService)
          val result = helper.coordinatesRow.value

          result.key.value mustBe "Coordinates"
          result.value.value mustBe "(90.1, 90.2)"
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

          val helper = new IncidentP5Helper(incidentType, refDataService)
          val result = helper.unLocodeRow.value

          result.key.value mustBe "UN/LOCODE"
          result.value.value mustBe "UNLocode"
          result.actions must not be defined
        }
      }
    }

    "sections" - {
      "incidentInformationSection" - {
        "must return a static section" in {

          val country      = Country("code", "description")
          val incidentCode = IncidentCode("code", "text")

          val location = arbitraryLocationType02.arbitrary.sample.value.copy(
            UNLocode = Some("unlocode"),
            GNSS = Some(GNSSType("50.1", "50.2")),
            Address = Some(AddressType18("streetAndNumber", None, "city"))
          )

          when(refDataService.getCountry(any())(any(), any()))
            .thenReturn(Future.successful(Right(country)))
          when(refDataService.getIncidentCode(any())(any(), any()))
            .thenReturn(Future.successful(incidentCode))

          val incident = incidentType03.copy(Location = location)
          val helper   = new IncidentP5Helper(incident, refDataService)
          val result   = helper.incidentInformationSection.futureValue

          result mustBe a[StaticSection]
          result.rows.size mustBe 7
        }
      }

      "transportEquipmentsSection" - {
        "must return a static section" in {
          val transportEquipment1 = arbitraryTransportEquipmentType07.arbitrary.sample.value.copy(sequenceNumber = "1")
          val transportEquipment2 = arbitraryTransportEquipmentType07.arbitrary.sample.value.copy(sequenceNumber = "2")
          val transportEquipments = Seq(transportEquipment1, transportEquipment2)
          val helper              = new IncidentP5Helper(incidentType03.copy(TransportEquipment = transportEquipments), refDataService)
          val result              = helper.transportEquipmentsSection

          result mustBe a[StaticSection]
          result.rows.size mustBe 0
          result.children.size mustBe 2

          result.children.head mustBe a[AccordionSection]
          result.children.head.sectionTitle mustBe Some("Transport equipment 1")
          result.children.head.isOpen mustBe true

          result.children(1) mustBe a[AccordionSection]
          result.children(1).sectionTitle mustBe Some("Transport equipment 2")
          result.children(1).isOpen mustBe false
        }
      }
    }
  }
}
