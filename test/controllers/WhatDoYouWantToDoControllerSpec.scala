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
import matchers.JsonMatchers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.test.Helpers
import play.twirl.api.Html

import scala.concurrent.Future
import play.api.inject.guice.GuiceApplicationBuilder

class WhatDoYouWantToDoControllerSpec extends SpecBase with MockitoSugar with JsonMatchers with BeforeAndAfterEach {

  val mockRenderer: NunjucksRenderer  = mock[NunjucksRenderer]
  val mockArrivalMovementConnector    = mock[ArrivalMovementConnector]
  val mockDeparturesMovementConnector = mock[DeparturesMovementConnector]
  val mockFrontendAppConfig           = mock[FrontendAppConfig]

  override def beforeEach {
    Mockito.reset(
      mockRenderer
    )
    super.beforeEach()
  }

  "WhatDoYouWantToDo Controller" - {

    "return OK and the correct view for a GET when NI is not enabled" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      when(mockArrivalMovementConnector.getArrivals()(any()))
        .thenReturn(Future.successful(None))

      when(mockArrivalMovementConnector.getDepartures()(any()))
        .thenReturn(Future.successful(None))

      when(mockFrontendAppConfig.isNIJourneyEnabled)
        .thenReturn(false)

      val controller = new WhatDoYouWantToDoController(
        messagesApi                 = messagesApi,
        identify                    = FakeIdentifierAction(),
        cc                          = stubControllerComponents(),
        renderer                    = mockRenderer,
        formProvider                = new WhatDoYouWantToDoFormProvider,
        arrivalMovementConnector    = mockArrivalMovementConnector,
        departuresMovementConnector = mockDeparturesMovementConnector,
        appConfig                   = mockFrontendAppConfig
      )

      val request = FakeRequest(GET, controller.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj()

      templateCaptor.getValue mustEqual "whatDoYouWantToDo.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "return OK and the correct view for a GET when NI is enabled" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      when(mockArrivalMovementConnector.getArrivals()(any()))
        .thenReturn(Future.successful(None))

      when(mockArrivalMovementConnector.getDepartures()(any()))
        .thenReturn(Future.successful(None))

      when(mockFrontendAppConfig.isNIJourneyEnabled)
        .thenReturn(true)

      val controller = new WhatDoYouWantToDoController(
        messagesApi                 = messagesApi,
        identify                    = FakeIdentifierAction(),
        cc                          = stubControllerComponents(),
        renderer                    = mockRenderer,
        formProvider                = new WhatDoYouWantToDoFormProvider,
        arrivalMovementConnector    = mockArrivalMovementConnector,
        departuresMovementConnector = mockDeparturesMovementConnector,
        appConfig                   = mockFrontendAppConfig
      )

      val request = FakeRequest(GET, controller.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj()

      templateCaptor.getValue mustEqual "index.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "return BAD_REQUEST and the correct view if an invalid value is selected" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

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
    }

    "redirect to index page if GB Movements is selected" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request = FakeRequest(POST, routes.WhatDoYouWantToDoController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "gbMovements")

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.IndexController.onPageLoad().url)

    }

    "redirect to NI interstitial page if NI is selected" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      // val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request = FakeRequest(POST, routes.WhatDoYouWantToDoController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "northernIrelandMovements")

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.routes.NorthernIrelandInterstitialController.onPageLoad().url)
    }
  }
}
