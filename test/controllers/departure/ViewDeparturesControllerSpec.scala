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

package controllers.departure

import base.{FakeFrontendAppConfig, MockNunjucksRendererApp, SpecBase}
import connectors.DeparturesMovementConnector
import matchers.JsonMatchers
import models.departure.DepartureStatus.DepartureSubmitted
import models.{Departure, DepartureId, Departures, LocalReferenceNumber}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import java.time.LocalDateTime

import config.FrontendAppConfig
import viewModels.{ViewDeparture, ViewDepartureMovements}

import scala.concurrent.Future

class ViewDeparturesControllerSpec extends SpecBase with MockitoSugar with JsonMatchers with MockNunjucksRendererApp {

  private val totalSearchDepartures = 8
  private val someSearchMatches     = 5

  val localDateTime: LocalDateTime = LocalDateTime.now()

  private val mockDepartureResponse: Departures =
    Departures(
      retrievedDepartures = 1,
      totalDepartures = 2,
      totalMatched = None,
      departures = Seq(
        Departure(
          DepartureId(1),
          LocalDateTime.now(),
          LocalReferenceNumber("test lrn"),
          DepartureSubmitted
        )
      )
    )

  private def mockDepartureSearchResponse(retrievedDepartures: Int, totalMatched: Int): Departures =
    Departures(
      retrievedDepartures = retrievedDepartures,
      totalDepartures = totalSearchDepartures,
      totalMatched = Some(totalMatched),
      departures = Seq(
        Departure(
          DepartureId(1),
          LocalDateTime.now(),
          LocalReferenceNumber("test lrn"),
          DepartureSubmitted
        )
      )
    )

  private val mockViewMovement = ViewDeparture(
    localDateTime.toLocalDate,
    localDateTime.toLocalTime,
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

  override def beforeEach: Unit = {
    super.beforeEach
    reset(mockDepartureMovementsConnector)
  }

  val mockDepartureMovementsConnector               = mock[DeparturesMovementConnector]
  implicit val frontendAppConfig: FrontendAppConfig = FakeFrontendAppConfig()

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DeparturesMovementConnector].toInstance(mockDepartureMovementsConnector)
      )

  "ViewDepartures Controller" - {

    "return OK and the correct view for a GET when displaying search results with results" in {

      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockDepartureMovementsConnector.getDepartureSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockDepartureSearchResponse(someSearchMatches, someSearchMatches))))

      val request = FakeRequest(
        GET,
        routes.ViewDeparturesController.onPageLoadSearch("theLrn").url
      )

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value
      status(result) mustEqual OK

      verify(mockDepartureMovementsConnector).getDepartureSearchResults(
        eqTo("theLrn"),
        eqTo(frontendAppConfig.maxSearchResults)
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
        routes.ViewDeparturesController.onPageLoadSearch("theLrn").url
      )

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value
      status(result) mustEqual OK

      verify(mockDepartureMovementsConnector).getDepartureSearchResults(
        eqTo("theLrn"),
        eqTo(frontendAppConfig.maxSearchResults)
      )(any())

      verify(mockNunjucksRenderer, times(1))
        .render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

      templateCaptor.getValue mustEqual "viewDeparturesSearchResults.njk"
      jsonCaptorWithoutConfig mustBe expectedJson ++
        expectedSearchJson(lrn = "theLrn", resultCount = someSearchMatches - 1, tooManyResults = true)
    }

    "render Technical difficulties page on failing to fetch departures" in {

      when(mockNunjucksRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

      when(mockDepartureMovementsConnector.getDepartures()(any()))
        .thenReturn(Future.successful(None))

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val request = FakeRequest(GET, routes.ViewAllDeparturesController.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR

      verify(mockNunjucksRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj()

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
