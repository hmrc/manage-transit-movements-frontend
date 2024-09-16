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
import generated.{CC056CType, FunctionalErrorType04}
import generators.Generators
import models.departureP5._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AmendmentService, DepartureP5MessageService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewModels.P5.departure.RejectionMessageP5ViewModel
import viewModels.P5.departure.RejectionMessageP5ViewModel.RejectionMessageP5ViewModelProvider
import viewModels.pagination.ListPaginationViewModel
import viewModels.sections.Section
import views.html.departureP5.RejectionMessageP5View

import scala.concurrent.Future

class RejectionMessageP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockRejectionMessageP5ViewModelProvider = mock[RejectionMessageP5ViewModelProvider]
  private val mockDepartureP5MessageService           = mock[DepartureP5MessageService]
  private val mockCacheService: AmendmentService      = mock[AmendmentService]

  lazy val rejectionMessageOnPageLoadRoute: String =
    routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, messageId).url

  lazy val rejectionMessageOnSubmitRoute: String =
    routes.RejectionMessageP5Controller.onSubmit(departureIdP5, messageId).url

  val sections: Seq[Section] = arbitrarySections.arbitrary.sample.value
  val tableRow: TableRow     = arbitraryTableRow.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
    reset(mockRejectionMessageP5ViewModelProvider)
    reset(mockCacheService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[RejectionMessageP5ViewModelProvider].toInstance(mockRejectionMessageP5ViewModelProvider))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))
      .overrides(bind[AmendmentService].toInstance(mockCacheService))

  "RejectionMessageP5Controller" - {

    "must return OK and the correct view for a GET" in {
      forAll(listWithMaxLength[FunctionalErrorType04](), arbitrary[RejectionMessageP5ViewModel]) {
        (functionalErrors, viewModel) =>
          forAll(arbitrary[CC056CType].map(_.copy(FunctionalError = functionalErrors))) {
            message =>
              when(mockDepartureP5MessageService.getMessage[CC056CType](any(), any())(any(), any(), any()))
                .thenReturn(Future.successful(message))
              when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
                .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
              when(mockCacheService.isDeclarationAmendable(any(), any())(any()))
                .thenReturn(Future.successful(true))

              when(mockRejectionMessageP5ViewModelProvider.apply(any(), any(), any())(any(), any(), any()))
                .thenReturn(Future.successful(viewModel))

              val paginationViewModel = ListPaginationViewModel(
                totalNumberOfItems = message.FunctionalError.length,
                currentPage = 1,
                numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
                href = routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, messageId).url,
                additionalParams = Seq()
              )

              val request = FakeRequest(GET, rejectionMessageOnPageLoadRoute)

              val result = route(app, request).value

              status(result) mustEqual OK

              val view = injector.instanceOf[RejectionMessageP5View]

              contentAsString(result) mustEqual
                view(
                  viewModel,
                  departureIdP5,
                  messageId,
                  paginationViewModel,
                  None
                )(request, messages, frontendAppConfig).toString
          }
      }
    }

    "must redirect to tech difficulties when user cannot proceed" in {
      forAll(arbitrary[CC056CType]) {
        message =>
          when(mockDepartureP5MessageService.getMessage[CC056CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))
          when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
          when(mockCacheService.isDeclarationAmendable(any(), any())(any()))
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
            when(mockDepartureP5MessageService.getMessage[CC056CType](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(message))
            when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
              .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
            when(mockCacheService.isDeclarationAmendable(any(), any())(any()))
              .thenReturn(Future.successful(true))
            when(mockCacheService.handleErrors(any(), any())(any()))
              .thenReturn(Future.successful(httpResponse(OK)))
            when(mockCacheService.nextPage(any(), any(), any()))
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
            when(mockDepartureP5MessageService.getMessage[CC056CType](any(), any())(any(), any(), any()))
              .thenReturn(Future.successful(message))
            when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
              .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
            when(mockCacheService.isDeclarationAmendable(any(), any())(any()))
              .thenReturn(Future.successful(true))
            when(mockCacheService.handleErrors(any(), any())(any()))
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
