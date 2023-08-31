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
import models.arrivalP5.{CustomsOfficeOfDestinationActual, IE057Data, IE057MessageData, TransitOperationIE057}
import models.departureP5._
import models.referenceData.FunctionalErrorWithDesc
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.ErrorViewModel.ErrorViewModelProvider

import scala.concurrent.{ExecutionContext, Future}

class ErrorViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  val mrnString                                      = "MRNAB123"
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]
  implicit private val ec: ExecutionContext          = ExecutionContext.global

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(api.inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  override def beforeEach(): Unit =
    reset(mockReferenceDataService)

  "ArrivalNotificationWithFunctionalErrorsP5ViewModel" - {

    val functionalErrorReferenceData: FunctionalErrorWithDesc = FunctionalErrorWithDesc("12", "Codelist violation")

    val message: IE057Data = IE057Data(
      IE057MessageData(
        TransitOperationIE057("MRNCD3232"),
        CustomsOfficeOfDestinationActual("1234"),
        Seq(FunctionalError("14", "12", "MRN incorrect", None))
      )
    )
    when(mockReferenceDataService.getFunctionalError(any())(any(), any())).thenReturn(Future.successful(functionalErrorReferenceData))

    val viewModelProvider = new ErrorViewModelProvider(mockReferenceDataService)
    val result            = viewModelProvider.apply(message.data.functionalErrors).futureValue

    "must return correct error Code Heading" in {
      result.errorCodeHeading mustBe "Error code"
    }
    "must return correct error reason heading" in {
      result.errorReasonHeading mustBe "Reason"
    }

    " must convert functional errors to error Row" in {

      val convertedError = result.errors.head
      convertedError.errorCode mustBe s"${message.data.functionalErrors.head.errorCode} - ${functionalErrorReferenceData.description}"
      convertedError.errorReason mustBe s"${message.data.functionalErrors.head.errorReason}"

    }

  }

}
