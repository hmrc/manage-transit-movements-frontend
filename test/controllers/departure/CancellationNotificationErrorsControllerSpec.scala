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
import connectors.DepartureCacheConnector
import generated.{CC056CType, FunctionalErrorType02}
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{DepartureMessageService, ReferenceDataService}
import viewModels.departure.CancellationNotificationErrorsViewModel
import views.html.departure.CancellationNotificationErrorsView

import scala.concurrent.Future

class CancellationNotificationErrorsControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureMessageService               = mock[DepartureMessageService]
  private val mockCacheService: DepartureCacheConnector = mock[DepartureCacheConnector]
  private val mockReferenceDataService                  = mock[ReferenceDataService]

  lazy val controllerRoute: String = controllers.departure.routes.CancellationNotificationErrorsController.onPageLoad(departureIdP5, messageId).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureMessageService)
    reset(mockCacheService)
    reset(mockReferenceDataService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureMessageService].toInstance(mockDepartureMessageService))
      .overrides(bind[DepartureCacheConnector].toInstance(mockCacheService))
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "CancellationNotificationErrorsController" - {

    "must return OK and the correct view for a GET when no Errors" in {
      forAll(arbitrary[CC056CType].map(_.copy(FunctionalError = Nil))) {
        message =>
          when(mockDepartureMessageService.getMessage[CC056CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
          when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any())).thenReturn(Future.successful(departureReferenceNumbers))
          when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(fakeCustomsOffice))

          val cancellationNotificationErrorsP5ViewModel = new CancellationNotificationErrorsViewModel(lrn.value, fakeCustomsOffice)

          val request = FakeRequest(GET, controllerRoute)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[CancellationNotificationErrorsView]

          contentAsString(result) mustEqual
            view(cancellationNotificationErrorsP5ViewModel)(request, messages).toString
      }
    }

    "must redirect to technical difficulties page when functionalErrors is between 1 to 10" in {
      forAll(listWithMaxLength[FunctionalErrorType02]()) {
        functionalErrors =>
          forAll(arbitrary[CC056CType].map(_.copy(FunctionalError = functionalErrors))) {
            message =>
              when(mockDepartureMessageService.getMessage[CC056CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(message))
              when(mockDepartureMessageService.getDepartureReferenceNumbers(any())(any(), any())).thenReturn(Future.successful(departureReferenceNumbers))
              when(mockReferenceDataService.getCustomsOffice(any())(any(), any())).thenReturn(Future.successful(fakeCustomsOffice))

              val request = FakeRequest(GET, controllerRoute)

              val result = route(app, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
          }
      }
    }
  }

}
