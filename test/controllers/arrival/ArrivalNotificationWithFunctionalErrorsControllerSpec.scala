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

package controllers.arrival

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.CC057CType
import generators.Generators
import models.FunctionalErrors.FunctionalErrorsWithoutSection
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verifyNoInteractions, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{ArrivalMessageService, FunctionalErrorsService}
import viewModels.arrival.ArrivalNotificationWithFunctionalErrorsViewModel
import views.html.arrival.ArrivalNotificationWithFunctionalErrorsView

import scala.concurrent.Future

class ArrivalNotificationWithFunctionalErrorsControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockArrivalMessageService   = mock[ArrivalMessageService]
  private val mockFunctionalErrorsService = mock[FunctionalErrorsService]

  private lazy val rejectionMessageController: String =
    routes.ArrivalNotificationWithFunctionalErrorsController.onPageLoad(None, arrivalIdP5, messageId).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalMessageService)
    reset(mockFunctionalErrorsService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ArrivalMessageService].toInstance(mockArrivalMessageService),
        bind[FunctionalErrorsService].toInstance(mockFunctionalErrorsService)
      )

  "ArrivalNotificationWithFunctionalErrorsController" - {

    "must return OK and the correct view for a GET" in {
      forAll(arbitrary[CC057CType], arbitrary[FunctionalErrorsWithoutSection]) {
        (message, functionalErrors) =>
          val mrn = message.TransitOperation.MRN

          when(mockArrivalMessageService.getMessage[CC057CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))

          when(mockFunctionalErrorsService.convertErrorsWithoutSection(any())(any(), any()))
            .thenReturn(Future.successful(functionalErrors))

          val viewModel = ArrivalNotificationWithFunctionalErrorsViewModel(
            functionalErrors = functionalErrors,
            mrn = mrn,
            currentPage = None,
            numberOfErrorsPerPage = paginationAppConfig.numberOfErrorsPerPage,
            arrivalId = arrivalIdP5,
            messageId = messageId
          )

          val request = FakeRequest(GET, rejectionMessageController)

          val view = injector.instanceOf[ArrivalNotificationWithFunctionalErrorsView]

          val result = route(app, request).value

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(viewModel, arrivalIdP5)(request, messages, frontendAppConfig).toString
      }
    }

    "must redirect to technical difficulties page when functionalErrors is 0" in {
      forAll(arbitrary[CC057CType].map(_.copy(FunctionalError = Nil))) {
        message =>
          when(mockArrivalMessageService.getMessage[CC057CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))

          val request = FakeRequest(GET, rejectionMessageController)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url

          verifyNoInteractions(mockFunctionalErrorsService)
      }
    }
  }
}
