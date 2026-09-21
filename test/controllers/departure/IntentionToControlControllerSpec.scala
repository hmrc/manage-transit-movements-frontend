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

import base.{AppWithDefaultMockFixtures, SpecBase}
import config.Constants.NotificationType.*
import generated.*
import generators.Generators
import models.departure.*
import models.referenceData.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{DepartureMessageService, ReferenceDataService}
import viewModels.departure.IntentionToControlViewModel.IntentionToControlViewModelProvider
import viewModels.departure.IntentionToControlViewModel
import views.html.departure.IntentionToControlView

import scala.concurrent.Future

class IntentionToControlControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockIntentionToControlViewModelProvider = mock[IntentionToControlViewModelProvider]
  private val mockReferenceDataService                = mock[ReferenceDataService]
  private val mockDepartureMessageService             = mock[DepartureMessageService]

  private val sections = arbitrarySections.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
    reset(mockDepartureMessageService)
    reset(mockIntentionToControlViewModelProvider)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[IntentionToControlViewModelProvider].toInstance(mockIntentionToControlViewModelProvider))
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))
      .overrides(bind[DepartureMessageService].toInstance(mockDepartureMessageService))

  private val customsOffice = arbitrary[CustomsOffice].sample.value

  "IntentionToControlController Controller" - {

    "must return OK and the correct view for a GET" in {
      forAll(arbitrary[CC060CType].map {
        x =>
          x.copy(TransitOperation = x.TransitOperation.copy(notificationType = IntentionToControl))
      }) {
        message =>
          val intentionToControlInformationRequestedController: String =
            controllers.departure.routes.IntentionToControlController.onPageLoad(departureIdP5, messageId).url

          when(mockDepartureMessageService.getMessage[CC060CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
          when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
          when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))
          when(mockIntentionToControlViewModelProvider.apply(any(), any())(any()))
            .thenReturn(IntentionToControlViewModel(sections, Some(lrn.toString), customsOffice))

          val intentionToControlP5ViewModel = new IntentionToControlViewModel(sections, Some(lrn.toString), customsOffice)

          val request = FakeRequest(GET, intentionToControlInformationRequestedController)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[IntentionToControlView]

          contentAsString(result) mustEqual
            view(intentionToControlP5ViewModel, departureIdP5, messageId)(request, messages).toString
      }
    }

    "must redirect to Presentation notification frontend" in {
      forAll(arbitrary[CC060CType]) {
        message =>
          val intentionToControlInformationRequestedController: String =
            controllers.departure.routes.IntentionToControlController.onPageLoad(departureIdP5, messageId).url

          when(mockDepartureMessageService.getMessage[CC060CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
          when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

          val request = FakeRequest(POST, intentionToControlInformationRequestedController)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual frontendAppConfig.presentationNotificationFrontendUrl(departureIdP5)
      }
    }
  }
}
