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
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import generated.{AddressType21, GNSSType}
import generators.Generators
import models.RichAddressType21
import models.referenceData.*
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

    val IncidentType02 = arbitraryIncidentType02.arbitrary.sample.value

    "rows" - {

      "incidentCodeRow" - {
        "must return a row" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val incidentCode = IncidentCode(
                "1",
                "The carrier is obliged to deviate from the itinerary prescribed in accordance with Article 298 of UCC/IA Regulation due to circumstances beyond his control."
              )

              val incidentType = IncidentType02.copy(code = value)

              when(refDataService.getIncidentCode(any())(any(), any()))
                .thenReturn(Future.successful(incidentCode))

              val helper = new IncidentP5Helper(incidentType, refDataService)
              val result = helper.incidentCodeRow.futureValue.value

              result.key.value mustEqual "Incident code"
              result.value.value mustEqual "1 - The carrier is obliged to deviate from the itinerary prescribed in accordance with Article 298 of UCC/IA Regulation due to circumstances beyond his control."
              result.actions must not be defined
          }
        }
      }

      "incidentDescriptionRow" - {
        "must return a row" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val incidentType = IncidentType02.copy(text = value)

              val helper = new IncidentP5Helper(incidentType, refDataService)
              val result = helper.incidentDescriptionRow.value

              result.key.value mustEqual "Description"
              result.value.value mustEqual value
              result.actions must not be defined
          }
        }
      }

      "countryRow" - {
        "must return a row with description when ref data look up is successful" in {

          val description      = "description"
          val countryCode      = IncidentType02.Location.country
          val expectedResponse = s"$description - $countryCode"

          when(refDataService.getCountry(any())(any(), any()))
            .thenReturn(Future.successful(Country(countryCode, description)))

          val helper = new IncidentP5Helper(IncidentType02, refDataService)
          val result = helper.countryRow.futureValue.value

          result.key.value mustEqual "Country"
          result.value.value mustEqual expectedResponse
          result.actions must not be defined
        }

        "must throw an exception when ref data look up cannot find description" in {
          when(refDataService.getCountry(any())(any(), any()))
            .thenReturn(Future.failed(new NoReferenceDataFoundException("")))

          val helper = new IncidentP5Helper(IncidentType02, refDataService)

          whenReady(helper.countryRow.failed) {
            result => result mustBe a[NoReferenceDataFoundException]
          }
        }
      }

      "identifierTypeRow" - {
        "must return a row with description when ref data look up is successful" in {
          val description = "description"
          val qualifier   = IncidentType02.Location.qualifierOfIdentification

          when(refDataService.getQualifierOfIdentification(any())(any(), any()))
            .thenReturn(Future.successful(QualifierOfIdentification(qualifier, description)))

          val helper = new IncidentP5Helper(IncidentType02, refDataService)
          val result = helper.identifierTypeRow.futureValue.value

          result.key.value mustEqual "Identifier type"
          result.value.value mustEqual description
          result.actions must not be defined
        }

        "must throw an exception when ref data look up cannot find description" in {
          val refDataService: ReferenceDataService = mock[ReferenceDataService]

          when(refDataService.getQualifierOfIdentification(any())(any(), any()))
            .thenReturn(Future.failed(new NoReferenceDataFoundException("")))

          val helper = new IncidentP5Helper(IncidentType02, refDataService)

          whenReady(helper.identifierTypeRow.failed) {
            result => result mustBe a[NoReferenceDataFoundException]
          }
        }
      }

      "coordinatesRow" - {
        "must return a row" in {
          val locationType = arbitraryLocationType.arbitrary.sample.value.copy(GNSS = Some(GNSSType("90.1", "90.2")))

          val helper = new IncidentP5Helper(IncidentType02.copy(Location = locationType), refDataService)
          val result = helper.coordinatesRow.value

          result.key.value mustEqual "Coordinates"
          result.value.value mustEqual "(90.1, 90.2)"
          result.actions must not be defined
        }
      }

      "addressRow" - {
        "must return a row" in {
          forAll(arbitrary[AddressType21]) {
            address =>
              val modifiedIncidentType02 = IncidentType02.copy(Location = IncidentType02.Location.copy(Address = Some(address)))
              val helper                 = new IncidentP5Helper(modifiedIncidentType02, refDataService)
              val result                 = helper.addressRow.value

              result.key.value mustEqual "Address"
              result.value.value mustEqual address.toDynamicAddress.toString
              result.actions must not be defined
          }
        }
      }

      "unLocodeRow" - {
        "must return a row" in {

          val incidentType = IncidentType02.copy(
            Location = IncidentType02.Location.copy(
              UNLocode = Some("UNLocode")
            )
          )

          val helper = new IncidentP5Helper(incidentType, refDataService)
          val result = helper.unLocodeRow.value

          result.key.value mustEqual "UN/LOCODE"
          result.value.value mustEqual "UNLocode"
          result.actions must not be defined
        }
      }
    }

    "sections" - {
      "incidentInformationSection" - {
        "must return a static section" in {

          val country      = Country("code", "description")
          val incidentCode = IncidentCode("code", "text")

          val location = arbitraryLocationType.arbitrary.sample.value.copy(
            UNLocode = Some("unlocode"),
            GNSS = Some(GNSSType("50.1", "50.2")),
            Address = Some(AddressType21("streetAndNumber", None, "city"))
          )

          when(refDataService.getCountry(any())(any(), any()))
            .thenReturn(Future.successful(country))
          when(refDataService.getIncidentCode(any())(any(), any()))
            .thenReturn(Future.successful(incidentCode))

          val incident = IncidentType02.copy(Location = location)
          val helper   = new IncidentP5Helper(incident, refDataService)
          val result   = helper.incidentInformationSection.futureValue

          result mustBe a[StaticSection]
          result.rows.size mustEqual 7
        }
      }

      "transportEquipmentsSection" - {
        "must return a static section" in {
          val transportEquipment1 = arbitraryTransportEquipmentType06.arbitrary.sample.value.copy(sequenceNumber = 1)
          val transportEquipment2 = arbitraryTransportEquipmentType06.arbitrary.sample.value.copy(sequenceNumber = 2)
          val transportEquipments = Seq(transportEquipment1, transportEquipment2)
          val helper              = new IncidentP5Helper(IncidentType02.copy(TransportEquipment = transportEquipments), refDataService)
          val result              = helper.transportEquipmentsSection

          result mustBe a[StaticSection]
          result.rows.size mustEqual 0
          result.children.size mustEqual 2

          result.children.head mustBe a[AccordionSection]
          result.children.head.sectionTitle mustEqual Some("Transport equipment 1")
          result.children.head.isOpen mustEqual true

          result.children(1) mustBe a[AccordionSection]
          result.children(1).sectionTitle mustEqual Some("Transport equipment 2")
          result.children(1).isOpen mustEqual false
        }
      }
    }
  }
}
