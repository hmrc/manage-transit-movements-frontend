/*
 * Copyright 2021 HM Revenue & Customs
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
import base.MockNunjucksRendererApp
import config.FrontendAppConfig
import generators.Generators
import matchers.JsonMatchers
import models.departure.NoReleaseForTransitMessage
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.DepartureMessageService

import scala.concurrent.Future

class NoReleaseForTransitControllerSpec extends SpecBase with MockitoSugar with JsonMatchers with Generators with MockNunjucksRendererApp {

  private val mockDepartureMessageService = mock[DepartureMessageService]

  override def beforeEach: Unit = {
    reset(
      mockDepartureMessageService
    )
    super.beforeEach
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

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockDepartureMessageService.noReleaseForTransitMessage(any())(any(), any()))
        .thenReturn(Future.successful(Some(transitMessage)))

      val request        = FakeRequest(GET, routes.NoReleaseForTransitController.onPageLoad(departureId).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj("noReleaseForTransitMessage" -> Json.toJson(transitMessage))

      templateCaptor.getValue mustEqual "noReleaseForTransit.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "render Technical difficulties page on failing to fetch noReleaseForTransitMessage" in {
      val config = app.injector.instanceOf[FrontendAppConfig]
      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

      when(mockDepartureMessageService.noReleaseForTransitMessage(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val request = FakeRequest(GET, routes.NoReleaseForTransitController.onPageLoad(departureId).url)

      val result = route(app, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj {
        "contactUrl" -> config.nctsEnquiriesUrl
      }

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
