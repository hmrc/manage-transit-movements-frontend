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
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import play.twirl.api.Html
import renderer.Renderer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionExpiredControllerSpec extends SpecBase with JsonMatchers with MockNunjucksRendererApp {

  "Session Expired Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockRenderer: Renderer = mock[Renderer]

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

      val controller = new SessionExpiredController(Helpers.stubMessagesControllerComponents(), mockRenderer)

      val request                                = FakeRequest(GET, routes.SessionExpiredController.onPageLoad().url)
      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = controller.onPageLoad.apply(request)

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj {
        "signInUrl" -> "/manage-transit-movements/what-do-you-want-to-do"
      }

      templateCaptor.getValue mustEqual "session-expired.njk"
      val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey
      jsonCaptorWithoutConfig mustBe expectedJson
    }
  }
}
