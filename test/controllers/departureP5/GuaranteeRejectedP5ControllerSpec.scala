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

package controllers.departureP5

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.CC055CType
import generators.Generators
import models.GuaranteeReference
import models.departureP5.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{AmendmentService, DepartureP5MessageService, FunctionalErrorsService}
import viewModels.P5.departure.GuaranteeRejectedP5ViewModel
import viewModels.P5.departure.GuaranteeRejectedP5ViewModel.GuaranteeRejectedP5ViewModelProvider
import views.html.departureP5.GuaranteeRejectedP5View

import scala.concurrent.Future

class GuaranteeRejectedP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureP5MessageService             = mock[DepartureP5MessageService]
  private val mockAmendmentService: AmendmentService    = mock[AmendmentService]
  private val mockGuaranteeRejectionP5ViewModelProvider = mock[GuaranteeRejectedP5ViewModelProvider]
  private val mockFunctionalErrorsService               = mock[FunctionalErrorsService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
    reset(mockAmendmentService)
    reset(mockGuaranteeRejectionP5ViewModelProvider)
    reset(mockFunctionalErrorsService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService),
        bind[AmendmentService].toInstance(mockAmendmentService),
        bind[GuaranteeRejectedP5ViewModelProvider].toInstance(mockGuaranteeRejectionP5ViewModelProvider),
        bind[FunctionalErrorsService].toInstance(mockFunctionalErrorsService)
      )

  "GuaranteeRejected" - {

    lazy val controller = routes.GuaranteeRejectedP5Controller.onPageLoad(departureIdP5, messageId).url

    "onPageLoad" - {
      "when rejection is amendable" - {
        "must return OK and the correct view for a GET" in {
          forAll(arbitrary[CC055CType], listWithMaxLength[GuaranteeReference](), arbitrary[GuaranteeRejectedP5ViewModel]) {
            (message, guaranteeReferences, viewModel) =>
              when(mockDepartureP5MessageService.getMessage[CC055CType](any(), any())(any(), any(), any()))
                .thenReturn(Future.successful(message))

              when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
                .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

              when(mockFunctionalErrorsService.convertGuaranteeReferences(any())(any(), any()))
                .thenReturn(Future.successful(guaranteeReferences))

              when(mockAmendmentService.isRejectionAmendable(any(), any())(any(), any())) `thenReturn` Future.successful(true)

              when(mockGuaranteeRejectionP5ViewModelProvider.apply(any(), any(), any(), any())(any()))
                .thenReturn(viewModel)

              val request = FakeRequest(GET, controller)

              val result = route(app, request).value

              status(result) mustEqual OK

              val view = injector.instanceOf[GuaranteeRejectedP5View]

              contentAsString(result) mustEqual
                view(viewModel, departureIdP5, messageId)(request, messages).toString
          }
        }
      }

      "when rejection is not amendable" - {
        "must redirect to GuaranteeRejectedNotAmendableP5Controller" in {
          forAll(arbitrary[CC055CType], listWithMaxLength[GuaranteeReference](), arbitrary[GuaranteeRejectedP5ViewModel]) {
            (message, guaranteeReferences, viewModel) =>
              when(mockDepartureP5MessageService.getMessage[CC055CType](any(), any())(any(), any(), any()))
                .thenReturn(Future.successful(message))

              when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
                .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

              when(mockFunctionalErrorsService.convertGuaranteeReferences(any())(any(), any()))
                .thenReturn(Future.successful(guaranteeReferences))

              when(mockAmendmentService.isRejectionAmendable(any(), any())(any(), any())) `thenReturn` Future.successful(false)

              when(mockGuaranteeRejectionP5ViewModelProvider.apply(any(), any(), any(), any())(any()))
                .thenReturn(viewModel)

              val request = FakeRequest(GET, controller)

              val result = route(app, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value `mustBe`
                controllers.departureP5.routes.GuaranteeRejectedNotAmendableP5Controller.onPageLoad(departureIdP5, messageId).url
          }
        }
      }
    }

    "onSubmit" - {

      lazy val controller = routes.GuaranteeRejectedP5Controller.onSubmit(departureIdP5, messageId).url

      "must redirect to NewLocalReferenceNumber page on success" in {
        forAll(arbitrary[CC055CType]) {
          message =>
            when(mockDepartureP5MessageService.getMessage[CC055CType](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(message))

            when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
              .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

            when(mockAmendmentService.handleErrors(any(), any())(any(), any()))
              .thenReturn(Future.successful(httpResponse(OK)))

            val request = FakeRequest(POST, controller)

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value `mustBe` frontendAppConfig.departureFrontendTaskListUrl(lrn.value)
        }
      }

      "must redirect to technical difficulties page on failure" in {
        forAll(arbitrary[CC055CType]) {
          message =>
            when(mockDepartureP5MessageService.getMessage[CC055CType](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(message))

            when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
              .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

            when(mockAmendmentService.handleErrors(any(), any())(any(), any()))
              .thenReturn(Future.successful(httpResponse(INTERNAL_SERVER_ERROR)))

            val request = FakeRequest(POST, controller)

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value `mustBe` controllers.routes.ErrorController.technicalDifficulties().url
        }
      }
    }
  }
}
