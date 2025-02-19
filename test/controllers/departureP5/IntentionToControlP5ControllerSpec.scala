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

package controllers.departureP5

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated._
import generators.Generators
import models.departureP5._
import models.referenceData.CustomsOffice
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{DepartureP5MessageService, ReferenceDataService}
import viewModels.P5.departure.IntentionToControlP5ViewModel.IntentionToControlP5ViewModelProvider
import viewModels.P5.departure.{CustomsOfficeContactViewModel, IntentionToControlP5ViewModel}
import views.html.departureP5.IntentionToControlP5View

import scala.concurrent.Future

class IntentionToControlP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockIntentionToControlP5ViewModelProvider = mock[IntentionToControlP5ViewModelProvider]
  private val mockReferenceDataService                  = mock[ReferenceDataService]
  private val mockDepartureP5MessageService             = mock[DepartureP5MessageService]

  private val sections = arbitrarySections.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
    reset(mockDepartureP5MessageService)
    reset(mockIntentionToControlP5ViewModelProvider)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[IntentionToControlP5ViewModelProvider].toInstance(mockIntentionToControlP5ViewModelProvider))
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))

  private val customsOffice = arbitrary[CustomsOffice].sample.value

  "IntentionToControlP5Controller Controller" - {

    "must return OK and the correct view for a GET when informationRequested" in {
      forAll(listWithMaxLength[RequestedDocumentType]()) {
        requestedDocuments =>
          forAll(arbitrary[CC060CType].map(_.copy(RequestedDocument = requestedDocuments))) {
            message =>
              val intentionToControlInformationRequestedController: String =
                controllers.departureP5.routes.IntentionToControlP5Controller.informationRequested(departureIdP5, messageId).url

              when(mockDepartureP5MessageService.getMessage[CC060CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
              when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
                .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
              when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))
              when(mockIntentionToControlP5ViewModelProvider.apply(any())(any()))
                .thenReturn(IntentionToControlP5ViewModel(sections, requestedDocuments = true, Some(lrn.toString)))

              val intentionToControlP5ViewModel = new IntentionToControlP5ViewModel(sections, true, Some(lrn.toString))
              val customsOfficeContactViewModel = CustomsOfficeContactViewModel(customsOffice.id, Some(customsOffice))

              val request = FakeRequest(GET, intentionToControlInformationRequestedController)

              val result = route(app, request).value

              status(result) mustEqual OK

              val view = injector.instanceOf[IntentionToControlP5View]

              contentAsString(result) mustEqual
                view(intentionToControlP5ViewModel, departureIdP5, messageId, customsOfficeContactViewModel)(request, messages).toString
          }
      }
    }

    "must return OK and the correct view for a GET when noInformationRequested" in {
      forAll(arbitrary[CC060CType].map {
        _.copy(RequestedDocument = Nil)
      }) {
        message =>
          val intentionToControlInformationRequestedController: String =
            controllers.departureP5.routes.IntentionToControlP5Controller.noInformationRequested(departureIdP5, messageId).url

          when(mockDepartureP5MessageService.getMessage[CC060CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
          when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))
          when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(customsOffice))
          when(mockIntentionToControlP5ViewModelProvider.apply(any())(any()))
            .thenReturn(IntentionToControlP5ViewModel(sections, requestedDocuments = false, Some(lrn.toString)))

          val intentionToControlP5ViewModel = new IntentionToControlP5ViewModel(sections, false, Some(lrn.toString))
          val customsOfficeContactViewModel = CustomsOfficeContactViewModel(customsOffice.id, Some(customsOffice))

          val request = FakeRequest(GET, intentionToControlInformationRequestedController)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[IntentionToControlP5View]

          contentAsString(result) mustEqual
            view(intentionToControlP5ViewModel, departureIdP5, messageId, customsOfficeContactViewModel)(request, messages).toString
      }
    }

    "must redirect to Presentation notification frontend" in {
      forAll(arbitrary[CC060CType]) {
        message =>
          val intentionToControlInformationRequestedController: String =
            controllers.departureP5.routes.IntentionToControlP5Controller.informationRequested(departureIdP5, messageId).url

          when(mockDepartureP5MessageService.getMessage[CC060CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
          when(mockDepartureP5MessageService.getDepartureReferenceNumbers(any())(any(), any()))
            .thenReturn(Future.successful(DepartureReferenceNumbers(lrn.value, None)))

          val request = FakeRequest(POST, intentionToControlInformationRequestedController)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual frontendAppConfig.presentationNotificationFrontendUrl(departureIdP5)
      }
    }
  }
}
