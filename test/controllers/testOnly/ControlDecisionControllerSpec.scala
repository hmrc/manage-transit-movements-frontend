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

package controllers.testOnly

import base.SpecBase
import generators.Generators
import matchers.JsonMatchers
import models.LocalReferenceNumber
import models.departure.ControlDecision
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.DepartureMessageService

import scala.concurrent.Future

class ControlDecisionControllerSpec extends SpecBase with MockitoSugar with JsonMatchers with Generators {

  private val mockDepartureMessageService = mock[DepartureMessageService]

  override def beforeEach: Unit = {
    reset(mockDepartureMessageService)
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

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[DepartureMessageService].toInstance(mockDepartureMessageService))
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

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[DepartureMessageService].toInstance(mockDepartureMessageService))
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
  }
}
