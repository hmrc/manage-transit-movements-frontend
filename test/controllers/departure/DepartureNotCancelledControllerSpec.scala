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
import generated.CC009CType
import generators.Generators
import models.departure.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.DepartureMessageService
import viewModels.departure.DepartureNotCancelledViewModel.DepartureNotCancelledViewModelProvider
import viewModels.departure.DepartureNotCancelledViewModel
import views.html.departure.DepartureNotCancelledView

import scala.concurrent.Future

class DepartureNotCancelledControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureNotCancelledViewModelProvider = mock[DepartureNotCancelledViewModelProvider]
  private val mockDepartureMessageService                = mock[DepartureMessageService]

  private val sections = arbitrarySections.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureMessageService)
    reset(mockDepartureNotCancelledViewModelProvider)

  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureNotCancelledViewModelProvider].toInstance(mockDepartureNotCancelledViewModelProvider))
      .overrides(bind[DepartureMessageService].toInstance(mockDepartureMessageService))

  "DepartureCancelledController" - {

    "must return OK and the correct view for a GET" in {
      forAll(arbitrary[CC009CType]) {
        message =>
          val departureNotCancelledP5ViewModel =
            new DepartureNotCancelledViewModel(sections, departureIdP5, lrn.toString)

          when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
          when(mockDepartureMessageService.getMessage[CC009CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
          when(mockDepartureNotCancelledViewModelProvider.apply(any(), any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(departureNotCancelledP5ViewModel))

          val request = FakeRequest(GET, controllers.departure.routes.DepartureNotCancelledController.onPageLoad(departureIdP5, messageId).url)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[DepartureNotCancelledView]

          contentAsString(result) mustEqual
            view(departureNotCancelledP5ViewModel)(request, messages, frontendAppConfig).toString
      }
    }
  }

}
