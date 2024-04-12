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
import generated.CC009CType
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
import services.DepartureP5MessageService
import viewModels.P5.departure.DepartureNotCancelledP5ViewModel
import viewModels.P5.departure.DepartureNotCancelledP5ViewModel.DepartureNotCancelledP5ViewModelProvider
import views.html.departureP5.DepartureNotCancelledP5View

import scala.concurrent.Future

class DepartureNotCancelledP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureNotCancelledP5ViewModelProvider = mock[DepartureNotCancelledP5ViewModelProvider]
  private val mockDepartureP5MessageService                = mock[DepartureP5MessageService]

  private val sections = arbitrarySections.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
    reset(mockDepartureNotCancelledP5ViewModelProvider)

  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .p5GuiceApplicationBuilder()
      .overrides(bind[DepartureNotCancelledP5ViewModelProvider].toInstance(mockDepartureNotCancelledP5ViewModelProvider))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))

  "DepartureCancelledP5Controller" - {

    "must return OK and the correct view for a GET" in {
      forAll(arbitrary[CC009CType]) {
        message =>
          val departureNotCancelledP5ViewModel =
            new DepartureNotCancelledP5ViewModel(sections, departureIdP5, lrn.toString)

          when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn, None)))
          when(mockDepartureP5MessageService.getMessage[CC009CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
          when(mockDepartureNotCancelledP5ViewModelProvider.apply(any(), any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(departureNotCancelledP5ViewModel))

          val request = FakeRequest(GET, controllers.departureP5.routes.DepartureNotCancelledP5Controller.onPageLoad(departureIdP5, messageId).url)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[DepartureNotCancelledP5View]

          contentAsString(result) mustEqual
            view(departureNotCancelledP5ViewModel)(request, messages, frontendAppConfig).toString
      }
    }
  }
}
