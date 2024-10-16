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
import models.referenceData.InvalidGuaranteeReason
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.P5.departure.GuaranteeRejectedNotAmendableP5ViewModel.GuaranteeRejectedNotAmendableP5ViewModelProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GuaranteeRejectedNotAmendableP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(api.inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  override def beforeEach(): Unit =
    reset(mockReferenceDataService)

  "GuaranteeRejectedP5ViewModel" - {

    val acceptanceDate = arbitraryXMLGregorianCalendar.arbitrary.sample.value

    "when there is one guarantee with one error" - {

      val invalidGuaranteeReasons = Seq(InvalidGuaranteeReasonType01(1, "G02", Some("text")))

      val guaranteeErrors: Seq[GuaranteeReferenceType08] = Seq(GuaranteeReferenceType08(1, "GRN", invalidGuaranteeReasons))

      when(mockReferenceDataService.getInvalidGuaranteeReason(any())(any(), any()))
        .thenReturn(Future.successful(InvalidGuaranteeReason("G02", "Guarantee exists, but not valid")))

      val viewModelProvider = new GuaranteeRejectedNotAmendableP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(guaranteeErrors, lrn.toString, mrn, acceptanceDate).futureValue

      "must return correct number of guarantees" in {
        result.tables.length `mustBe` 1
      }

      "must return correct number of errors in guarantee" in {
        result.tables.head.table.rows.length `mustBe` 1
      }

      "must return correct paragraph 1" in {
        result.paragraph1 mustBe
          "There is a problem with the guarantee in this declaration. Review the error and make a new declaration with the right information."
      }

      "must return correct paragraph 2" in {
        result.paragraph2 `mustBe` "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab)."
      }

      "must return link content" in {
        result.link `mustBe` "Make another departure declaration"
      }
    }

    "when there is one guarantee with multiple errors" - {

      val invalidGuaranteeReasons = Seq(InvalidGuaranteeReasonType01(1, "G02", Some("text")), InvalidGuaranteeReasonType01(1, "G03", Some("text")))

      val guaranteeErrors: Seq[GuaranteeReferenceType08] = Seq(GuaranteeReferenceType08(1, "GRN", invalidGuaranteeReasons))

      when(mockReferenceDataService.getInvalidGuaranteeReason(eqTo("G02"))(any(), any()))
        .thenReturn(Future.successful(InvalidGuaranteeReason("G02", "Guarantee exists, but not valid")))

      when(mockReferenceDataService.getInvalidGuaranteeReason(eqTo("G03"))(any(), any()))
        .thenReturn(Future.successful(InvalidGuaranteeReason("G03", "Access code not valid")))

      val viewModelProvider = new GuaranteeRejectedNotAmendableP5ViewModelProvider(mockReferenceDataService)
      val result            = viewModelProvider.apply(guaranteeErrors, lrn.toString, mrn, acceptanceDate).futureValue

      "must return correct number of guarantees" in {
        result.tables.length `mustBe` 1
      }

      "must return correct number of errors in guarantee" in {
        result.tables.head.table.rows.length `mustBe` 2
      }

      "must return correct paragraph 1" in {
        result.paragraph1 mustBe
          "There is a problem with the guarantee in this declaration. Review the errors and make a new declaration with the right information."
      }

      "must return correct paragraph 2" in {
        result.paragraph2 `mustBe` "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab)."
      }

      "must return link content" in {
        result.link `mustBe` "Make another departure declaration"
      }
    }

    val invalidGuaranteeReasons = Seq(InvalidGuaranteeReasonType01(1, "G02", Some("text")), InvalidGuaranteeReasonType01(1, "G03", Some("text")))

    val guaranteeErrors: Seq[GuaranteeReferenceType08] =
      Seq(GuaranteeReferenceType08(1, "GRN", invalidGuaranteeReasons), GuaranteeReferenceType08(2, "GRN2", invalidGuaranteeReasons))

    when(mockReferenceDataService.getInvalidGuaranteeReason(eqTo("G02"))(any(), any()))
      .thenReturn(Future.successful(InvalidGuaranteeReason("G02", "Guarantee exists, but not valid")))

    when(mockReferenceDataService.getInvalidGuaranteeReason(eqTo("G03"))(any(), any()))
      .thenReturn(Future.successful(InvalidGuaranteeReason("G03", "Access code not valid")))

    val viewModelProvider = new GuaranteeRejectedNotAmendableP5ViewModelProvider(mockReferenceDataService)
    val result            = viewModelProvider.apply(guaranteeErrors, lrn.toString, mrn, acceptanceDate).futureValue

    "must return correct number of guarantees" in {
      result.tables.length `mustBe` 2
    }

    "must return correct number of errors in guarantee" in {
      result.tables.head.table.rows.length `mustBe` 2
    }

    "must return correct paragraph 1" in {
      result.paragraph1 mustBe
        "There is a problem with the guarantees in this declaration. Review the errors and make a new declaration with the right information."
    }

    "must return correct paragraph 2" in {
      result.paragraph2 `mustBe` "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab)."
    }

    "must return link content" in {
      result.link `mustBe` "Make another departure declaration"
    }
  }

}
