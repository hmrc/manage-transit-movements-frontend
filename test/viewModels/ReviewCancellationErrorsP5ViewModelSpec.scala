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

package viewModels

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated._
import generators.Generators
import models.referenceData.FunctionalErrorWithDesc
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.P5.departure.ReviewCancellationErrorsP5ViewModel.ReviewCancellationErrorsP5ViewModelProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReviewCancellationErrorsP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(api.inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  override def beforeEach(): Unit =
    reset(mockReferenceDataService)

  val lrnString = "LRNAB123"

  "ReviewCancellationErrorsP5ViewModel" - {

    "when there is one error" - {

      val errors: Seq[FunctionalErrorType04] = Seq(FunctionalErrorType04("14", Number12, "MRN incorrect", None))

      when(mockReferenceDataService.getFunctionalError(any())(any(), any()))
        .thenReturn(Future.successful(FunctionalErrorWithDesc("14", "Rule violation")))

      val viewModelProvider = new ReviewCancellationErrorsP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(errors, lrnString).futureValue

      "must return correct section length" in {
        result.tableRows.length `mustBe` 1
      }
      "must return correct title" in {
        result.title `mustBe` "Review cancellation errors"
      }
      "must return correct heading" in {
        result.heading `mustBe` "Review cancellation errors"
      }
      "must return correct paragraph 1" in {
        result.paragraph1 `mustBe` s"The office of departure was not able to cancel this declaration. Review the error - then if you still want to cancel the declaration, try cancelling it again."
      }
      "must return correct paragraph 2" in {
        result.paragraph2 `mustBe` "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab)"
      }
      "must return correct hyperlink text" in {
        result.hyperlink `mustBe` "View departure declarations"
      }

      "must return correct url" in {
        result.viewDeparturesLink `mustBe` controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url
      }
    }

    "when there is multiple errors" - {
      val functionalErrors = Seq(
        FunctionalErrorType04("1", Number12, "Codelist violation", None),
        FunctionalErrorType04("2", Number14, "Rule violation", None)
      )

      when(mockReferenceDataService.getFunctionalError(any())(any(), any()))
        .thenReturn(Future.successful(FunctionalErrorWithDesc("14", "Rule violation")))
        .thenReturn(Future.successful(FunctionalErrorWithDesc("12", "Codelist violation")))

      val viewModelProvider = new ReviewCancellationErrorsP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(functionalErrors, lrnString).futureValue

      "must return correct rows size" in {
        result.tableRows.length `mustBe` 2
      }
      "must return correct title" in {
        result.title `mustBe` "Review cancellation errors"
      }
      "must return correct heading" in {
        result.heading `mustBe` "Review cancellation errors"
      }
      "must return correct paragraph 1" in {
        result.paragraph1 `mustBe` s"The office of departure was not able to cancel this declaration. Review the errors - then if you still want to cancel the declaration, try cancelling it again."
      }
      "must return correct paragraph 2" in {
        result.paragraph2 `mustBe` "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab)"
      }
      "must return correct hyperlink text" in {
        result.hyperlink `mustBe` "View departure declarations"
      }

      "must return correct url" in {
        result.viewDeparturesLink `mustBe` controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url
      }
    }
  }

}
