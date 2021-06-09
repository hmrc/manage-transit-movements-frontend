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

import base.SpecBase
import connectors.BetaAuthorizationConnector
import matchers.JsonMatchers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.DisplayDeparturesService

import scala.concurrent.Future

class WhatDoYouWantToDoControllerSpec extends SpecBase with MockitoSugar with JsonMatchers {

  "WhatDoYouWantToDo Controller" - {

    "return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application    = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request        = FakeRequest(GET, routes.WhatDoYouWantToDoController.onPageLoad().url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj()

      templateCaptor.getValue mustEqual "whatDoYouWantToDo.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "return BAD_REQUEST and the correct view if an invalid value is seelected" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request = FakeRequest(POST, routes.WhatDoYouWantToDoController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "somethingsWrong")

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj()

      templateCaptor.getValue mustEqual "whatDoYouWantToDo.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "redirect to index page if Arrivals is selected" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request = FakeRequest(POST, routes.WhatDoYouWantToDoController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "arrivalNotifications")

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.IndexController.onPageLoad().url)

      application.stop()
    }

    "redirect to old service interstitial page if Departures is selected and user is not beta registered" in {

      val mockDisplayDeparturesService = mock[DisplayDeparturesService]

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[DisplayDeparturesService].toInstance(mockDisplayDeparturesService)
          )
          .build()

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockDisplayDeparturesService.showDepartures(any())(any()))
        .thenReturn(Future.successful(false))

      val request = FakeRequest(POST, routes.WhatDoYouWantToDoController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "departureMakeDeclarations")

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.OldServiceInterstitialController.onPageLoad().url)

      application.stop()
    }

    "redirect to index page if Departures is selected and user is beta registered" in {

      val mockDisplayDeparturesService = mock[DisplayDeparturesService]

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[DisplayDeparturesService].toInstance(mockDisplayDeparturesService)
          )
          .build()

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockDisplayDeparturesService.showDepartures(any())(any()))
        .thenReturn(Future.successful(true))

      val request = FakeRequest(POST, routes.WhatDoYouWantToDoController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "departureMakeDeclarations")

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.IndexController.onPageLoad().url)

      application.stop()
    }

    "redirect to NI interstitial page if NI is selected" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request = FakeRequest(POST, routes.WhatDoYouWantToDoController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "northernIrelandMovements")

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.NorthernIrelandInterstitialController.onPageLoad().url)

      application.stop()
    }
  }
}
