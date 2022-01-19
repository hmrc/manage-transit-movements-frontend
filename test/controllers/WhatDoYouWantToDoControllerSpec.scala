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

package controllers

import base.{MockNunjucksRendererApp, SpecBase}
import connectors.{ArrivalMovementConnector, DeparturesMovementConnector}
import models._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

class WhatDoYouWantToDoControllerSpec extends SpecBase with MockNunjucksRendererApp {

  private val manageTransitMovementRoute   = "manage-transit-movements"
  private val viewArrivalNotificationUrl   = s"/$manageTransitMovementRoute/view-arrivals"
  private val viewDepartureNotificationUrl = s"/$manageTransitMovementRoute/view-departures"

  private val mockArrivalMovementConnector: ArrivalMovementConnector      = mock[ArrivalMovementConnector]
  private val mockDepartureMovementConnector: DeparturesMovementConnector = mock[DeparturesMovementConnector]

  override def beforeEach: Unit = {
    reset(mockArrivalMovementConnector)
    reset(mockDepartureMovementConnector)
    super.beforeEach
  }

  private def expectedJson(arrivalsAvailable: Boolean, hasArrivals: Boolean, departuresAvailable: Boolean, hasDepartures: Boolean): JsObject =
    Json.obj(
      "declareArrivalNotificationUrl"  -> frontendAppConfig.declareArrivalNotificationStartUrl,
      "viewArrivalNotificationUrl"     -> viewArrivalNotificationUrl,
      "arrivalsAvailable"              -> arrivalsAvailable,
      "hasArrivals"                    -> hasArrivals,
      "declareDepartureDeclarationUrl" -> frontendAppConfig.declareDepartureStartWithLRNUrl,
      "viewDepartureNotificationUrl"   -> viewDepartureNotificationUrl,
      "departuresAvailable"            -> departuresAvailable,
      "hasDepartures"                  -> hasDepartures,
      "isGuaranteeBalanceEnabled"      -> frontendAppConfig.isGuaranteeBalanceEnabled,
      "checkGuaranteeBalanceUrl"       -> frontendAppConfig.checkGuaranteeBalanceUrl
    )

  override def guiceApplicationBuilder() =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector),
        bind[DeparturesMovementConnector].toInstance(mockDepartureMovementConnector)
      )

  "WhatDoYouWantToDo Controller" - {

    "must return OK and the correct view for a GET with" - {

      "Arrivals and departures" in {
        when(mockNunjucksRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("foo")))

        when(mockArrivalMovementConnector.arrivalsAvailability()(any()))
          .thenReturn(Future.successful(Availability.NonEmpty))

        when(mockDepartureMovementConnector.departuresAvailability()(any()))
          .thenReturn(Future.successful(Availability.NonEmpty))

        val request = FakeRequest(GET, routes.WhatDoYouWantToDoController.onPageLoad().url)
        val result  = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockNunjucksRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

        templateCaptor.getValue mustEqual "whatDoYouWantToDo.njk"
        jsonCaptorWithoutConfig mustBe expectedJson(true, true, true, true)
      }

      "No arrivals and no departures" in {
        when(mockNunjucksRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("foo")))

        when(mockArrivalMovementConnector.arrivalsAvailability()(any()))
          .thenReturn(Future.successful(Availability.Empty))

        when(mockDepartureMovementConnector.departuresAvailability()(any()))
          .thenReturn(Future.successful(Availability.Empty))

        val request = FakeRequest(GET, routes.WhatDoYouWantToDoController.onPageLoad().url)
        val result  = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockNunjucksRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

        templateCaptor.getValue mustEqual "whatDoYouWantToDo.njk"
        jsonCaptorWithoutConfig mustBe
          expectedJson(true, false, true, false)

      }

      "No response from arrivals and departures" in {

        when(mockNunjucksRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("foo")))

        when(mockArrivalMovementConnector.arrivalsAvailability()(any()))
          .thenReturn(Future.successful(Availability.Unavailable))

        when(mockDepartureMovementConnector.departuresAvailability()(any()))
          .thenReturn(Future.successful(Availability.Unavailable))

        val request = FakeRequest(GET, routes.WhatDoYouWantToDoController.onPageLoad().url)
        val result  = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockNunjucksRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

        templateCaptor.getValue mustEqual "whatDoYouWantToDo.njk"
        jsonCaptorWithoutConfig mustBe expectedJson(false, false, false, false)

      }
    }
  }
}
