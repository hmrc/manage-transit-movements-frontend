/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDateTime

import base.SpecBase
import connectors.DestinationConnector
import models.{Arrival, Arrivals}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.mockito.Matchers.any
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import play.api.inject.bind

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  val manageTransitMovementRoute = "manage-transit-movements"
  val viewArrivalNotificationUrl = s"/$manageTransitMovementRoute/view-arrivals"
  val mockDestinationConnector   = mock[DestinationConnector]

  val localDateTime: LocalDateTime = LocalDateTime.now()

  private val mockDestinationResponse: Arrivals = {
    Arrivals(
      Seq(
        Arrival(
          localDateTime,
          localDateTime,
          "Submitted",
          "test mrn"
        )
      )
    )
  }

  "Index Controller" - {

    "must return OK and the correct view for a GET with Arrivals" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("foo")))

      when(mockDestinationConnector.getArrivals()(any()))
        .thenReturn(Future.successful(mockDestinationResponse))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[DestinationConnector].toInstance(mockDestinationConnector)
        )
        .build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val expectedJson = Json.obj(
        "declareArrivalNotificationUrl" -> frontendAppConfig.declareArrivalNotificationUrl,
        "viewArrivalNotificationUrl"    -> viewArrivalNotificationUrl,
        "hasArrivals"                   -> true
      )

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

      templateCaptor.getValue mustEqual "index.njk"
      jsonCaptorWithoutConfig mustBe expectedJson

      application.stop()
    }

    "must return OK and the correct view for a GET with no Arrivals" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("foo")))

      when(mockDestinationConnector.getArrivals()(any()))
        .thenReturn(
          Future.successful(
            Arrivals(
              Seq.empty[Arrival]
            )))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[DestinationConnector].toInstance(mockDestinationConnector)
        )
        .build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val expectedJson = Json.obj(
        "declareArrivalNotificationUrl" -> frontendAppConfig.declareArrivalNotificationUrl,
        "viewArrivalNotificationUrl"    -> viewArrivalNotificationUrl,
        "hasArrivals"                   -> false
      )

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

      templateCaptor.getValue mustEqual "index.njk"
      jsonCaptorWithoutConfig mustBe expectedJson

      application.stop()
    }

    "must redirect to technical difficulties when call to getArrivals fails" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("foo")))

      when(mockDestinationConnector.getArrivals()(any()))
        .thenReturn(Future.failed(new Exception))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[DestinationConnector].toInstance(mockDestinationConnector)
        )
        .build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      application.stop()
    }
  }
}
