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

import base.SpecBase
import featureFlags.DisplayDepartures
import generators.Generators
import matchers.JsonMatchers
import models.departure.ControlDecision
import models.{DepartureId, LocalReferenceNumber}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.DepartureMessageService

import scala.concurrent.Future

class ControlDecisionControllerSpec extends SpecBase with MockitoSugar with JsonMatchers with Generators {

  private val mockDepartureMessageService  = mock[DepartureMessageService]
  private val mockDisplayDeparturesService = mock[DisplayDepartures]

  override def beforeEach: Unit = {
    reset(
      mockDepartureMessageService,
      mockDisplayDeparturesService
    )
    super.beforeEach
  }

  "ControlDecision Controller" - {

    "return OK and the correct view for a GET" in {

      val controlDecision      = arbitrary[ControlDecision].sample.value
      val localReferenceNumber = arbitrary[LocalReferenceNumber].sample.value

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockDepartureMessageService.controlDecisionMessage(any())(any(), any()))
        .thenReturn(Future.successful(Some(controlDecision)))

      when(mockDisplayDeparturesService.showDepartures(any())(any()))
        .thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[DepartureMessageService].toInstance(mockDepartureMessageService),
          bind[DisplayDepartures].toInstance(mockDisplayDeparturesService)
        )
        .build()

      val request = FakeRequest(GET, routes.ControlDecisionController.onPageLoad(departureId, localReferenceNumber).url)

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj("controlDecisionMessage" -> controlDecision, "lrn" -> localReferenceNumber)

      templateCaptor.getValue mustEqual "controlDecision.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "return InternalServerError and the TechnicalDifficulties page for a failed GET " in {

      val controlDecision      = arbitrary[ControlDecision].sample.value
      val localReferenceNumber = arbitrary[LocalReferenceNumber].sample.value

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockDepartureMessageService.controlDecisionMessage(any())(any(), any()))
        .thenReturn(Future.successful(None))

      when(mockDisplayDeparturesService.showDepartures(any())(any()))
        .thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[DepartureMessageService].toInstance(mockDepartureMessageService),
          bind[DisplayDepartures].toInstance(mockDisplayDeparturesService)
        )
        .build()

      val request = FakeRequest(GET, routes.ControlDecisionController.onPageLoad(departureId, localReferenceNumber).url)

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj("nctsEnquiries" -> frontendAppConfig.nctsEnquiriesUrl)

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      application.stop()
    }

    "must redirect to OldInterstitialController if user is not part of the private beta list" in {
      when(mockDisplayDeparturesService.showDepartures(any())(any()))
        .thenReturn(Future.successful(false))

      val departureId          = DepartureId(0)
      val localReferenceNumber = arbitrary[LocalReferenceNumber].sample.value

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          inject.bind[DisplayDepartures].toInstance(mockDisplayDeparturesService)
        )
        .build()

      val request = FakeRequest(GET, routes.ControlDecisionController.onPageLoad(departureId, localReferenceNumber).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.OldServiceInterstitialController.onPageLoad().url)

      application.stop()
    }
  }
}
