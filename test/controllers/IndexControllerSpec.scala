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

import java.time.LocalDateTime
import base.SpecBase
import base.MockNunjucksRendererApp
import connectors.{ArrivalMovementConnector, DeparturesMovementConnector}
import models._
import models.departure.DepartureStatus.DepartureSubmitted
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.Configuration
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future
import play.api.inject.guice.GuiceApplicationBuilder
import config.FrontendAppConfig

class IndexControllerSpec extends SpecBase with MockNunjucksRendererApp {

  private val manageTransitMovementRoute = "manage-transit-movements"

  private val mockArrivalMovementConnector: ArrivalMovementConnector      = mock[ArrivalMovementConnector]
  private val mockDepartureMovementConnector: DeparturesMovementConnector = mock[DeparturesMovementConnector]

  private val localDateTime: LocalDateTime = LocalDateTime.now()

  private val mockDestinationResponse =
    Arrivals(Seq(Arrival(ArrivalId(1), localDateTime, localDateTime, "Submitted", "test mrn")))

  private val mockDepartureResponse =
    Departures(Seq(Departure(DepartureId(1), localDateTime, LocalReferenceNumber("GB12345"), DepartureSubmitted)))

  override def beforeEach: Unit = {
    reset(mockArrivalMovementConnector)
    reset(mockDepartureMovementConnector)
    super.beforeEach
  }

  private def expectedJson(arrivalsAvailable: Boolean, hasArrivals: Boolean, departuresAvailable: Boolean, hasDepartures: Boolean) =
    Json.obj(
      "declareArrivalNotificationUrl"  -> app.injector.instanceOf[FrontendAppConfig].declareArrivalNotificationStartUrl,
      "viewArrivalNotificationUrl"     -> s"/$manageTransitMovementRoute/view-arrivals",
      "arrivalsAvailable"              -> arrivalsAvailable,
      "hasArrivals"                    -> hasArrivals,
      "declareDepartureDeclarationUrl" -> app.injector.instanceOf[FrontendAppConfig].declareDepartureStartWithLRNUrl,
      "viewDepartureNotificationUrl"   -> s"/$manageTransitMovementRoute/view-departures",
      "departuresAvailable"            -> departuresAvailable,
      "hasDepartures"                  -> hasDepartures
    )

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(Configuration("microservice.services.features.departureJourney" -> true))
      .overrides(
        bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector),
        bind[DeparturesMovementConnector].toInstance(mockDepartureMovementConnector)
      )

  "Index Controller" - {

    "must return OK and the correct view for a GET with" - {
      "Arrivals and Departures when both respond" in {
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("foo")))

        when(mockArrivalMovementConnector.getArrivals()(any()))
          .thenReturn(Future.successful(Some(mockDestinationResponse)))

        when(mockDepartureMovementConnector.getDepartures()(any()))
          .thenReturn(Future.successful(Some(mockDepartureResponse)))

        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
        val result  = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

        templateCaptor.getValue mustEqual "index.njk"
        jsonCaptorWithoutConfig mustBe
          expectedJson(true, true, true, true)

      }

      "Arrivals and no departures when display departures services returns false" in {
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("foo")))

        when(mockArrivalMovementConnector.getArrivals()(any()))
          .thenReturn(Future.successful(Some(mockDestinationResponse)))

        when(mockDepartureMovementConnector.getDepartures()(any()))
          .thenReturn(Future.successful(Some(mockDepartureResponse)))

        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
        val result  = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

        templateCaptor.getValue mustEqual "index.njk"
        jsonCaptorWithoutConfig mustBe
          expectedJson(true, true, true, true)
      }

      "Arrivals when Departures does not respond" in {

        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("foo")))

        when(mockArrivalMovementConnector.getArrivals()(any()))
          .thenReturn(Future.successful(Some(mockDestinationResponse)))

        when(mockDepartureMovementConnector.getDepartures()(any()))
          .thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
        val result  = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

        templateCaptor.getValue mustEqual "index.njk"
        jsonCaptorWithoutConfig mustBe expectedJson(true, true, false, false)
      }

      "no Arrivals and Departures" in {
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("foo")))

        when(mockArrivalMovementConnector.getArrivals()(any()))
          .thenReturn(Future.successful(None))

        when(mockDepartureMovementConnector.getDepartures()(any()))
          .thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
        val result  = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

        templateCaptor.getValue mustEqual "index.njk"
        jsonCaptorWithoutConfig mustBe expectedJson(false, false, false, false)
      }
    }
  }
}
