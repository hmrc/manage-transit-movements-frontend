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
import forms.ConfirmCancellationFormProvider
import matchers.JsonMatchers
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.ConfirmCancellationPage
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import repositories.SessionRepository
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.Future

class ConfirmCancellationControllerSpec extends SpecBase with MockitoSugar with MockNunjucksRendererApp with JsonMatchers {

  def onwardRoute = Call("GET", "/foo")

  val formProvider: ConfirmCancellationFormProvider = new ConfirmCancellationFormProvider()
  val form: Form[Boolean]                           = formProvider()

  lazy val confirmCancellationRoute = routes.ConfirmCancellationController.onPageLoad(lrn).url

  "ConfirmCancellation Controller" - {

    "must return OK and the correct view for a GET" in {
      dataRetrievalWithData(emptyUserAnswers)

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application    = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request        = FakeRequest(GET, confirmCancellationRoute)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "confirmCancellation.njk"

      application.stop()
    }

    "must redirect to the next page when valid data is submitted" in {

      dataRetrievalWithData(emptyUserAnswers)

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilders(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      val request =
        FakeRequest(POST, confirmCancellationRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

//    "must return a Bad Request and errors when invalid data is submitted" in {
//
//      dataRetrievalWithData(emptyUserAnswers)
//
//      when(mockRenderer.render(any(), any())(any()))
//        .thenReturn(Future.successful(Html("")))
//
//      val application    = applicationBuilders(userAnswers = Some(emptyUserAnswers)).build()
//      val request        = FakeRequest(POST, confirmCancellationRoute).withFormUrlEncodedBody(("value", ""))
//      val boundForm      = form.bind(Map("value" -> ""))
//      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
//      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])
//
//      val result = route(application, request).value
//
//      status(result) mustEqual BAD_REQUEST
//
//      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())
//
//      val expectedJson = Json.obj(
//      "form" -> boundForm,
//      "lrn"  -> lrn,
//      "mode" -> NormalMode
//      )
//
//      templateCaptor.getValue mustEqual "confirmCancellation.njk"
//      jsonCaptor.getValue must containJson(expectedJson)
//
//      application.stop()
//    }

    "must redirect to Session Expired for a POST if no existing data is found" in {
      dataRetrievalNoData()

      val application = applicationBuilders(userAnswers = None).build()

      val request =
        FakeRequest(POST, confirmCancellationRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
