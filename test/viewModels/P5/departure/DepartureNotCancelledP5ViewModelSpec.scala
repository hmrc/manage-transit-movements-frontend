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

import base.SpecBase
import generated.{CC009CType, CustomsOfficeOfDepartureType03}
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import scalaxb.XMLCalendar
import services.ReferenceDataService
import viewModels.P5.departure.DepartureNotCancelledP5ViewModel.DepartureNotCancelledP5ViewModelProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DepartureNotCancelledP5ViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(api.inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "DepartureNotCancelledP5ViewModelSpec" - {

    val lrn                = "AB123"
    val customsReferenceId = "CD123"

    val x = arbitrary[CC009CType].sample.value

    val message = x
      .copy(TransitOperation = x.TransitOperation.copy(MRN = Some("mrn123")))
      .copy(Invalidation =
        x.Invalidation.copy(
          requestDateAndTime = Some(XMLCalendar("2022-07-15")),
          justification = Some("some justification")
        )
      )
      .copy(CustomsOfficeOfDeparture = CustomsOfficeOfDepartureType03(customsReferenceId))

    val viewModelProvider = new DepartureNotCancelledP5ViewModelProvider(mockReferenceDataService)

    when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(fakeCustomsOffice))

    val viewModel: DepartureNotCancelledP5ViewModel = viewModelProvider.apply(message, departureIdP5, lrn).futureValue

    "must return correct section" in {
      viewModel.sections.size `mustBe` 1
      viewModel.sections.head.sectionTitle `mustBe` None
    }

    "title" - {
      "must return correct message" in {
        viewModel.title `mustBe` "Declaration not cancelled"
      }
    }

    "heading" - {
      "must return correct message" in {
        viewModel.title `mustBe` "Declaration not cancelled"
      }
    }

    "paragraph" in {
      viewModel.paragraph `mustBe` s"The office of departure could not cancel the declaration for LRN $lrn as requested."
    }

    "hyperlink" - {
      "must return correct message" in {
        viewModel.hyperlink `mustBe` "Make another departure declaration"
      }
    }

    "tryAgainUrl" - {
      "must return correct url" in {
        viewModel.tryAgainUrl `mustBe` s"http://localhost:10122/manage-transit-movements/cancellation/$departureIdP5/index/$lrn"
      }
    }
  }
}
