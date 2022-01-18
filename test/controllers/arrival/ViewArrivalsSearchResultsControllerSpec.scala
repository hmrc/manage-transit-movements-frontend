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

import base.{FakeSearchResultsAppConfig, MockNunjucksRendererApp, SpecBase}
import config.{FrontendAppConfig, SearchResultsAppConfig}
import connectors.ArrivalMovementConnector
import generators.Generators
import matchers.JsonMatchers
import models.arrival.ArrivalStatus.ArrivalSubmitted
import models.{Arrival, ArrivalId, Arrivals}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.NunjucksSupport
import viewModels.{ViewArrival, ViewArrivalMovements}
import java.time.LocalDateTime

import scala.concurrent.Future

class ViewArrivalsSearchResultsControllerSpec
    extends SpecBase
    with MockitoSugar
    with JsonMatchers
    with Generators
    with NunjucksSupport
    with BeforeAndAfterEach
    with MockNunjucksRendererApp {

  private val mockArrivalMovementConnector                    = mock[ArrivalMovementConnector]
  implicit val searchResultsAppConfig: SearchResultsAppConfig = FakeSearchResultsAppConfig()
  private val totalSearchArrivals                             = 8
  private val someSearchMatches                               = 5

  val localDateTime: LocalDateTime = LocalDateTime.now()

  override def beforeEach: Unit = {
    reset(mockArrivalMovementConnector)
    super.beforeEach
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ArrivalMovementConnector].toInstance(mockArrivalMovementConnector),
        bind[FrontendAppConfig].toInstance(frontendAppConfig)
      )

  private def mockArrivalSearchResponse(retrievedArrivals: Int, totalMatched: Int): Arrivals =
    Arrivals(
      retrievedArrivals = retrievedArrivals,
      totalArrivals = totalSearchArrivals,
      totalMatched = Some(totalMatched),
      arrivals = Seq(
        Arrival(ArrivalId(1), localDateTime, localDateTime, "test mrn", ArrivalSubmitted)
      )
    )

  private val mockViewMovement = ViewArrival(
    localDateTime.toLocalDate,
    localDateTime.toLocalTime,
    "test mrn",
    "movement.status.arrivalSubmitted",
    Nil
  )

  private lazy val expectedJson: JsObject =
    Json.toJsObject(
      ViewArrivalMovements(Seq(mockViewMovement))
    ) ++ Json.obj(
      "declareArrivalNotificationUrl" -> frontendAppConfig.declareArrivalNotificationStartUrl,
      "homePageUrl"                   -> controllers.routes.WhatDoYouWantToDoController.onPageLoad().url
    )

  private def expectedSearchJson(
    mrn: String,
    retrieved: Int,
    tooManyResults: Boolean
  ): JsObject = Json.obj(
    "mrn"            -> mrn,
    "retrieved"      -> retrieved,
    "tooManyResults" -> tooManyResults
  )

  "ViewArrivalsSearchResultsController" - {

    "return OK and the correct view for a GET when displaying search results with results" in {

      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockArrivalMovementConnector.getArrivalSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockArrivalSearchResponse(someSearchMatches, someSearchMatches))))

      val request = FakeRequest(
        GET,
        routes.ViewArrivalsSearchResultsController.onPageLoad("theMrn").url
      )

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value
      status(result) mustEqual OK

      verify(mockArrivalMovementConnector).getArrivalSearchResults(
        meq("theMrn"),
        meq(searchResultsAppConfig.maxSearchResults)
      )(any())

      verify(mockNunjucksRenderer, times(1))
        .render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

      templateCaptor.getValue mustEqual "viewArrivalsSearchResults.njk"
      jsonCaptorWithoutConfig mustBe expectedJson ++
        expectedSearchJson(mrn = "theMrn", retrieved = someSearchMatches, tooManyResults = false)
    }

    "return OK and the correct view for a GET when displaying search results with too many results" in {

      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockArrivalMovementConnector.getArrivalSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockArrivalSearchResponse(someSearchMatches - 1, someSearchMatches))))

      val request = FakeRequest(
        GET,
        routes.ViewArrivalsSearchResultsController.onPageLoad("theMrn").url
      )

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value
      status(result) mustEqual OK

      verify(mockArrivalMovementConnector).getArrivalSearchResults(
        meq("theMrn"),
        meq(searchResultsAppConfig.maxSearchResults)
      )(any())

      verify(mockNunjucksRenderer, times(1))
        .render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

      templateCaptor.getValue mustEqual "viewArrivalsSearchResults.njk"
      jsonCaptorWithoutConfig mustBe expectedJson ++
        expectedSearchJson(mrn = "theMrn", retrieved = someSearchMatches - 1, tooManyResults = true)
    }

    "return OK and the correct view for a GET when displaying search results with 0 results" in {

      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockArrivalMovementConnector.getArrivalSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockArrivalSearchResponse(0, 0))))

      val request = FakeRequest(
        GET,
        routes.ViewArrivalsSearchResultsController.onPageLoad("theMrn").url
      )

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value
      status(result) mustEqual OK

      verify(mockArrivalMovementConnector).getArrivalSearchResults(
        meq("theMrn"),
        meq(searchResultsAppConfig.maxSearchResults)
      )(any())

      verify(mockNunjucksRenderer, times(1))
        .render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

      templateCaptor.getValue mustEqual "viewArrivalsSearchResults.njk"
      jsonCaptorWithoutConfig mustBe expectedJson ++
        Json.obj(
          "mrn"            -> "theMrn",
          "retrieved"      -> 0,
          "tooManyResults" -> false
        )
    }

    "trim search string" in {

      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockArrivalMovementConnector.getArrivalSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(Some(mockArrivalSearchResponse(someSearchMatches, someSearchMatches))))

      val request = FakeRequest(
        GET,
        routes.ViewArrivalsSearchResultsController.onPageLoad(" theMrn ").url
      )

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value
      status(result) mustEqual OK

      verify(mockArrivalMovementConnector).getArrivalSearchResults(
        meq("theMrn"),
        meq(searchResultsAppConfig.maxSearchResults)
      )(any())

      verify(mockNunjucksRenderer, times(1))
        .render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

      templateCaptor.getValue mustEqual "viewArrivalsSearchResults.njk"
      jsonCaptorWithoutConfig mustBe expectedJson ++
        expectedSearchJson(mrn = "theMrn", retrieved = someSearchMatches, tooManyResults = false)
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

      val config = app.injector.instanceOf[FrontendAppConfig]
      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockArrivalMovementConnector.getArrivalSearchResults(any(), any())(any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(
        GET,
        routes.ViewArrivalsSearchResultsController.onPageLoad("theMrn").url
      )

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      verify(mockNunjucksRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj {
        "contactUrl" -> config.nctsEnquiriesUrl
      }

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
