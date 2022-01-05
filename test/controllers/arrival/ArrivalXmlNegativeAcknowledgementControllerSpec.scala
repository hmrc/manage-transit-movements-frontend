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

import base.{MockNunjucksRendererApp, SpecBase}
import generators.Generators
import matchers.JsonMatchers
import models.ArrivalId
import models.arrival.XMLSubmissionNegativeAcknowledgementMessage
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import services.ArrivalMessageService

import scala.concurrent.Future

class ArrivalXmlNegativeAcknowledgementControllerSpec extends SpecBase with MockitoSugar with JsonMatchers with MockNunjucksRendererApp with Generators {
  private val mockArrivalMessageService = mock[ArrivalMessageService]

  override def beforeEach: Unit = {
    reset(mockArrivalMessageService)
    super.beforeEach
  }

  private val arrivalId = ArrivalId(1)

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(inject.bind[ArrivalMessageService].toInstance(mockArrivalMessageService))

  "ArrivalXmlNegativeAcknowledgementController Controller" - {

    "return OK and the correct view for a GET" in {
      val negativeAcknowledgementMessage = arbitrary[XMLSubmissionNegativeAcknowledgementMessage].sample.value

      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockArrivalMessageService.getXMLSubmissionNegativeAcknowledgementMessage(any())(any(), any()))
        .thenReturn(Future.successful(Some(negativeAcknowledgementMessage)))

      val request                                = FakeRequest(GET, routes.ArrivalXmlNegativeAcknowledgementController.onPageLoad(arrivalId).url)
      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockNunjucksRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "contactUrl"      -> frontendAppConfig.nctsEnquiriesUrl,
        "functionalError" -> negativeAcknowledgementMessage.error
      )

      templateCaptor.getValue mustEqual "xmlNegativeAcknowledgement.njk"
      jsonCaptor.getValue must containJson(expectedJson)

    }

    "render 'Technical difficulty page' when service fails to get rejection message" in {
      when(mockNunjucksRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockArrivalMessageService.getXMLSubmissionNegativeAcknowledgementMessage(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val expectedJson = Json.obj("nctsEnquiries" -> frontendAppConfig.nctsEnquiriesUrl)

      val request = FakeRequest(GET, routes.ArrivalXmlNegativeAcknowledgementController.onPageLoad(arrivalId).url)

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
      verify(mockNunjucksRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
