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
import connectors.DepartureCacheConnector
import generated.{CC056CType, FunctionalErrorType02}
import generators.Generators
import models.departure.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.DepartureMessageService
import viewModels.departure.DepartureDeclarationErrorsViewModel.DepartureDeclarationErrorsViewModelProvider
import viewModels.departure.DepartureDeclarationErrorsViewModel
import views.html.departure.DepartureDeclarationErrorsView

import scala.concurrent.Future

class DepartureDeclarationErrorsControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureMessageService               = mock[DepartureMessageService]
  private val mockCacheService: DepartureCacheConnector = mock[DepartureCacheConnector]

  private val mockViewModelProvider = mock[DepartureDeclarationErrorsViewModelProvider]

  lazy val departureDeclarationErrorsController: String =
    controllers.departure.routes.DepartureDeclarationErrorsController.onPageLoad(departureIdP5, messageId).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureMessageService)
    reset(mockCacheService)
    reset(mockViewModelProvider)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DepartureMessageService].toInstance(mockDepartureMessageService),
        bind[DepartureCacheConnector].toInstance(mockCacheService),
        bind[DepartureDeclarationErrorsViewModelProvider].toInstance(mockViewModelProvider)
      )

  "DepartureDeclarationErrorsController" - {

    "must return OK and the correct view for a GET when no Errors" in {
      forAll(arbitrary[CC056CType].map(_.copy(FunctionalError = Nil)), Gen.option(nonEmptyString), arbitrary[DepartureDeclarationErrorsViewModel]) {
        (message, mrn, viewModel) =>
          beforeEach()

          when(mockDepartureMessageService.getMessage[CC056CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))

          when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, mrn)))

          when(mockViewModelProvider.apply(any(), any(), any()))
            .thenReturn(viewModel)

          val request = FakeRequest(GET, departureDeclarationErrorsController)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[DepartureDeclarationErrorsView]

          contentAsString(result) mustEqual
            view(viewModel)(request, messages, frontendAppConfig).toString

          verify(mockViewModelProvider).apply(eqTo(lrn.value), eqTo(mrn), any())
      }
    }

    "must redirect to technical difficulties page when functionalErrors is between 1 to 10" in {
      forAll(listWithMaxLength[FunctionalErrorType02](), Gen.option(nonEmptyString)) {
        (functionalErrors, mrn) =>
          forAll(arbitrary[CC056CType].map(_.copy(FunctionalError = functionalErrors))) {
            message =>
              when(mockDepartureMessageService.getMessage[CC056CType](any(), any())(any(), any(), any()))
                .thenReturn(Future.successful(message))

              when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
                .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, mrn)))

              val request = FakeRequest(GET, departureDeclarationErrorsController)

              val result = route(app, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
          }
      }
    }
  }

}
