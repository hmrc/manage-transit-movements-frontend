/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import java.time.{LocalDate, LocalTime}

import base.SpecBase
import config.FrontendAppConfig
import connectors.DestinationConnector
import generators.ModelGenerators
import matchers.JsonMatchers
import models.Movement
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.viewmodels.NunjucksSupport
import viewModels.ViewArrivalMovements

import scala.concurrent.Future

class ViewArrivalNotificationsControllerSpec
    extends SpecBase
    with MockitoSugar
    with JsonMatchers
    with ModelGenerators
    with NunjucksSupport {

  private val mockDestinationConnector = mock[DestinationConnector]

  val localDate = LocalDate.now()
  val localTime = LocalTime.now()

  val mockConnectorResponse = {
    Seq(
      Movement(
        localDate,
        localTime,
        "test mrn",
        "test name",
        "test presentation office",
        "normal"
      ),
      Movement(
        localDate,
        localTime,
        "test mrn",
        "test name",
        "test presentation office",
        "normal"
      )
    )
  }


  "ViewArrivalNotifications Controller" - {
    "return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      when(mockDestinationConnector.getArrivalMovements()(any(), any()))
        .thenReturn(Future.successful(mockConnectorResponse))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides {
          bind[DestinationConnector].toInstance(mockDestinationConnector)
        }
        .build()

      implicit val feAppConfig = application.injector.instanceOf[FrontendAppConfig]

      val request = FakeRequest(
        GET,
        routes.ViewArrivalNotificationsController.onPageLoad().url
      )

      val expectedJson = Json.toJsObject(ViewArrivalMovements(mockConnectorResponse))

      val templateCaptor = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value
      status(result) mustEqual OK

      verify(mockRenderer, times(1))
        .render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val jsonCaptorWithoutConfig: JsObject = jsonCaptor.getValue - configKey

      templateCaptor.getValue mustEqual "viewArrivalNotifications.njk"
      jsonCaptorWithoutConfig mustBe expectedJson

      application.stop()
    }
  }
}
