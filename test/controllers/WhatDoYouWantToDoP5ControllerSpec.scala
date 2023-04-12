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
import connectors.{ArrivalMovementConnector, ArrivalMovementP5Connector, DeparturesDraftsP5Connector, DeparturesMovementConnector}
import generators.Generators
import models._
import models.arrivalP5.ArrivalMovements
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.WhatDoYouWantToDoView

import scala.concurrent.Future

class WhatDoYouWantToDoP5ControllerSpec extends SpecBase with Generators {

  private val mockArrivalMovementConnector: ArrivalMovementConnector         = mock[ArrivalMovementConnector]
  private val mockArrivalMovementP5Connector: ArrivalMovementP5Connector     = mock[ArrivalMovementP5Connector]
  private val mockDepartureMovementConnector: DeparturesMovementConnector    = mock[DeparturesMovementConnector]
  private val mockDepartureMovementsP5Connector: DeparturesDraftsP5Connector = mock[DeparturesDraftsP5Connector]
  private val viewAllUrl                                                     = controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None).url

  override def beforeEach(): Unit = {
    reset(mockArrivalMovementConnector)
    reset(mockArrivalMovementP5Connector)
    reset(mockDepartureMovementConnector)
    reset(mockDepartureMovementsP5Connector)
    super.beforeEach()
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector),
        bind[ArrivalMovementP5Connector].toInstance(mockArrivalMovementP5Connector),
        bind[DeparturesMovementConnector].toInstance(mockDepartureMovementConnector),
        bind[DeparturesDraftsP5Connector].toInstance(mockDepartureMovementsP5Connector)
      )
      .configure("microservice.services.features.phase5Enabled.departure" -> true)
      .configure("microservice.services.features.phase5Enabled.arrival" -> true)

  "WhatDoYouWantToDoP5 Controller" - {

    "must return OK and the correct view for a GET with" - {

      "Arrivals and departures" in {

        val arrivalMovements = arbitrary[ArrivalMovements].sample.value

        when(mockArrivalMovementConnector.getArrivalsAvailability()(any()))
          .thenReturn(Future.successful(Availability.NonEmpty))

        when(mockArrivalMovementP5Connector.getAllMovements()(any()))
          .thenReturn(Future.successful(Some(arrivalMovements)))

        when(mockDepartureMovementConnector.getDeparturesAvailability()(any()))
          .thenReturn(Future.successful(Availability.NonEmpty))

        when(mockDepartureMovementsP5Connector.getDraftDeparturesAvailability()(any()))
          .thenReturn(Future.successful(DraftAvailability.NonEmpty))

        val request = FakeRequest(GET, routes.WhatDoYouWantToDoController.onPageLoad().url)
        val result  = route(app, request).value

        val view = injector.instanceOf[WhatDoYouWantToDoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(Availability.NonEmpty, Availability.NonEmpty, Some(DraftAvailability.NonEmpty), viewAllUrl)(request, messages).toString

        verify(mockDepartureMovementsP5Connector).getDraftDeparturesAvailability()(any())
      }

      "No arrivals and no departures" in {
        when(mockArrivalMovementConnector.getArrivalsAvailability()(any()))
          .thenReturn(Future.successful(Availability.Empty))

        when(mockArrivalMovementP5Connector.getAllMovements()(any()))
          .thenReturn(Future.successful(Some(ArrivalMovements(Seq.empty))))

        when(mockDepartureMovementConnector.getDeparturesAvailability()(any()))
          .thenReturn(Future.successful(Availability.Empty))

        when(mockDepartureMovementsP5Connector.getDraftDeparturesAvailability()(any()))
          .thenReturn(Future.successful(DraftAvailability.Empty))

        val request = FakeRequest(GET, routes.WhatDoYouWantToDoController.onPageLoad().url)
        val result  = route(app, request).value

        val view = injector.instanceOf[WhatDoYouWantToDoView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(Availability.Empty, Availability.Empty, Some(DraftAvailability.Empty), viewAllUrl)(request, messages).toString

        verify(mockDepartureMovementsP5Connector).getDraftDeparturesAvailability()(any())
      }

      "No response from arrivals and departures" in {
        when(mockArrivalMovementConnector.getArrivalsAvailability()(any()))
          .thenReturn(Future.successful(Availability.Unavailable))

        when(mockArrivalMovementP5Connector.getAllMovements()(any()))
          .thenReturn(Future.successful(None))

        when(mockDepartureMovementConnector.getDeparturesAvailability()(any()))
          .thenReturn(Future.successful(Availability.Unavailable))

        when(mockDepartureMovementsP5Connector.getDraftDeparturesAvailability()(any()))
          .thenReturn(Future.successful(DraftAvailability.Unavailable))

        val request = FakeRequest(GET, routes.WhatDoYouWantToDoController.onPageLoad().url)
        val result  = route(app, request).value

        val view = injector.instanceOf[WhatDoYouWantToDoView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(Availability.Unavailable, Availability.Unavailable, Some(DraftAvailability.Unavailable), viewAllUrl)(request, messages).toString

        verify(mockDepartureMovementsP5Connector).getDraftDeparturesAvailability()(any())
      }
    }
  }
}
