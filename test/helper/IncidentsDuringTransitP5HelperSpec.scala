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
import models.Link
import models.referenceData.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import utils.IncidentsDuringTransitP5Helper
import viewModels.sections.Section.{AccordionSection, StaticSection}

import javax.xml.datatype.XMLGregorianCalendar
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IncidentsDuringTransitP5HelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "ConsignmentAnswersHelper" - {

    val CC182CType = Arbitrary.arbitrary[CC182CType].sample.value

    "rows" - {
      "mrnRow" - {
        "must return a row" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val modifiedCC182CType = CC182CType.copy(TransitOperation = CC182CType.TransitOperation.copy(MRN = value))

              val helper = new IncidentsDuringTransitP5Helper(modifiedCC182CType, isMultipleIncidents = true, mockReferenceDataService)
              val result = helper.mrnRow.value

              result.key.value mustBe "Movement Reference Number (MRN)"
              result.value.value mustBe value
              result.actions must not be defined
          }
        }
      }

      "dateTimeIncidentReportedRow" - {
        "must return a row with correct heading" - {
          "when isMultipleIncidents is false" in {
            forAll(Arbitrary.arbitrary[XMLGregorianCalendar]) {
              value =>
                val modifiedCC182CType = CC182CType.copy(TransitOperation = CC182CType.TransitOperation.copy(incidentNotificationDateAndTime = value))
                val helper             = new IncidentsDuringTransitP5Helper(modifiedCC182CType, isMultipleIncidents = false, mockReferenceDataService)

                val result = helper.dateTimeIncidentReportedRow.value

                result.key.value mustBe "Date and time incident reported"
                result.value.value mustBe "time of incident here"
                result.actions must not be defined
            }
          }
          "when isMultipleIncidents is true" in {
            forAll(Arbitrary.arbitrary[XMLGregorianCalendar]) {
              value =>
                val modifiedCC182CType = CC182CType.copy(TransitOperation = CC182CType.TransitOperation.copy(incidentNotificationDateAndTime = value))
                val helper             = new IncidentsDuringTransitP5Helper(modifiedCC182CType, isMultipleIncidents = true, mockReferenceDataService)

                val result = helper.dateTimeIncidentReportedRow.value

                result.key.value mustBe "Date and time incidents reported"
                result.value.value mustBe "time of incident here"
                result.actions must not be defined
            }
          }
        }
      }

      "customsOfficeOfIncidentRow" - {
        "must return a row" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val customsOffice = CustomsOffice("XI000142", "Belfast", None)

              when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any()))
                .thenReturn(Future.successful(customsOffice))

              val modifiedCC182CType =
                CC182CType.copy(CustomsOfficeOfIncidentRegistration = CC182CType.CustomsOfficeOfIncidentRegistration.copy(referenceNumber = value))

              val helper = new IncidentsDuringTransitP5Helper(modifiedCC182CType, isMultipleIncidents = true, mockReferenceDataService)
              val result = helper.customsOfficeOfIncidentRow.futureValue.value

              result.key.value mustBe "Customs office of incident"
              result.value.value mustBe "Belfast (XI000142)"
              result.actions must not be defined
          }
        }
      }

      "officeOfDepartureRow" - {
        "must return a row" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val customsOffice = CustomsOffice("XI000142", "Belfast", None)

              when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any()))
                .thenReturn(Future.successful(customsOffice))

              val modifiedCC182CType =
                CC182CType.copy(CustomsOfficeOfIncidentRegistration = CC182CType.CustomsOfficeOfIncidentRegistration.copy(referenceNumber = value))

              val helper = new IncidentsDuringTransitP5Helper(modifiedCC182CType, isMultipleIncidents = true, mockReferenceDataService)
              val result = helper.officeOfDepartureRow.futureValue.value

              result.key.value mustBe "Office of departure"
              result.value.value mustBe "Belfast (XI000142)"
              result.actions must not be defined
          }
        }
      }

      "incidentCodeRow" - {
        "must return a row" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val updatedIncident = arbitraryIncidentType03.arbitrary.sample.value.copy(
                code = value
              )

              val updatedConsignment = CC182CType.Consignment.copy(
                Incident = Seq(updatedIncident)
              )

              val modifiedCC182CType = CC182CType.copy(
                Consignment = updatedConsignment
              )

              val helper = new IncidentsDuringTransitP5Helper(modifiedCC182CType, isMultipleIncidents = true, mockReferenceDataService)
              val result = helper.incidentCodeRow(incidentIndex).value

              result.key.value mustBe "Incident code"
              result.value.value mustBe "incident code here"
              result.actions must not be defined
          }
        }
      }

      "incidentDescriptionRow" - {
        "must return a row" in {
          forAll(Gen.alphaNumStr) {
            value =>
              val updatedIncident = arbitraryIncidentType03.arbitrary.sample.value.copy(
                text = value
              )

              val updatedConsignment = CC182CType.Consignment.copy(
                Incident = Seq(updatedIncident)
              )

              val modifiedCC182CType = CC182CType.copy(
                Consignment = updatedConsignment
              )

              val helper = new IncidentsDuringTransitP5Helper(modifiedCC182CType, isMultipleIncidents = true, mockReferenceDataService)
              val result = helper.incidentDescriptionRow(incidentIndex).value

              result.key.value mustBe "Description"
              result.value.value mustBe "incident description here"
              result.actions must not be defined
          }
        }
      }
    }

    "sections" - {
      "incidentInformationSection" - {
        "must return a static section" in {
          val helper = new IncidentsDuringTransitP5Helper(CC182CType, isMultipleIncidents = true, mockReferenceDataService)
          val result = helper.incidentInformationSection.futureValue

          result mustBe a[StaticSection]
          result.rows.size mustBe 4
        }
      }

      "incidentSection" - {
        "must return a accordion section" in {
          forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
            (code, text) =>
              val updatedIncident = arbitraryIncidentType03.arbitrary.sample.value.copy(
                code = code,
                text = text
              )

              val updatedConsignment = CC182CType.Consignment.copy(
                Incident = Seq(updatedIncident)
              )

              val modifiedCC182CType = CC182CType.copy(
                Consignment = updatedConsignment
              )

              val helper = new IncidentsDuringTransitP5Helper(modifiedCC182CType, isMultipleIncidents = true, mockReferenceDataService)
              val result = helper.incidentSection(incidentIndex)

              result mustBe a[AccordionSection]
              result.sectionTitle mustBe Some("Incident 1")
              result.isOpen mustBe true
              result.rows.size mustBe 2
          }
        }
      }

      "incidentsSection" - {
        "must return 2 child accordion sections when 2 incidents defined" in {
          forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
            (code, text) =>
              val updatedIncident = arbitraryIncidentType03.arbitrary.sample.value.copy(
                code = code,
                text = text
              )

              val updatedConsignment = CC182CType.Consignment.copy(
                Incident = Seq(updatedIncident, updatedIncident)
              )

              val modifiedCC182CType = CC182CType.copy(
                Consignment = updatedConsignment
              )

              val helper = new IncidentsDuringTransitP5Helper(modifiedCC182CType, isMultipleIncidents = true, mockReferenceDataService)
              val result = helper.incidentsSection

              result mustBe a[AccordionSection]
              result.sectionTitle mustBe Some("Incidents")
              result.children.size mustBe 2

              result.children.head mustBe a[AccordionSection]
              result.children.head.isOpen mustBe true

              result.children(1) mustBe a[AccordionSection]
              result.children(1).isOpen mustBe false

              result.children.head.viewLinks.head mustBe Link(
                id = s"more-details-incident-${1}",
                text = "More details",
                href = controllers.routes.SessionExpiredController.onPageLoad().url,
                visuallyHidden = Some("more details on incident 1")
              )

              result.children(1).viewLinks.head mustBe Link(
                id = s"more-details-incident-${2}",
                text = "More details",
                href = controllers.routes.SessionExpiredController.onPageLoad().url,
                visuallyHidden = Some("more details on incident 2")
              )
          }
        }
      }
    }
  }
}
