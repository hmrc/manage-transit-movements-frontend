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

package viewModels.P5.departure

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.{CC009CType, CustomsOfficeOfDepartureType05}
import generators.Generators
import models.referenceData.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import scalaxb.XMLCalendar
import services.ReferenceDataService
import viewModels.P5.departure.DepartureCancelledP5ViewModel.DepartureCancelledP5ViewModelProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DepartureCancelledP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(api.inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "DepartureCancelledP5ViewModelSpec" - {

    val lrn                = "AB123"
    val customsReferenceId = "CD123"

    val message = arbitrary[CC009CType]
      .map {
        ie009 =>
          ie009
            .copy(
              TransitOperation = ie009.TransitOperation.copy(
                MRN = Some("mrn123")
              ),
              Invalidation = ie009.Invalidation.copy(
                decisionDateAndTime = Some(XMLCalendar("2022-07-15")),
                justification = Some("some justification")
              ),
              CustomsOfficeOfDeparture = CustomsOfficeOfDepartureType05(customsReferenceId)
            )
      }
      .sample
      .value

    val viewModelProvider = new DepartureCancelledP5ViewModelProvider(mockReferenceDataService)

    when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(fakeCustomsOffice))

    def viewModel(customsOffice: CustomsOffice): DepartureCancelledP5ViewModel =
      viewModelProvider.apply(message, lrn, customsOffice).futureValue

    "must" - {

      val result = viewModel(CustomsOffice(customsReferenceId, "", None, None))

      "return correct section" in {
        result.sections.head.sectionTitle must not be defined
        result.sections.head.rows.size mustEqual 5
      }

      "return correct title" in {
        result.title mustEqual "Declaration cancelled"
      }

      "return correct heading" in {
        result.heading mustEqual "Declaration cancelled"
      }

      "return correct paragraph" in {
        result.paragraph mustEqual s"The office of departure cancelled the declaration for LRN $lrn as requested."
      }

      "return correct hyperlink" in {
        result.hyperlink mustEqual "Make another departure declaration"
      }

      "must return correct customs office content" in {
        result.customsOfficeContent mustEqual s"If you have any questions, contact Customs office $customsReferenceId."
      }
    }

    "customsOfficeContent" - {

      "when customs office found with telephone number and name" - {
        "must return correct message" in {
          val customsOfficeName = "custName"
          val telephoneNo       = Some("123")
          val result            = viewModel(customsOffice = CustomsOffice(customsReferenceId, customsOfficeName, telephoneNo, None)).customsOfficeContent

          result mustEqual s"If you have any questions, contact Customs at $customsOfficeName on ${telephoneNo.get}."
        }
      }

      "when customs office found with name and no telephone number" - {
        "must return correct message" in {
          val customsOfficeName = "custName"
          val result            = viewModel(customsOffice = CustomsOffice(customsReferenceId, customsOfficeName, None, None)).customsOfficeContent

          result mustEqual s"If you have any questions, contact Customs at $customsOfficeName."
        }
      }

      "when customs office found with telephone number but empty name" - {
        "must return correct message" in {
          val customsOfficeName = ""
          val telephoneNo       = Some("123")
          val result            = viewModel(customsOffice = CustomsOffice(customsReferenceId, customsOfficeName, telephoneNo, None)).customsOfficeContent

          result mustEqual s"If you have any questions, contact Customs office $customsReferenceId on ${telephoneNo.get}."
        }
      }

      "when customs office found with no telephone number and empty name" - {
        "must return correct message" in {
          val customsOfficeName = ""
          val result            = viewModel(customsOffice = CustomsOffice(customsReferenceId, customsOfficeName, None, None)).customsOfficeContent

          result mustEqual s"If you have any questions, contact Customs office $customsReferenceId."
        }
      }
    }
  }

}
