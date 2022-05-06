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

import base.{FakeSearchResultsAppConfig, SpecBase}
import config.SearchResultsAppConfig
import connectors.ArrivalMovementConnector
import generators.Generators
import models.arrival.ArrivalStatus.ArrivalSubmitted
import models.{Arrival, ArrivalId, Arrivals, RichLocalDateTime}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.{ViewArrival, ViewArrivalMovements}
import views.html.ViewArrivalsSearchResultsView

import java.time.LocalDateTime
import scala.concurrent.Future

class ViewArrivalsSearchResultsControllerSpec extends SpecBase with Generators {

  private val mockArrivalMovementConnector                   = mock[ArrivalMovementConnector]
  private val searchResultsAppConfig: SearchResultsAppConfig = FakeSearchResultsAppConfig()
  private val totalSearchArrivals                            = 8
  private val someSearchMatches                              = 5

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

  private def mockArrivalSearchResponse(retrievedArrivals: Int, totalMatched: Int): Arrivals =
    Arrivals(
      retrievedArrivals = retrievedArrivals,
      totalArrivals = totalSearchArrivals,
      totalMatched = Some(totalMatched),
      arrivals = Seq(
        Arrival(
          arrivalId = ArrivalId(1),
          created = time,
          updated = time,
          movementReferenceNumber = mrn,
          status = ArrivalSubmitted
        )
      )
    )

  private val mockViewMovement = ViewArrival(
    updatedDate = systemDefaultTime.toLocalDate,
    updatedTime = systemDefaultTime.toLocalTime,
    movementReferenceNumber = mrn,
    status = "movement.status.arrivalSubmitted",
    actions = Nil
  )

  "ViewArrivalsSearchResultsController" - {

    "return OK and the correct view for a GET when displaying search results with results" in {

      when(mockArrivalMovementConnector.getArrivalSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockArrivalSearchResponse(someSearchMatches, someSearchMatches))))

      val request = FakeRequest(
        GET,
        routes.ViewArrivalsSearchResultsController.onPageLoad(mrn).url
      )

      val result = route(app, request).value

      val view = injector.instanceOf[ViewArrivalsSearchResultsView]

      status(result) mustEqual OK

      verify(mockArrivalMovementConnector).getArrivalSearchResults(
        meq(mrn),
        meq(searchResultsAppConfig.maxSearchResults)
      )(any())

      val expectedDataRows = ViewArrivalMovements(Seq(mockViewMovement)).dataRows

      contentAsString(result) mustEqual
        view(mrn, expectedDataRows, someSearchMatches, tooManyResults = false)(request, messages).toString
    }

    "return OK and the correct view for a GET when displaying search results with too many results" in {

      when(mockArrivalMovementConnector.getArrivalSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockArrivalSearchResponse(someSearchMatches - 1, someSearchMatches))))

      val request = FakeRequest(
        GET,
        routes.ViewArrivalsSearchResultsController.onPageLoad(mrn).url
      )

      val result = route(app, request).value

      val view = injector.instanceOf[ViewArrivalsSearchResultsView]

      status(result) mustEqual OK

      verify(mockArrivalMovementConnector).getArrivalSearchResults(
        meq(mrn),
        meq(searchResultsAppConfig.maxSearchResults)
      )(any())

      val expectedDataRows = ViewArrivalMovements(Seq(mockViewMovement)).dataRows

      contentAsString(result) mustEqual
        view(mrn, expectedDataRows, someSearchMatches - 1, tooManyResults = true)(request, messages).toString
    }

    "return OK and the correct view for a GET when displaying search results with 0 results" in {

      when(mockArrivalMovementConnector.getArrivalSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockArrivalSearchResponse(0, 0))))

      val request = FakeRequest(
        GET,
        routes.ViewArrivalsSearchResultsController.onPageLoad(mrn).url
      )

      val result = route(app, request).value

      val view = injector.instanceOf[ViewArrivalsSearchResultsView]

      status(result) mustEqual OK

      verify(mockArrivalMovementConnector).getArrivalSearchResults(
        meq(mrn),
        meq(searchResultsAppConfig.maxSearchResults)
      )(any())

      val expectedDataRows = ViewArrivalMovements(Seq(mockViewMovement)).dataRows

      contentAsString(result) mustEqual
        view(mrn, expectedDataRows, 0, tooManyResults = false)(request, messages).toString
    }

    "trim search string" in {

      when(mockArrivalMovementConnector.getArrivalSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockArrivalSearchResponse(someSearchMatches, someSearchMatches))))

      val request = FakeRequest(
        GET,
        routes.ViewArrivalsSearchResultsController.onPageLoad(s" $mrn ").url
      )

      val result = route(app, request).value

      val view = injector.instanceOf[ViewArrivalsSearchResultsView]

      status(result) mustEqual OK

      verify(mockArrivalMovementConnector).getArrivalSearchResults(
        meq(mrn),
        meq(searchResultsAppConfig.maxSearchResults)
      )(any())

      val expectedDataRows = ViewArrivalMovements(Seq(mockViewMovement)).dataRows

      contentAsString(result) mustEqual
        view(mrn, expectedDataRows, someSearchMatches, tooManyResults = false)(request, messages).toString
    }

    "redirects to all arrivals on empty string" in {
      val request = FakeRequest(
        GET,
        routes.ViewArrivalsSearchResultsController.onPageLoad("").url
      )

      val result = route(app, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.ViewAllArrivalsController.onPageLoad(None).url
    }

    "render technical difficulty" in {

      when(mockArrivalMovementConnector.getArrivalSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(
        GET,
        routes.ViewArrivalsSearchResultsController.onPageLoad(mrn).url
      )

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
    }
  }
}
