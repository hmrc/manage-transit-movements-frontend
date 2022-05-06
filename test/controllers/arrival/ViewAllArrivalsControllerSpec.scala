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
import models.arrival.ArrivalStatus.ArrivalSubmitted
import models.{Arrival, ArrivalId, Arrivals, RichLocalDateTime}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.pagination.PaginationViewModel
import viewModels.{ViewAllArrivalMovementsViewModel, ViewArrival}
import views.html.ViewAllArrivalsView

import java.time.LocalDateTime
import scala.concurrent.Future

class ViewAllArrivalsControllerSpec extends SpecBase with Generators {

  private val mockArrivalMovementConnector = mock[ArrivalMovementConnector]

  private val time: LocalDateTime              = LocalDateTime.now()
  private val systemDefaultTime: LocalDateTime = time.toSystemDefaultTime

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

  private val mockArrivalResponse: Arrivals = Arrivals(
    retrievedArrivals = 1,
    totalArrivals = 1,
    totalMatched = None,
    arrivals = Seq(
      Arrival(
        arrivalId = ArrivalId(1),
        created = time,
        updated = time,
        movementReferenceNumber = "test mrn",
        status = ArrivalSubmitted
      )
    )
  )

  private val mockViewMovement = ViewArrival(
    updatedDate = systemDefaultTime.toLocalDate,
    updatedTime = systemDefaultTime.toLocalTime,
    movementReferenceNumber = "test mrn",
    status = "movement.status.arrivalSubmitted",
    actions = Nil
  )

  "ViewAllArrivals Controller" - {
    "return OK and the correct view for a GET" in {

      when(mockArrivalMovementConnector.getPagedArrivals(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockArrivalResponse)))

      val currentPage = 1

      val request = FakeRequest(GET, controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(Some(currentPage)).url)

      val result = route(app, request).value

      val view = injector.instanceOf[ViewAllArrivalsView]

      status(result) mustEqual OK

      val expectedPaginationViewModel = PaginationViewModel(
        totalNumberOfMovements = mockArrivalResponse.totalArrivals,
        currentPage = currentPage,
        numberOfMovementsPerPage = paginationAppConfig.arrivalsNumberOfMovements,
        href = routes.ViewAllArrivalsController.onPageLoad(None).url
      )
      val expectedViewModel = ViewAllArrivalMovementsViewModel(Seq(mockViewMovement), expectedPaginationViewModel)

      contentAsString(result) mustEqual
        view(expectedViewModel)(request, messages).toString
    }

    "render technical difficulties when no paged arrivals retrieved" in {

      when(mockArrivalMovementConnector.getPagedArrivals(any(), any())(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(GET, controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
    }
  }
}
