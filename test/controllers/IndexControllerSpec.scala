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
import connectors.{ArrivalMovementConnector, DeparturesMovementConnector}
import models._
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import play.api.inject.bind

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  private val manageTransitMovementRoute   = "manage-transit-movements"
  private val viewArrivalNotificationUrl   = s"/$manageTransitMovementRoute/view-arrivals"
  private val viewDepartureNotificationUrl = s"/$manageTransitMovementRoute/test-only/view-departures"

  private val mockArrivalMovementConnector: ArrivalMovementConnector      = mock[ArrivalMovementConnector]
  private val mockDepartureMovementConnector: DeparturesMovementConnector = mock[DeparturesMovementConnector]

  private val localDateTime: LocalDateTime = LocalDateTime.now()

  private val mockDestinationResponse =
    Arrivals(Seq(Arrival(ArrivalId(1), localDateTime, localDateTime, "Submitted", "test mrn")))

  private val mockDepartureResponse =
    Departures(Seq(Departure(DepartureId(1), localDateTime, LocalReferenceNumber("GB12345"), "Submitted")))

  override def beforeEach: Unit = {
    reset(mockArrivalMovementConnector)
    reset(mockDepartureMovementConnector)
    super.beforeEach
  }

  private def expectedJson(arrivalsAvailable: Boolean, hasArrivals: Boolean, showDeparture: Boolean, departuresAvailable: Boolean, hasDepartures: Boolean) =
    Json.obj(
      "declareArrivalNotificationUrl"  -> frontendAppConfig.declareArrivalNotificationStartUrl,
      "viewArrivalNotificationUrl"     -> viewArrivalNotificationUrl,
      "arrivalsAvailable"              -> arrivalsAvailable,
      "hasArrivals"                    -> hasArrivals,
      "showDeparture"                  -> showDeparture,
      "declareDepartureDeclarationUrl" -> frontendAppConfig.declareDepartureStartWithLRNUrl,
      "viewDepartureNotificationUrl"   -> viewDepartureNotificationUrl,
      "departuresAvailable"            -> departuresAvailable,
      "hasDepartures"                  -> hasDepartures
    )

  def applicationBuild =
    applicationBuilder(userAnswers = None)
      .configure(Configuration("microservice.services.features.departureJourney" -> true))
      .overrides(
        bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector),
        bind[DeparturesMovementConnector].toInstance(mockDepartureMovementConnector)
      )
      .build()

  "Index Controller" - {

    "must return OK and the correct view for a GET with" - {
      "Arrivals and Departures when both respond" in {
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("foo")))

        when(mockArrivalMovementConnector.getArrivals()(any()))
          .thenReturn(Future.successful(Some(mockDestinationResponse)))

        when(mockDepartureMovementConnector.getDepartures()(any()))
          .thenReturn(Future.successful(Some(mockDepartureResponse)))

        val application = applicationBuild
        val request     = FakeRequest(GET, routes.IndexController.onPageLoad().url)
        val result      = route(application, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

        templateCaptor.getValue mustEqual "index.njk"
        jsonCaptorWithoutConfig mustBe
          expectedJson(true, true, true, true, true)

        application.stop()
      }

      "Arrivals when Departures does not respond" in {

        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("foo")))

        when(mockArrivalMovementConnector.getArrivals()(any()))
          .thenReturn(Future.successful(Some(mockDestinationResponse)))

        when(mockDepartureMovementConnector.getDepartures()(any()))
          .thenReturn(Future.successful(None))

        val application = applicationBuild
        val request     = FakeRequest(GET, routes.IndexController.onPageLoad().url)
        val result      = route(application, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

        templateCaptor.getValue mustEqual "index.njk"
        jsonCaptorWithoutConfig mustBe expectedJson(true, true, true, false, false)

        application.stop()
      }

      "no Arrivals and Departures" in {
        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("foo")))

        when(mockArrivalMovementConnector.getArrivals()(any()))
          .thenReturn(Future.successful(None))

        when(mockDepartureMovementConnector.getDepartures()(any()))
          .thenReturn(Future.successful(None))

        val application = applicationBuild
        val request     = FakeRequest(GET, routes.IndexController.onPageLoad().url)
        val result      = route(application, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

        templateCaptor.getValue mustEqual "index.njk"
        jsonCaptorWithoutConfig mustBe expectedJson(false, false, true, false, false)

        application.stop()
      }
    }
  }
}
