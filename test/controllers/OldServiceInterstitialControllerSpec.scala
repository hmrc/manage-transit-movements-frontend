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

package controllers

import base.{MockNunjucksRendererApp, SpecBase}
import matchers.JsonMatchers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, Json}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._
import play.twirl.api.Html
import renderer.Renderer

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class OldServiceInterstitialControllerSpec extends SpecBase with MockitoSugar with JsonMatchers with MockNunjucksRendererApp {

  "OldServiceInterstitial Controller" - {

    "return OK and the correct view for a GET" in {

      val renderer = app.injector.instanceOf[Renderer]

      val controller = new OldServiceInterstitialController(Helpers.stubMessagesControllerComponents(), renderer)

      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val request        = FakeRequest(GET, routes.OldServiceInterstitialController.onPageLoad().url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = controller.onPageLoad.apply(request)

      status(result) mustEqual OK

      verify(mockNunjucksRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj()

      templateCaptor.getValue mustEqual "oldServiceInterstitial.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
