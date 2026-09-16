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

package controllers.departure

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.CC055CType
import generators.Generators
import models.GuaranteeReference
import models.departure.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{AmendmentService, DepartureMessageService, FunctionalErrorsService}
import viewModels.departure.GuaranteeRejectedViewModel.GuaranteeRejectedViewModelProvider
import viewModels.departure.GuaranteeRejectedViewModel
import views.html.departure.GuaranteeRejectedView

import scala.concurrent.Future

class GuaranteeRejectedControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureMessageService             = mock[DepartureMessageService]
  private val mockAmendmentService: AmendmentService  = mock[AmendmentService]
  private val mockGuaranteeRejectionViewModelProvider = mock[GuaranteeRejectedViewModelProvider]
  private val mockFunctionalErrorsService             = mock[FunctionalErrorsService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureMessageService)
    reset(mockAmendmentService)
    reset(mockGuaranteeRejectionViewModelProvider)
    reset(mockFunctionalErrorsService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DepartureMessageService].toInstance(mockDepartureMessageService),
        bind[AmendmentService].toInstance(mockAmendmentService),
        bind[GuaranteeRejectedViewModelProvider].toInstance(mockGuaranteeRejectionViewModelProvider),
        bind[FunctionalErrorsService].toInstance(mockFunctionalErrorsService)
      )

  "GuaranteeRejected" - {

    lazy val controller = routes.GuaranteeRejectedController.onPageLoad(departureIdP5, messageId).url

    "onPageLoad" - {
      "when rejection is amendable" - {
        "must return OK and the correct view for a GET" in {
          forAll(arbitrary[CC055CType], listWithMaxLength[GuaranteeReference](), arbitrary[GuaranteeRejectedViewModel]) {
            (message, guaranteeReferences, viewModel) =>
              when(mockDepartureMessageService.getMessage[CC055CType](any(), any())(any(), any(), any()))
                .thenReturn(Future.successful(message))

              when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
                .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

              when(mockFunctionalErrorsService.convertGuaranteeReferences(any())(any(), any()))
                .thenReturn(Future.successful(guaranteeReferences))

              when(mockAmendmentService.isRejectionAmendable(any(), any())(any(), any())) `thenReturn` Future.successful(true)

              when(mockGuaranteeRejectionViewModelProvider.apply(any(), any(), any(), any())(any()))
                .thenReturn(viewModel)

              val request = FakeRequest(GET, controller)

              val result = route(app, request).value

              status(result) mustEqual OK

              val view = injector.instanceOf[GuaranteeRejectedView]

              contentAsString(result) mustEqual
                view(viewModel, departureIdP5, messageId)(request, messages).toString
          }
        }
      }

      "when rejection is not amendable" - {
        "must redirect to GuaranteeRejectedNotAmendableController" in {
          forAll(arbitrary[CC055CType], listWithMaxLength[GuaranteeReference](), arbitrary[GuaranteeRejectedViewModel]) {
            (message, guaranteeReferences, viewModel) =>
              when(mockDepartureMessageService.getMessage[CC055CType](any(), any())(any(), any(), any()))
                .thenReturn(Future.successful(message))

              when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
                .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

              when(mockFunctionalErrorsService.convertGuaranteeReferences(any())(any(), any()))
                .thenReturn(Future.successful(guaranteeReferences))

              when(mockAmendmentService.isRejectionAmendable(any(), any())(any(), any())) `thenReturn` Future.successful(false)

              when(mockGuaranteeRejectionViewModelProvider.apply(any(), any(), any(), any())(any()))
                .thenReturn(viewModel)

              val request = FakeRequest(GET, controller)

              val result = route(app, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual
                controllers.departure.routes.GuaranteeRejectedNotAmendableController.onPageLoad(departureIdP5, messageId).url
          }
        }
      }
    }

    "onSubmit" - {

      lazy val controller = routes.GuaranteeRejectedController.onSubmit(departureIdP5, messageId).url

      "must redirect to NewLocalReferenceNumber page on success" in {
        forAll(arbitrary[CC055CType]) {
          message =>
            when(mockDepartureMessageService.getMessage[CC055CType](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(message))

            when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
              .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

            when(mockAmendmentService.handleErrors(any(), any())(any(), any()))
              .thenReturn(Future.successful(httpResponse(OK)))

            val request = FakeRequest(POST, controller)

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual frontendAppConfig.departureFrontendTaskListUrl(lrn.value)
        }
      }

      "must redirect to technical difficulties page on failure" in {
        forAll(arbitrary[CC055CType]) {
          message =>
            when(mockDepartureMessageService.getMessage[CC055CType](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(message))

            when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
              .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

            when(mockAmendmentService.handleErrors(any(), any())(any(), any()))
              .thenReturn(Future.successful(httpResponse(INTERNAL_SERVER_ERROR)))

            val request = FakeRequest(POST, controller)

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
        }
      }
    }
  }
}
