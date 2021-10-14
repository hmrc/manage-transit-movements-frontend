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

import base.{MockNunjucksRendererApp, SpecBase}
import connectors.DeparturesMovementConnector
import matchers.JsonMatchers
import models.departure.DepartureMessageMetaData
import models.departure.DepartureStatus.DepartureSubmitted
import models.{Departure, DepartureId, Departures, LocalReferenceNumber}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyInt, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import java.time.LocalDateTime
import scala.concurrent.Future

class ViewAllDeparturesControllerSpec extends SpecBase with MockitoSugar with JsonMatchers with MockNunjucksRendererApp {

  private val mockDepartureResponse: Departures =
    Departures(
      1,
      2,
      Some(3),
      Seq(
        Departure(
          DepartureId(1),
          LocalDateTime.now(),
          LocalReferenceNumber("lrn"),
          Seq(DepartureMessageMetaData(DepartureSubmitted, LocalDateTime.now()))
        )
      )
    )

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockDeparturesMovementConnector)
  }

  val mockDeparturesMovementConnector = mock[DeparturesMovementConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DeparturesMovementConnector].toInstance(mockDeparturesMovementConnector)
      )

  "ViewAllDepartures Controller" - {

    "return OK and the correct view for a GET" in {

      when(mockDeparturesMovementConnector.getPagedDepartures(eqTo(1), anyInt())(any()))
        .thenReturn(Future.successful(Some(mockDepartureResponse)))

      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val request = FakeRequest(GET, controllers.departure.routes.ViewAllDeparturesController.onPageLoad(Some(1)).url)

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockNunjucksRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj()

      templateCaptor.getValue mustEqual "viewAllDepartures.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "render Technical difficulties page on failing to fetch departures" in {

      when(mockNunjucksRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

      when(mockDeparturesMovementConnector.getPagedDepartures(eqTo(1), anyInt())(any()))
        .thenReturn(Future.successful(None))

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val request = FakeRequest(GET, controllers.departure.routes.ViewAllDeparturesController.onPageLoad(Some(1)).url)

      val result = route(app, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR

      verify(mockNunjucksRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj()

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
