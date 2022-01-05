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

import base.{MockNunjucksRendererApp, SpecBase}
import config.FrontendAppConfig
import connectors.ArrivalMovementConnector
import generators.Generators
import matchers.JsonMatchers
import models.arrival.ArrivalMessageMetaData
import models.arrival.ArrivalStatus.ArrivalNotificationSubmitted
import models.{Arrival, ArrivalId, Arrivals}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.NunjucksSupport
import viewModels.pagination.PaginationViewModel
import viewModels.{ViewArrival, ViewArrivalMovements}

import java.time.LocalDateTime
import scala.concurrent.Future

class ViewAllArrivalsControllerSpec
    extends SpecBase
    with MockitoSugar
    with JsonMatchers
    with Generators
    with NunjucksSupport
    with BeforeAndAfterEach
    with MockNunjucksRendererApp {

  private val mockArrivalMovementConnector = mock[ArrivalMovementConnector]

  val localDateTime: LocalDateTime = LocalDateTime.now()

  override def beforeEach: Unit = {
    reset(mockArrivalMovementConnector)
    super.beforeEach
  }

  override def guiceApplicationBuilder() =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector),
        bind[FrontendAppConfig].toInstance(frontendAppConfig)
      )

  private val mockArrivalResponse: Arrivals =
    Arrivals(
      retrievedArrivals = 1,
      totalArrivals = 1,
      totalMatched = None,
      arrivals = Seq(
        Arrival(ArrivalId(1), localDateTime, localDateTime, Seq(ArrivalMessageMetaData(ArrivalNotificationSubmitted, localDateTime)), "test mrn")
      )
    )

  private val mockViewMovement = ViewArrival(
    localDateTime.toLocalDate,
    localDateTime.toLocalTime,
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

      val config = app.injector.instanceOf[FrontendAppConfig]
      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockArrivalMovementConnector.getPagedArrivals(any(), any())(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(
        GET,
        controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url
      )

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      verify(mockNunjucksRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj {
        "contactUrl" -> config.nctsEnquiriesUrl
      }

      val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptorWithoutConfig mustBe expectedJson
    }
  }
}
