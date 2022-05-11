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

package controllers.departure

import base.SpecBase
import connectors.DeparturesMovementConnector
import models.departure.DepartureStatus.DepartureSubmitted
import models.{Departure, DepartureId, Departures, RichLocalDateTime}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.pagination.PaginationViewModel
import viewModels.{ViewAllDepartureMovementsViewModel, ViewDeparture}
import views.html.departure.ViewAllDeparturesView

import java.time.LocalDateTime
import scala.concurrent.Future

class ViewAllDeparturesControllerSpec extends SpecBase {

  private val mockDeparturesMovementConnector = mock[DeparturesMovementConnector]

  private val time: LocalDateTime              = LocalDateTime.now()
  private val systemDefaultTime: LocalDateTime = time.toSystemDefaultTime

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDeparturesMovementConnector)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DeparturesMovementConnector].toInstance(mockDeparturesMovementConnector)
      )

  private val mockDepartureResponse: Departures = Departures(
    retrievedDepartures = 1,
    totalDepartures = 2,
    totalMatched = Some(3),
    departures = Seq(
      Departure(
        departureId = DepartureId(1),
        updated = LocalDateTime.now(),
        localReferenceNumber = lrn,
        status = DepartureSubmitted
      )
    )
  )

  private val mockViewMovement = ViewDeparture(
    updatedDate = systemDefaultTime.toLocalDate,
    updatedTime = systemDefaultTime.toLocalTime,
    localReferenceNumber = lrn,
    status = "departure.status.submitted",
    actions = Nil
  )

  "ViewAllDepartures Controller" - {

    "return OK and the correct view for a GET" - {

      "when page provided" in {

        when(mockDeparturesMovementConnector.getPagedDepartures(any(), any())(any()))
          .thenReturn(Future.successful(Some(mockDepartureResponse)))

        val currentPage = 1

        val request = FakeRequest(GET, controllers.departure.routes.ViewAllDeparturesController.onPageLoad(Some(currentPage)).url)

        val result = route(app, request).value

        val view = injector.instanceOf[ViewAllDeparturesView]

        status(result) mustEqual OK

        val expectedPaginationViewModel = PaginationViewModel(
          totalNumberOfMovements = mockDepartureResponse.totalDepartures,
          currentPage = currentPage,
          numberOfMovementsPerPage = paginationAppConfig.arrivalsNumberOfMovements,
          href = routes.ViewAllDeparturesController.onPageLoad(None).url
        )
        val expectedViewModel = ViewAllDepartureMovementsViewModel(Seq(mockViewMovement), expectedPaginationViewModel)

        contentAsString(result) mustEqual
          view(expectedViewModel)(request, messages).toString
      }

      "when page not provided must default to 1" in {

        when(mockDeparturesMovementConnector.getPagedDepartures(any(), any())(any()))
          .thenReturn(Future.successful(Some(mockDepartureResponse)))

        val request = FakeRequest(GET, controllers.departure.routes.ViewAllDeparturesController.onPageLoad(None).url)

        val result = route(app, request).value

        val view = injector.instanceOf[ViewAllDeparturesView]

        status(result) mustEqual OK

        val expectedPaginationViewModel = PaginationViewModel(
          totalNumberOfMovements = mockDepartureResponse.totalDepartures,
          currentPage = 1,
          numberOfMovementsPerPage = paginationAppConfig.arrivalsNumberOfMovements,
          href = routes.ViewAllDeparturesController.onPageLoad(None).url
        )
        val expectedViewModel = ViewAllDepartureMovementsViewModel(Seq(mockViewMovement), expectedPaginationViewModel)

        contentAsString(result) mustEqual
          view(expectedViewModel)(request, messages).toString
      }
    }

    "render technical difficulties page on failing to fetch departures" in {

      when(mockDeparturesMovementConnector.getPagedDepartures(any(), any())(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(GET, controllers.departure.routes.ViewAllDeparturesController.onPageLoad(None).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
    }
  }
}
