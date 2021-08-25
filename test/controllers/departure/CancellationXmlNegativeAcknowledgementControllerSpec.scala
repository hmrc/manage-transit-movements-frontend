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

import base.{MockNunjucksRendererApp, SpecBase}
import config.FrontendAppConfig
import controllers.actions.FakeIdentifierAction
import generators.Generators
import matchers.JsonMatchers
import models.DepartureId
import models.arrival.XMLSubmissionNegativeAcknowledgementMessage
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import renderer.Renderer
import services.DepartureMessageService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CancellationXmlNegativeAcknowledgementControllerSpec extends SpecBase with MockitoSugar with JsonMatchers with Generators with MockNunjucksRendererApp {

  private val mockDepartureMessageService = mock[DepartureMessageService]
  private val mockFrontendAppConfig       = mock[FrontendAppConfig]

  private val renderer = app.injector.instanceOf[Renderer]

  override def beforeEach: Unit = {
    reset(
      mockDepartureMessageService
    )
    super.beforeEach
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DepartureMessageService].toInstance(mockDepartureMessageService),
        bind[FrontendAppConfig].toInstance(mockFrontendAppConfig)
      )

  "CancellationXmlNegativeAcknowledgement Controller" - {

    "return OK and the correct view for a GET" in {
      val negativeAcknowledgementMessage = arbitrary[XMLSubmissionNegativeAcknowledgementMessage].sample.value

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockDepartureMessageService.getXMLSubmissionNegativeAcknowledgementMessage(any())(any(), any()))
        .thenReturn(Future.successful(Some(negativeAcknowledgementMessage)))

      val controller = new CancellationXmlNegativeAcknowledgementController(
        messagesApi             = messagesApi,
        identify                = FakeIdentifierAction(),
        cc                      = stubMessagesControllerComponents(),
        renderer                = renderer,
        frontendAppConfig       = mockFrontendAppConfig,
        departureMessageService = mockDepartureMessageService
      )

      val request        = FakeRequest(GET, routes.CancellationXmlNegativeAcknowledgementController.onPageLoad(departureId).url)
      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val result = controller.onPageLoad(DepartureId(0)).apply(request)

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "contactUrl"             -> mockFrontendAppConfig.nctsEnquiriesUrl,
        "confirmCancellationUrl" -> mockFrontendAppConfig.departureFrontendConfirmCancellationUrl(departureId),
        "functionalError"        -> negativeAcknowledgementMessage.error
      )

      templateCaptor.getValue mustEqual "cancellationXmlNegativeAcknowledgement.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }

    "render 'Technical difficulty page' when service fails to get rejection message" in {
      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))
      when(mockDepartureMessageService.getXMLSubmissionNegativeAcknowledgementMessage(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

      val expectedJson = Json.obj("nctsEnquiries" -> frontendAppConfig.nctsEnquiriesUrl)

      val request = FakeRequest(GET, routes.DepartureXmlNegativeAcknowledgementController.onPageLoad(departureId).url)

      val result = route(app, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      jsonCaptor.getValue must containJson(expectedJson)
    }
  }
}
