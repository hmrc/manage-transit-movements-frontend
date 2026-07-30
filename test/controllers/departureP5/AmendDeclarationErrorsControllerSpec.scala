/*
 * Copyright 2026 HM Revenue & Customs
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
import connectors.DepartureCacheConnector
import generated.{CC022CType, FunctionalErrorType01}
import generators.Generators
import models.departureP5.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.DepartureP5MessageService
import viewModels.P5.departure.AmendDeclarationErrorsViewModel
import viewModels.P5.departure.AmendDeclarationErrorsViewModel.AmendDeclarationErrorsViewModelProvider
import views.html.departureP5.AmendDeclarationErrorsView

import scala.concurrent.Future

class AmendDeclarationErrorsControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureP5MessageService             = mock[DepartureP5MessageService]
  private val mockCacheService: DepartureCacheConnector = mock[DepartureCacheConnector]

  private val mockViewModelProvider = mock[AmendDeclarationErrorsViewModelProvider]

  lazy val amendDeclarationErrorsController: String =
    controllers.departureP5.routes.AmendDeclarationErrorsController.onPageLoad(departureIdP5, messageId).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
    reset(mockCacheService)
    reset(mockViewModelProvider)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService),
        bind[DepartureCacheConnector].toInstance(mockCacheService),
        bind[AmendDeclarationErrorsViewModelProvider].toInstance(mockViewModelProvider)
      )

  "AmendDeclarationErrorsController" - {

    "must return OK and the correct view for a GET when no Errors" in {
      forAll(arbitrary[CC022CType].map(_.copy(FunctionalError = Nil)), Gen.option(nonEmptyString), arbitrary[AmendDeclarationErrorsViewModel]) {
        (message, mrn, viewModel) =>
          beforeEach()

          when(mockDepartureP5MessageService.getMessage[CC022CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))

          when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, mrn)))

          when(mockViewModelProvider.apply(any(), any()))
            .thenReturn(viewModel)

          val request = FakeRequest(GET, amendDeclarationErrorsController)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[AmendDeclarationErrorsView]

          contentAsString(result) mustEqual
            view(viewModel)(request, messages, frontendAppConfig).toString

          verify(mockViewModelProvider).apply(eqTo(lrn.value), eqTo(mrn))
      }
    }

    "must redirect to technical difficulties page when functionalErrors is between 1 to 10" in {
      forAll(listWithMaxLength[FunctionalErrorType01](), Gen.option(nonEmptyString)) {
        (functionalErrors, mrn) =>
          forAll(arbitrary[CC022CType].map(_.copy(FunctionalError = functionalErrors))) {
            message =>
              when(mockDepartureP5MessageService.getMessage[CC022CType](any(), any())(any(), any(), any()))
                .thenReturn(Future.successful(message))

              when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
                .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, mrn)))

              val request = FakeRequest(GET, amendDeclarationErrorsController)

              val result = route(app, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
          }
      }
    }
  }

}
