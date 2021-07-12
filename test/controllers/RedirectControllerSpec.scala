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
import config.FrontendAppConfig
import matchers.JsonMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import play.api.inject.bind

import scala.concurrent.Future

class RedirectControllerSpec extends SpecBase with MockitoSugar with JsonMatchers with MockNunjucksRendererApp {

  val mockFrontendAppConfig = mock[FrontendAppConfig]

  override def beforeEach = {
    reset(mockFrontendAppConfig)
    super.beforeEach
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind(classOf[FrontendAppConfig]).toInstance(mockFrontendAppConfig))

  "return OK and the correct view for a GET when NI journey is enabled" in {

    when(mockRenderer.render(any(), any())(any()))
      .thenReturn(Future.successful(Html("")))
    when(mockFrontendAppConfig.isNIJourneyEnabled)
      .thenReturn(true)

    val request = FakeRequest(GET, routes.RedirectController.onPageLoad().url)

    val result = route(app, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
  }

  "return OK and the correct view for a GET when NI journey is disabled" in {

    when(mockRenderer.render(any(), any())(any()))
      .thenReturn(Future.successful(Html("")))
    when(mockFrontendAppConfig.isNIJourneyEnabled)
      .thenReturn(false)

    val request = FakeRequest(GET, routes.RedirectController.onPageLoad().url)

    val result = route(app, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual routes.WhatDoYouWantToDoController.onPageLoad().url
  }
}
