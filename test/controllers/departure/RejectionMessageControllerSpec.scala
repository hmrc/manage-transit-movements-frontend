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
import generated.CC056CType
import generators.Generators
import models.FunctionalErrors.FunctionalErrorsWithSection
import models.departure.*
import models.departure.BusinessRejectionType.DepartureBusinessRejectionType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{AmendmentService, DepartureMessageService, FunctionalErrorsService}
import viewModels.departure.RejectionMessageViewModel
import views.html.departure.RejectionMessageView

import scala.concurrent.Future

class RejectionMessageControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureMessageService = mock[DepartureMessageService]
  private val mockAmendmentService        = mock[AmendmentService]
  private val mockFunctionalErrorsService = mock[FunctionalErrorsService]

  lazy val rejectionMessageOnPageLoadRoute: String =
    routes.RejectionMessageController.onPageLoad(None, departureIdP5, messageId).url

  lazy val rejectionMessageOnSubmitRoute: String =
    routes.RejectionMessageController.onSubmit(departureIdP5, messageId).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureMessageService)
    reset(mockAmendmentService)
    reset(mockFunctionalErrorsService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DepartureMessageService].toInstance(mockDepartureMessageService),
        bind[AmendmentService].toInstance(mockAmendmentService),
        bind[FunctionalErrorsService].toInstance(mockFunctionalErrorsService)
      )

  "RejectionMessageController" - {

    "must return OK and the correct view" in {

      forAll(arbitrary[CC056CType], arbitrary[FunctionalErrorsWithSection]) {
        (message, functionalErrors) =>
          when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

          when(mockDepartureMessageService.getMessage[CC056CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))

          when(mockAmendmentService.isRejectionAmendable(any(), any())(any(), any()))
            .thenReturn(Future.successful(true))

          when(mockFunctionalErrorsService.convertErrorsWithSectionAndSender(any(), any())(any(), any()))
            .thenReturn(Future.successful(functionalErrors))

          val viewModel = RejectionMessageViewModel(
            functionalErrors = functionalErrors,
            lrn = lrn.value,
            businessRejectionType = DepartureBusinessRejectionType(message),
            currentPage = None,
            numberOfErrorsPerPage = paginationAppConfig.numberOfErrorsPerPage,
            departureId = departureIdP5,
            messageId = messageId
          )

          val request = FakeRequest(GET, rejectionMessageOnPageLoadRoute)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[RejectionMessageView]

          contentAsString(result) mustEqual
            view(viewModel, departureIdP5, messageId, None)(request, messages).toString
      }
    }

    "must redirect to tech difficulties when user cannot proceed" in {
      forAll(arbitrary[CC056CType]) {
        message =>
          when(mockDepartureMessageService.getMessage[CC056CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))

          when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

          when(mockAmendmentService.isRejectionAmendable(any(), any())(any(), any()))
            .thenReturn(Future.successful(false))

          val request = FakeRequest(GET, rejectionMessageOnPageLoadRoute)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
      }
    }

    "onSubmit" - {

      "must redirect to declaration summary page on success of handleErrors" in {
        forAll(arbitrary[CC056CType], nonEmptyString) {
          (message, nextPage) =>
            when(mockDepartureMessageService.getMessage[CC056CType](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(message))
            when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
              .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
            when(mockAmendmentService.isRejectionAmendable(any(), any())(any(), any()))
              .thenReturn(Future.successful(true))
            when(mockAmendmentService.handleErrors(any(), any())(any(), any()))
              .thenReturn(Future.successful(httpResponse(OK)))
            when(mockAmendmentService.nextPage(any(), any(), any()))
              .thenReturn(nextPage)

            val request = FakeRequest(POST, rejectionMessageOnSubmitRoute)

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual nextPage
        }
      }

      "must redirect to technical difficulties on failure of handleErrors" in {
        forAll(arbitrary[CC056CType]) {
          message =>
            when(mockDepartureMessageService.getMessage[CC056CType](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(message))
            when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
              .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
            when(mockAmendmentService.isRejectionAmendable(any(), any())(any(), any()))
              .thenReturn(Future.successful(true))
            when(mockAmendmentService.handleErrors(any(), any())(any(), any()))
              .thenReturn(Future.successful(httpResponse(INTERNAL_SERVER_ERROR)))

            val request = FakeRequest(POST, rejectionMessageOnSubmitRoute)

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
        }
      }
    }
  }

}
