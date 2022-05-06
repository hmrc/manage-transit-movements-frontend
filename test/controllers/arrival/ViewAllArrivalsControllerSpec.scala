/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.arrival

import base.SpecBase
import connectors.ArrivalMovementConnector
import generators.Generators
import matchers.JsonMatchers
import models.arrival.ArrivalStatus.ArrivalSubmitted
import models.{Arrival, ArrivalId, Arrivals, RichLocalDateTime}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.NunjucksSupport
import viewModels.pagination.PaginationViewModel
import viewModels.{ViewArrival, ViewArrivalMovements}

import java.time.LocalDateTime
import scala.concurrent.Future

class ViewAllArrivalsControllerSpec extends SpecBase with JsonMatchers with Generators with NunjucksSupport {

  private val mockArrivalMovementConnector = mock[ArrivalMovementConnector]

  val time: LocalDateTime              = LocalDateTime.now()
  val systemDefaultTime: LocalDateTime = time.toSystemDefaultTime

  override def beforeEach(): Unit = {
    reset(mockArrivalMovementConnector)
    super.beforeEach()
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector)
      )

  private val mockArrivalResponse: Arrivals =
    Arrivals(
      retrievedArrivals = 1,
      totalArrivals = 1,
      totalMatched = None,
      arrivals = Seq(
        Arrival(ArrivalId(1), time, time, "test mrn", ArrivalSubmitted)
      )
    )

  private val mockViewMovement = ViewArrival(
    systemDefaultTime.toLocalDate,
    systemDefaultTime.toLocalTime,
    "test mrn",
    "movement.status.arrivalSubmitted",
    Nil
  )

  private lazy val expectedJson =
    Json.toJsObject(
      ViewArrivalMovements(Seq(mockViewMovement))
    ) ++ Json.obj(
      "declareArrivalNotificationUrl" -> frontendAppConfig.declareArrivalNotificationStartUrl,
      "homePageUrl"                   -> controllers.routes.WhatDoYouWantToDoController.onPageLoad().url,
      "singularOrPlural"              -> "numberOfMovements.singular"
    ) ++ Json.toJsObject(
      PaginationViewModel(1, 1, 20, controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url)
    )

  "ViewAllArrivals Controller" - {
    "return OK and the correct view for a GET" in {

      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockArrivalMovementConnector.getPagedArrivals(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockArrivalResponse)))

      val request = FakeRequest(
        GET,
        controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(Some(1)).url
      )

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value
      status(result) mustEqual OK

      verify(mockNunjucksRenderer, times(1))
        .render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

      templateCaptor.getValue mustEqual "viewAllArrivals.njk"
      jsonCaptorWithoutConfig mustBe expectedJson
    }

    "render technical difficulty" in {

      when(mockArrivalMovementConnector.getPagedArrivals(any(), any())(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(
        GET,
        controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url
      )

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
    }
  }
}
