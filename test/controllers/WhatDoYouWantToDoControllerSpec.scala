/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.{ArrivalMovementConnector, DeparturesDraftsP5Connector, DeparturesMovementConnector}
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.WhatDoYouWantToDoView

import scala.concurrent.Future

class WhatDoYouWantToDoControllerSpec extends SpecBase {

  private val mockArrivalMovementConnector: ArrivalMovementConnector      = mock[ArrivalMovementConnector]
  private val mockDepartureMovementConnector: DeparturesMovementConnector = mock[DeparturesMovementConnector]
  private val mockDraftDepartureP5Connector: DeparturesDraftsP5Connector  = mock[DeparturesDraftsP5Connector]
  private val viewAllArrivalsUrl                                          = controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url
  private val viewAllDeparturesUrl                                        = controllers.departure.routes.ViewAllDeparturesController.onPageLoad(None).url

  override def beforeEach(): Unit = {
    reset(mockArrivalMovementConnector)
    reset(mockDepartureMovementConnector)
    reset(mockDraftDepartureP5Connector)
    super.beforeEach()
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector),
        bind[DeparturesMovementConnector].toInstance(mockDepartureMovementConnector),
        bind[DeparturesDraftsP5Connector].toInstance(mockDraftDepartureP5Connector)
      )
      .configure("microservice.services.features.phase5Enabled.departure" -> false, "microservice.services.features.phase5Enabled.arrival" -> false)

  "WhatDoYouWantToDoP4 Controller" - {

    "must return OK and the correct view for a GET with" - {

      "Arrivals and departures" in {

        when(mockArrivalMovementConnector.getArrivalsAvailability()(any()))
          .thenReturn(Future.successful(Availability.NonEmpty))

        when(mockDepartureMovementConnector.getDeparturesAvailability()(any()))
          .thenReturn(Future.successful(Availability.NonEmpty))

        val request = FakeRequest(GET, routes.WhatDoYouWantToDoController.onPageLoad().url)
        val result  = route(app, request).value
        val view    = injector.instanceOf[WhatDoYouWantToDoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(Availability.NonEmpty, Availability.NonEmpty, None, viewAllArrivalsUrl, viewAllDeparturesUrl)(request, messages).toString

        verifyNoInteractions(mockDraftDepartureP5Connector)
      }

      "No arrivals and no departures" in {
        when(mockArrivalMovementConnector.getArrivalsAvailability()(any()))
          .thenReturn(Future.successful(Availability.Empty))

        when(mockDepartureMovementConnector.getDeparturesAvailability()(any()))
          .thenReturn(Future.successful(Availability.Empty))

        val request = FakeRequest(GET, routes.WhatDoYouWantToDoController.onPageLoad().url)
        val result  = route(app, request).value

        val view = injector.instanceOf[WhatDoYouWantToDoView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(Availability.Empty, Availability.Empty, None, viewAllArrivalsUrl, viewAllDeparturesUrl)(request, messages).toString

        verifyNoInteractions(mockDraftDepartureP5Connector)
      }

      "No response from arrivals and departures" in {
        when(mockArrivalMovementConnector.getArrivalsAvailability()(any()))
          .thenReturn(Future.successful(Availability.Unavailable))

        when(mockDepartureMovementConnector.getDeparturesAvailability()(any()))
          .thenReturn(Future.successful(Availability.Unavailable))

        val request = FakeRequest(GET, routes.WhatDoYouWantToDoController.onPageLoad().url)
        val result  = route(app, request).value

        val view = injector.instanceOf[WhatDoYouWantToDoView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(Availability.Unavailable, Availability.Unavailable, None, viewAllArrivalsUrl, viewAllDeparturesUrl)(request, messages).toString

        verifyNoInteractions(mockDraftDepartureP5Connector)
      }
    }
  }
}
