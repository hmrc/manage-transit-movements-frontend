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
import generated.CC182CType
import generators.Generators
import models.{Country, IncidentCode}
import models.departureP5.DepartureReferenceNumbers
import models.referenceData.CustomsOffice
import models.{Country, LocalReferenceNumber, QualifierOfIdentification}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.P5.departure.IncidentP5ViewModel
import viewModels.P5.departure.IncidentP5ViewModel.IncidentP5ViewModelProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IncidentP5ViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  private val country  = Country("GB", "United Kingdom")
  private val incident = IncidentCode("code", "text")
  private val identification = QualifierOfIdentification("U", "UN/LOCODE")

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
    when(mockReferenceDataService.getIncidentCode(any())(any(), any()))
      .thenReturn(Future.successful(incident))
    when(mockReferenceDataService.getCountry(any())(any(), any()))
      .thenReturn(Future.successful(Right(country)))
    when(mockReferenceDataService.getQualifierOfIdentification(any())(any(), any()))
      .thenReturn(Future.successful(Right(identification)))
  }

  "IncidentP5ViewModel" - {

    val mrn                = "AB123"
    val lrn                = "LRN123"
    val customsReferenceId = "CD123"

    val departureReferenceNumbers = DepartureReferenceNumbers(lrn, Some(mrn))

    val viewModelProvider = new IncidentP5ViewModelProvider()

    val cc182Data = Arbitrary.arbitrary[CC182CType].sample.value

    def viewModel(
      cc182Data: CC182CType = cc182Data,
      customsOffice: Either[String, CustomsOffice] = Left(customsReferenceId),
      isMultipleIncidents: Boolean = true
    ): IncidentP5ViewModel =
      viewModelProvider.apply(cc182Data, mockReferenceDataService, departureReferenceNumbers, customsOffice, isMultipleIncidents, incidentIndex).futureValue

    "viewModel must have correct sections" in {
      val updatedIncident = arbitraryIncidentType03.arbitrary.sample.value.copy(
        code = incident.code,
        text = "text"
      )

      val updatedConsignment = cc182Data.Consignment.copy(
        Incident = Seq(updatedIncident, updatedIncident)
      )

      val modifiedCC182CType = cc182Data.copy(
        Consignment = updatedConsignment
      )

      val modifiedViewModel = viewModel(modifiedCC182CType)

      val sections = modifiedViewModel.sections

      sections.length mustBe 4
    }

    "title" - {
      "must return correct message" in {
        viewModel().title mustBe "Incident 1"
      }
    }

    "heading" - {
      "must return correct message" in {
        viewModel().heading mustBe "Incident 1"
      }
    }

    "when multiple incident" - {
      "paragraph1" - {
        "must return correct message" in {
          viewModel().paragraph1 mustBe
            "Multiple incidents have been reported by the customs office of incident. Review the incident details and contact the carrier for more information."
        }
      }
    }

    "when 1 incident" - {
      "paragraph1" - {
        "must return correct message" in {
          viewModel(isMultipleIncidents = false).paragraph1 mustBe
            "An incident has been reported by the customs office of incident. Review the incident details and contact the carrier for more information."
        }
      }
    }
  }
}
