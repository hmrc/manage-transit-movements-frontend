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

import base.{FakeSearchResultsAppConfig, SpecBase}
import config.SearchResultsAppConfig
import connectors.DeparturesMovementConnector
import matchers.JsonMatchers
import models.departure.DepartureStatus.DepartureSubmitted
import models.{Departure, DepartureId, Departures, LocalReferenceNumber, RichLocalDateTime}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import viewModels.{ViewDeparture, ViewDepartureMovements}

import java.time.LocalDateTime
import scala.concurrent.Future

class ViewDeparturesSearchResultsControllerSpec extends SpecBase with JsonMatchers {

  private val totalSearchDepartures = 8
  private val someSearchMatches     = 5

  val time: LocalDateTime              = LocalDateTime.now()
  val systemDefaultTime: LocalDateTime = time.toSystemDefaultTime

  private def mockDepartureSearchResponse(retrievedDepartures: Int, totalMatched: Int): Departures =
    Departures(
      retrievedDepartures = retrievedDepartures,
      totalDepartures = totalSearchDepartures,
      totalMatched = Some(totalMatched),
      departures = Seq(
        Departure(
          DepartureId(1),
          time,
          LocalReferenceNumber("test lrn"),
          DepartureSubmitted
        )
      )
    )

  private val mockViewMovement = ViewDeparture(
    systemDefaultTime.toLocalDate,
    systemDefaultTime.toLocalTime,
    LocalReferenceNumber("test lrn"),
    "departure.status.submitted",
    Nil
  )

  private lazy val expectedJson: JsObject =
    Json.toJsObject(
      ViewDepartureMovements(Seq(mockViewMovement))
    ) ++ Json.obj(
      "declareDepartureNotificationUrl" -> frontendAppConfig.declareDepartureStartWithLRNUrl,
      "homePageUrl"                     -> controllers.routes.WhatDoYouWantToDoController.onPageLoad().url
    )

  private def expectedSearchJson(
    lrn: String,
    resultCount: Int,
    tooManyResults: Boolean
  ): JsObject = Json.obj(
    "lrn"            -> lrn,
    "resultsText"    -> s"Showing $resultCount results matching $lrn.",
    "tooManyResults" -> tooManyResults
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureMovementsConnector)
  }

  val mockDepartureMovementsConnector                         = mock[DeparturesMovementConnector]
  implicit val searchResultsAppConfig: SearchResultsAppConfig = FakeSearchResultsAppConfig()

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DeparturesMovementConnector].toInstance(mockDepartureMovementsConnector)
      )

  "ViewDeparturesSearchResultsController" - {

    "return OK and the correct view for a GET when displaying search results with results" in {

      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockDepartureMovementsConnector.getDepartureSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockDepartureSearchResponse(someSearchMatches, someSearchMatches))))

      val request = FakeRequest(
        GET,
        routes.ViewDeparturesSearchResultsController.onPageLoad("theLrn").url
      )

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value
      status(result) mustEqual OK

      verify(mockDepartureMovementsConnector).getDepartureSearchResults(
        eqTo("theLrn"),
        eqTo(searchResultsAppConfig.maxSearchResults)
      )(any())

      verify(mockNunjucksRenderer, times(1))
        .render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

      templateCaptor.getValue mustEqual "viewDeparturesSearchResults.njk"
      jsonCaptorWithoutConfig mustBe expectedJson ++
        expectedSearchJson(lrn = "theLrn", resultCount = someSearchMatches, tooManyResults = false)
    }

    "return OK and the correct view for a GET when displaying search results with too many results" in {

      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockDepartureMovementsConnector.getDepartureSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockDepartureSearchResponse(someSearchMatches - 1, someSearchMatches))))

      val request = FakeRequest(
        GET,
        routes.ViewDeparturesSearchResultsController.onPageLoad("theLrn").url
      )

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value
      status(result) mustEqual OK

      verify(mockDepartureMovementsConnector).getDepartureSearchResults(
        eqTo("theLrn"),
        eqTo(searchResultsAppConfig.maxSearchResults)
      )(any())

      verify(mockNunjucksRenderer, times(1))
        .render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

      templateCaptor.getValue mustEqual "viewDeparturesSearchResults.njk"
      jsonCaptorWithoutConfig mustBe expectedJson ++
        expectedSearchJson(lrn = "theLrn", resultCount = someSearchMatches - 1, tooManyResults = true)
    }

    "trim search string" in {

      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockDepartureMovementsConnector.getDepartureSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockDepartureSearchResponse(someSearchMatches, someSearchMatches))))

      val request = FakeRequest(
        GET,
        routes.ViewDeparturesSearchResultsController.onPageLoad(" theLrn ").url
      )

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value
      status(result) mustEqual OK

      verify(mockDepartureMovementsConnector).getDepartureSearchResults(
        eqTo("theLrn"),
        eqTo(searchResultsAppConfig.maxSearchResults)
      )(any())

      verify(mockNunjucksRenderer, times(1))
        .render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

      templateCaptor.getValue mustEqual "viewDeparturesSearchResults.njk"
      jsonCaptorWithoutConfig mustBe expectedJson ++
        expectedSearchJson(lrn = "theLrn", resultCount = someSearchMatches, tooManyResults = false)
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

    "render technical difficulty" in {

      when(mockDepartureMovementsConnector.getDepartureSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(
        GET,
        routes.ViewDeparturesSearchResultsController.onPageLoad("theLrn").url
      )

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
    }
  }
}
