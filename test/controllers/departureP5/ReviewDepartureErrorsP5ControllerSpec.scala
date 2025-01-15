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
import generated.CC056CType
import generators.Generators
import models.FunctionalErrors.FunctionalErrorsWithSection
import models.departureP5.*
import models.departureP5.BusinessRejectionType.DepartureBusinessRejectionType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{DepartureP5MessageService, FunctionalErrorsService}
import viewModels.P5.departure.ReviewDepartureErrorsP5ViewModel
import views.html.departureP5.ReviewDepartureErrorsP5View

import scala.concurrent.Future

class ReviewDepartureErrorsP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureP5MessageService = mock[DepartureP5MessageService]
  private val mockFunctionalErrorsService   = mock[FunctionalErrorsService]

  lazy val rejectionMessageController: String =
    routes.ReviewDepartureErrorsP5Controller.onPageLoad(None, departureIdP5, messageId).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
    reset(mockFunctionalErrorsService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService),
        bind[FunctionalErrorsService].toInstance(mockFunctionalErrorsService)
      )

  "ReviewDepartureErrorsP5Controller" - {

    "must return OK and the correct view for a GET" in {
      forAll(arbitrary[CC056CType], arbitrary[FunctionalErrorsWithSection]) {
        (message, functionalErrors) =>
          when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

          when(mockDepartureP5MessageService.getMessage[CC056CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))

          when(mockFunctionalErrorsService.convertErrorsWithSection(any())(any(), any()))
            .thenReturn(Future.successful(functionalErrors))

          val viewModel = ReviewDepartureErrorsP5ViewModel(
            functionalErrors = functionalErrors,
            lrn = lrn.value,
            businessRejectionType = DepartureBusinessRejectionType(message),
            currentPage = None,
            numberOfErrorsPerPage = paginationAppConfig.numberOfErrorsPerPage,
            departureId = departureIdP5,
            messageId = messageId
          )

          val request = FakeRequest(GET, rejectionMessageController)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[ReviewDepartureErrorsP5View]

          contentAsString(result) mustEqual
            view(viewModel, departureIdP5, None)(request, messages).toString
      }
    }
  }
}
