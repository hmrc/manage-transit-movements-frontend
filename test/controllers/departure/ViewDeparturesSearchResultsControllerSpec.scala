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

package controllers.departure

import base.{FakeSearchResultsAppConfig, SpecBase}
import config.SearchResultsAppConfig
import connectors.DeparturesMovementConnector
import forms.SearchFormProvider
import generators.Generators
import models.departure.DepartureStatus.DepartureSubmitted
import models.{Departure, DepartureId, Departures, LocalReferenceNumber, RichLocalDateTime}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.{ViewDeparture, ViewDepartureMovements}
import views.html.departure.ViewDeparturesSearchResultsView

import java.time.LocalDateTime
import scala.concurrent.Future

class ViewDeparturesSearchResultsControllerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val totalSearchDepartures = 8
  private val someSearchMatches     = 5

  private val time: LocalDateTime              = LocalDateTime.now()
  private val systemDefaultTime: LocalDateTime = time.toSystemDefaultTime

  private val formProvider = new SearchFormProvider()
  private val form         = formProvider("departures.search.form.value.invalid")

  private def mockDepartureSearchResponse(retrievedDepartures: Int, totalMatched: Int): Departures =
    Departures(
      retrievedDepartures = retrievedDepartures,
      totalDepartures = totalSearchDepartures,
      totalMatched = Some(totalMatched),
      departures = Seq(
        Departure(
          departureId = DepartureId(1),
          updated = time,
          localReferenceNumber = LocalReferenceNumber(lrn.toString),
          status = DepartureSubmitted
        )
      )
    )

  private val mockViewMovement = ViewDeparture(
    updatedDate = systemDefaultTime.toLocalDate,
    updatedTime = systemDefaultTime.toLocalTime,
    localReferenceNumber = LocalReferenceNumber(lrn.toString),
    status = "departure.status.submitted",
    actions = Nil
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureMovementsConnector)
  }

  private val mockDepartureMovementsConnector                = mock[DeparturesMovementConnector]
  private val searchResultsAppConfig: SearchResultsAppConfig = FakeSearchResultsAppConfig()

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DeparturesMovementConnector].toInstance(mockDepartureMovementsConnector)
      )

  "ViewDeparturesSearchResultsController" - {

    "return OK and the correct view for a GET when displaying search results with results" in {

      when(mockDepartureMovementsConnector.getDepartureSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockDepartureSearchResponse(someSearchMatches, someSearchMatches))))

      val request = FakeRequest(
        GET,
        routes.ViewDeparturesSearchResultsController.onPageLoad(lrn.toString).url
      )

      val result = route(app, request).value

      val view = injector.instanceOf[ViewDeparturesSearchResultsView]

      status(result) mustEqual OK

      verify(mockDepartureMovementsConnector).getDepartureSearchResults(
        eqTo(lrn.toString),
        eqTo(searchResultsAppConfig.maxSearchResults)
      )(any())

      val expectedDataRows = ViewDepartureMovements(Seq(mockViewMovement)).dataRows

      contentAsString(result) mustEqual
        view(form.fill(lrn.toString), lrn.toString, expectedDataRows, someSearchMatches, tooManyResults = false)(request, messages).toString
    }

    "return OK and the correct view for a GET when displaying search results with too many results" in {

      when(mockDepartureMovementsConnector.getDepartureSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockDepartureSearchResponse(someSearchMatches - 1, someSearchMatches))))

      val request = FakeRequest(
        GET,
        routes.ViewDeparturesSearchResultsController.onPageLoad(lrn.toString).url
      )

      val result = route(app, request).value

      val view = injector.instanceOf[ViewDeparturesSearchResultsView]

      status(result) mustEqual OK

      verify(mockDepartureMovementsConnector).getDepartureSearchResults(
        eqTo(lrn.toString),
        eqTo(searchResultsAppConfig.maxSearchResults)
      )(any())

      val expectedDataRows = ViewDepartureMovements(Seq(mockViewMovement)).dataRows

      contentAsString(result) mustEqual
        view(form.fill(lrn.toString), lrn.toString, expectedDataRows, someSearchMatches - 1, tooManyResults = true)(request, messages).toString
    }

    "return OK and the correct view for a GET when displaying search results with 0 results" in {

      when(mockDepartureMovementsConnector.getDepartureSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockDepartureSearchResponse(0, 0))))

      val request = FakeRequest(
        GET,
        routes.ViewDeparturesSearchResultsController.onPageLoad(lrn.toString).url
      )

      val result = route(app, request).value

      val view = injector.instanceOf[ViewDeparturesSearchResultsView]

      status(result) mustEqual OK

      verify(mockDepartureMovementsConnector).getDepartureSearchResults(
        eqTo(lrn.toString),
        eqTo(searchResultsAppConfig.maxSearchResults)
      )(any())

      val expectedDataRows = ViewDepartureMovements(Seq(mockViewMovement)).dataRows

      contentAsString(result) mustEqual
        view(form.fill(lrn.toString), lrn.toString, expectedDataRows, 0, tooManyResults = false)(request, messages).toString
    }

    "trim search string" in {

      when(mockDepartureMovementsConnector.getDepartureSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockDepartureSearchResponse(someSearchMatches, someSearchMatches))))

      val request = FakeRequest(
        GET,
        routes.ViewDeparturesSearchResultsController.onPageLoad(s" $lrn ").url
      )

      val result = route(app, request).value

      val view = injector.instanceOf[ViewDeparturesSearchResultsView]

      status(result) mustEqual OK

      verify(mockDepartureMovementsConnector).getDepartureSearchResults(
        eqTo(lrn.toString),
        eqTo(searchResultsAppConfig.maxSearchResults)
      )(any())

      val expectedDataRows = ViewDepartureMovements(Seq(mockViewMovement)).dataRows

      contentAsString(result) mustEqual
        view(form.fill(lrn.toString), lrn.toString, expectedDataRows, someSearchMatches, tooManyResults = false)(request, messages).toString
    }

    "redirects to all departures on empty string" in {
      val request = FakeRequest(
        GET,
        routes.ViewDeparturesSearchResultsController.onPageLoad("").url
      )

      val result = route(app, request).value
      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.ViewAllDeparturesController.onPageLoad(None).url
    }

    "must redirect back to view departures search results when valid data is submitted" in {

      forAll(arbitrary[LocalReferenceNumber]) {
        case LocalReferenceNumber(lrn) =>
          val request = FakeRequest(POST, routes.ViewDeparturesSearchResultsController.onSubmit(lrn).url)
            .withFormUrlEncodedBody(("value", lrn))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.ViewDeparturesSearchResultsController.onPageLoad(lrn).url
      }
    }

    "render technical difficulty when connector returns None" in {

      when(mockDepartureMovementsConnector.getDepartureSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(
        GET,
        routes.ViewDeparturesSearchResultsController.onPageLoad(lrn.toString).url
      )

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
    }
  }
}
