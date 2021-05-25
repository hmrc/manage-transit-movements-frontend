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
import connectors.DeparturesMovementConnector
import matchers.JsonMatchers
import models.departure.DepartureStatus.DepartureSubmitted
import models.{Departure, DepartureId, Departures, LocalReferenceNumber}
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

import java.time.LocalDateTime
import scala.concurrent.Future

class ViewDeparturesControllerSpec extends SpecBase with MockitoSugar with JsonMatchers {

  private val mockDepartureResponse: Departures = {
    Departures(
      Seq(
        Departure(
          DepartureId(1),
          LocalDateTime.now(),
          LocalReferenceNumber("lrn"),
          DepartureSubmitted
        )
      )
    )
  }

  "ViewDepartures Controller" - {

    "return OK and the correct view for a GET" in {

      val mockConnector                = mock[DeparturesMovementConnector]
      val mockDisplayDeparturesService = mock[DisplayDeparturesService]

      when(mockConnector.getDepartures()(any()))
        .thenReturn(Future.successful(Some(mockDepartureResponse)))

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockDisplayDeparturesService.showDepartures(any())(any()))
        .thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[DeparturesMovementConnector].toInstance(mockConnector))
        .overrides(bind[DisplayDeparturesService].toInstance(mockDisplayDeparturesService))
        .build()

      val request = FakeRequest(GET, routes.ViewDeparturesController.onPageLoad().url)

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj()

      templateCaptor.getValue mustEqual "viewDepartures.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "render Technical difficulties page on failing to fetch departures" in {

      val mockConnector                = mock[DeparturesMovementConnector]
      val mockDisplayDeparturesService = mock[DisplayDeparturesService]

      when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
      when(mockDisplayDeparturesService.showDepartures(any())(any()))
        .thenReturn(Future.successful(true))

      when(mockConnector.getDepartures()(any()))
        .thenReturn(Future.successful(None))

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[DeparturesMovementConnector].toInstance(mockConnector))
        .overrides(bind[DisplayDeparturesService].toInstance(mockDisplayDeparturesService))
        .build()

      val request = FakeRequest(GET, routes.ViewDeparturesController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj()

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to OldInterstitialController if user is not part of the private beta list" in {
      val mockDisplayDeparturesService = mock[DisplayDeparturesService]

      when(mockDisplayDeparturesService.showDepartures(any())(any()))
        .thenReturn(Future.successful(false))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[DisplayDeparturesService].toInstance(mockDisplayDeparturesService)
        )
        .build()

      val request = FakeRequest(GET, routes.ViewDeparturesController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.OldServiceInterstitialController.onPageLoad().url)

      application.stop()
    }
  }
}
