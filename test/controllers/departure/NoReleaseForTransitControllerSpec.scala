/*
 * Copyright 2022 HM Revenue & Customs
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

import base.SpecBase
import generators.Generators
import models.departure.NoReleaseForTransitMessage
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DepartureMessageService
import views.html.departure.NoReleaseForTransitView

import scala.concurrent.Future

class NoReleaseForTransitControllerSpec extends SpecBase with Generators {

  private val mockDepartureMessageService = mock[DepartureMessageService]

  override def beforeEach(): Unit = {
    reset(mockDepartureMessageService)
    super.beforeEach()
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DepartureMessageService].toInstance(mockDepartureMessageService)
      )

  "NoReleaseForTransit Controller" - {

    "return OK and the correct view for a GET" in {

      val transitMessage = arbitrary[NoReleaseForTransitMessage].sample.value

      when(mockDepartureMessageService.noReleaseForTransitMessage(any())(any()))
        .thenReturn(Future.successful(Some(transitMessage)))

      val request = FakeRequest(GET, routes.NoReleaseForTransitController.onPageLoad(departureId).url)

      val result = route(app, request).value

      val view = injector.instanceOf[NoReleaseForTransitView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(transitMessage)(request, messages).toString
    }

    "render Technical difficulties page on failing to fetch noReleaseForTransitMessage" in {

      when(mockDepartureMessageService.noReleaseForTransitMessage(any())(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(GET, routes.NoReleaseForTransitController.onPageLoad(departureId).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
    }
  }
}
