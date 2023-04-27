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
import generators.Generators
import models.departureP5._
import models.referenceData.FunctionalErrorWithDesc
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.P5.departure.RejectionMessageP5ViewModel.RejectionMessageP5ViewModelProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RejectionMessageP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(api.inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  override def beforeEach(): Unit =
    reset(mockReferenceDataService)

  val lrnString = "LRNAB123"

  "RejectionMessageP5ViewModel" - {

    val functionalErrorReferenceData = FunctionalErrorWithDesc("12", "Codelist violation")

    "when there is one error" - {

      val message: IE056Data = IE056Data(
        IE056MessageData(
          TransitOperationIE056(Some("MRNCD3232"), Some(lrnString)),
          Some(Seq(FunctionalError("14", "12", "MRN incorrect", None)))
        )
      )

      when(mockReferenceDataService.getFunctionalErrorType(any())(any(), any())).thenReturn(Future.successful(functionalErrorReferenceData))

      val viewModelProvider = new RejectionMessageP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(message.data).futureValue

      "must return correct section length" in {
        result.sections.length mustBe 1
      }

      "must return correct title" in {
        result.title mustBe "Amend declaration errors"
      }
      "must return correct heading" in {
        result.heading mustBe "Amend declaration errors"
      }
      "must return correct paragraph 1" in {
        result.paragraph1Prefix mustBe s"There is a problem with departure declaration $lrnString."
        result.paragraph1Suffix mustBe "Amend the error and resend the declaration."
      }
      "must return correct paragraph 2 prefix, link and suffix" in {
        result.paragraph2Prefix mustBe "Contact the"
        result.paragraph2Link mustBe "New Computerised Transit System helpdesk"
        result.paragraph2Suffix mustBe "for help understanding the error (opens in a new tab)."
      }
      "must return correct hyperlink text" in {
        result.hyperlink mustBe "create another departure declaration"
      }
    }

    "when there is multiple errors" - {
      val functionalErrors = Some(Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None)))

      val message: IE056Data = IE056Data(
        IE056MessageData(
          TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123")),
          functionalErrors
        )
      )

      when(mockReferenceDataService.getFunctionalErrorType(any())(any(), any())).thenReturn(Future.successful(functionalErrorReferenceData))

      val viewModelProvider = new RejectionMessageP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(message.data).futureValue

      "must return correct title" in {
        result.title mustBe "Amend declaration errors"
      }
      "must return correct heading" in {
        result.heading mustBe "Amend declaration errors"
      }
      "must return correct paragraph 1" in {
        result.paragraph1Prefix mustBe s"There is a problem with departure declaration $lrnString."
        result.paragraph1Suffix mustBe "Amend the errors and resend the declaration."
      }
      "must return correct paragraph 2 prefix, link and suffix" in {
        result.paragraph2Prefix mustBe "Contact the"
        result.paragraph2Link mustBe "New Computerised Transit System helpdesk"
        result.paragraph2Suffix mustBe "for help understanding the errors (opens in a new tab)."
      }
      "must return correct hyperlink text" in {
        result.hyperlink mustBe "create another departure declaration"
      }
    }

    "must render rows" in {

      val message: IE056Data = IE056Data(
        IE056MessageData(
          TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123")),
          Some(Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None)))
        )
      )

      when(mockReferenceDataService.getFunctionalErrorType(any())(any(), any())).thenReturn(Future.successful(functionalErrorReferenceData))

      val viewModelProvider = new RejectionMessageP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(message.data).futureValue

      result.sections.length mustBe 1
      result.sections.head.rows.size mustBe 4
    }

  }
}
