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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.{FunctionalErrorType04, Number12, Number14}
import generators.Generators
import models.referenceData.FunctionalErrorWithDesc
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.P5.arrival.UnloadingRemarkWithFunctionalErrorsP5ViewModel.UnloadingRemarkWithFunctionalErrorsP5ViewModelProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnloadingRemarkWithFunctionalErrorsP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(api.inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  override def beforeEach(): Unit =
    reset(mockReferenceDataService)

  val mrnString = "MRNAB123"

  "UnloadingRemarkWithFunctionalErrorsP5ViewModel" - {

    val functionalErrorReferenceData = Seq(FunctionalErrorWithDesc("12", "Codelist violation"), FunctionalErrorWithDesc("14", "Rule violation"))

    "when there is one error" - {

      val functionalErrors = Seq(FunctionalErrorType04("14", Number12, "MRN incorrect", None))

      when(mockReferenceDataService.getFunctionalErrors()(any(), any())).thenReturn(Future.successful(functionalErrorReferenceData))

      val viewModelProvider = new UnloadingRemarkWithFunctionalErrorsP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(functionalErrors, mrnString).futureValue

      "must return correct section length" in {
        result.tableRows.length mustBe 1
      }
      "must return correct title" in {
        result.title mustBe "Review unloading remarks errors"
      }
      "must return correct heading" in {
        result.heading mustBe "Review unloading remarks errors"
      }
      "must return correct paragraph 1" in {
        result.paragraph1 mustBe
          s"There is a problem with the unloading remarks for this notification. Review the error and try making the unloading remarks again."
      }
      "must return correct paragraph 2 prefix, link and suffix" in {
        result.paragraph2Prefix mustBe "Contact the"
        result.paragraph2Link mustBe "New Computerised Transit System helpdesk"
        result.paragraph2Suffix mustBe "for help understanding the error (opens in a new tab)."
      }
      "must return correct hyperlink text" in {
        result.hyperlink mustBe "View arrival notifications"
      }
    }

    "when there is multiple errors" - {
      val functionalErrors = Seq(
        FunctionalErrorType04("1", Number12, "Codelist violation", None),
        FunctionalErrorType04("2", Number14, "Rule violation", None)
      )

      when(mockReferenceDataService.getFunctionalErrors()(any(), any())).thenReturn(Future.successful(functionalErrorReferenceData))

      val viewModelProvider = new UnloadingRemarkWithFunctionalErrorsP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(functionalErrors, mrnString).futureValue

      "must return correct section length" in {
        result.tableRows.length mustBe 2
      }
      "must return correct title" in {
        result.title mustBe "Review unloading remarks errors"
      }
      "must return correct heading" in {
        result.heading mustBe "Review unloading remarks errors"
      }
      "must return correct paragraph 1" in {
        result.paragraph1 mustBe
          s"There is a problem with the unloading remarks for this notification. Review the errors and try making the unloading remarks again."
      }
      "must return correct paragraph 2 prefix, link and suffix" in {
        result.paragraph2Prefix mustBe "Contact the"
        result.paragraph2Link mustBe "New Computerised Transit System helpdesk"
        result.paragraph2Suffix mustBe "for help understanding the errors (opens in a new tab)."
      }
      "must return correct hyperlink text" in {
        result.hyperlink mustBe "View arrival notifications"
      }
    }
  }
}
