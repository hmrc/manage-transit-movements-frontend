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
import models.departureP5._
import models.referenceData.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.P5.departure.DepartureCancelledP5ViewModel
import viewModels.P5.departure.DepartureCancelledP5ViewModel.DepartureCancelledP5ViewModelProvider

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DepartureCancelledP5ViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(api.inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "DepartureCancelledP5ViewModelSpec" - {

    val lrn                = "AB123"
    val customsReferenceId = "CD123"

    val ie009Data: IE009Data = IE009Data(
      IE009MessageData(
        TransitOperationIE009(
          Some("mrn123")
        ),
        Invalidation(
          Some(LocalDateTime.now()),
          Some("0"),
          "1",
          Some("some justification")
        ),
        CustomsOfficeOfDeparture(
          s"$customsReferenceId"
        )
      )
    )

    val viewModelProvider = new DepartureCancelledP5ViewModelProvider(mockReferenceDataService)

    when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any())).thenReturn(Future.successful(None))

    def viewModel(customsOffice: Option[CustomsOffice] = None): DepartureCancelledP5ViewModel =
      viewModelProvider.apply(ie009Data.data, lrn, customsReferenceId, customsOffice).futureValue

    "must return correct section" in {
      viewModel().sections.head.sectionTitle mustBe None
      viewModel().sections.head.rows.size mustBe 5
    }

    "title" - {
      "must return correct message" in {
        viewModel().title mustBe "Declaration cancelled"
      }
    }

    "heading" - {
      "must return correct message" in {
        viewModel().title mustBe "Declaration cancelled"
      }
    }

    "paragraph" in {
      viewModel().paragraph mustBe s"The office of departure cancelled the declaration for LRN $lrn as requested."
    }

    "customsOfficeContent" - {

      "when no customs office found" - {
        "must return correct message" in {
          viewModel(customsOffice = None).customsOfficeContent mustBe s"If you have any questions, contact Customs office $customsReferenceId."
        }
      }

      "when customs office found with telephone number and name" - {
        "must return correct message" in {
          val customsOfficeName = "custName"
          val telephoneNo       = Some("123")
          val result            = viewModel(customsOffice = Some(CustomsOffice(customsReferenceId, customsOfficeName, telephoneNo))).customsOfficeContent

          result mustBe s"If you have any questions, contact Customs at $customsOfficeName on ${telephoneNo.get}."
        }
      }

      "when customs office found with name and no telephone number" - {
        "must return correct message" in {
          val customsOfficeName = "custName"
          val result            = viewModel(customsOffice = Some(CustomsOffice(customsReferenceId, customsOfficeName, None))).customsOfficeContent

          result mustBe s"If you have any questions, contact Customs at $customsOfficeName."
        }
      }

      "when customs office found with telephone number but empty name" - {
        "must return correct message" in {
          val customsOfficeName = ""
          val telephoneNo       = Some("123")
          val result            = viewModel(customsOffice = Some(CustomsOffice(customsReferenceId, customsOfficeName, telephoneNo))).customsOfficeContent

          result mustBe s"If you have any questions, contact Customs office $customsReferenceId on ${telephoneNo.get}."
        }
      }

      "when customs office found with no telephone number and empty name" - {
        "must return correct message" in {
          val customsOfficeName = ""
          val result            = viewModel(customsOffice = Some(CustomsOffice(customsReferenceId, customsOfficeName, None))).customsOfficeContent

          result mustBe s"If you have any questions, contact Customs office $customsReferenceId."
        }
      }
    }

    "hyperlink" - {
      "must return correct message" in {
        viewModel().hyperlink mustBe "Make another departure declaration"
      }
    }
  }
}
