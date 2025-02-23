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
import generators.Generators
import models.departureP5.BusinessRejectionType
import models.referenceData.FunctionalErrorWithDesc
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.P5.departure.DepartureDeclarationErrorsP5ViewModel.DepartureDeclarationErrorsP5ViewModelProvider

import scala.concurrent.Future

class DepartureDeclarationErrorsP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(api.inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  override def beforeEach(): Unit =
    reset(mockReferenceDataService)

  private val lrnString = nonEmptyString.sample.value
  private val mrnString = Gen.option(nonEmptyString).sample.value

  "DepartureDeclarationErrorsP5ViewModel" - {

    val functionalErrorReferenceData = FunctionalErrorWithDesc("12", "Codelist violation")

    "when 013 rejection type" - {
      when(mockReferenceDataService.getFunctionalError(any())(any(), any()))
        .thenReturn(Future.successful(functionalErrorReferenceData))

      val viewModelProvider = new DepartureDeclarationErrorsP5ViewModelProvider()
      val result            = viewModelProvider.apply(lrnString, mrnString, BusinessRejectionType.AmendmentRejection)

      "must return correct paragraph 1" in {
        result.paragraph1 `mustBe` "There are one or more errors in this declaration that cannot be amended. Contact the helpdesk to discuss further."
      }
    }

    "when 015 rejection type" - {

      when(mockReferenceDataService.getFunctionalError(any())(any(), any()))
        .thenReturn(Future.successful(functionalErrorReferenceData))

      val viewModelProvider = new DepartureDeclarationErrorsP5ViewModelProvider()
      val result            = viewModelProvider.apply(lrnString, mrnString, BusinessRejectionType.DeclarationRejection)

      "must return correct title" in {
        result.title `mustBe` "Declaration errors"
      }
      "must return correct heading" in {
        result.heading `mustBe` "Declaration errors"
      }
      "must return correct paragraph 1" in {
        result.paragraph1 `mustBe` "There are one or more errors in this declaration that cannot be amended. Make a new declaration with the right information."
      }
      "must return correct paragraph 3" in {
        result.paragraph3 `mustBe` "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab)"
      }
      "must return correct hyperlink text" in {
        result.hyperlink.value `mustBe` "Make another departure declaration"
      }
    }
  }

}
