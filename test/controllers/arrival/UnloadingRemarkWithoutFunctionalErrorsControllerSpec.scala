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

package controllers.arrival

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated._
import generators.Generators
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{ArrivalMessageService, ReferenceDataService}
import viewModels.arrival.UnloadingRemarkWithoutFunctionalErrorsViewModel
import views.html.arrival.UnloadingRemarkWithoutFunctionalErrorsView

import scala.concurrent.Future

class UnloadingRemarkWithoutFunctionalErrorsControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockArrivalMessageService = mock[ArrivalMessageService]
  private val mockReferenceDataService  = mock[ReferenceDataService]

  lazy val unloadingRemarkWithErrorsController: String =
    controllers.arrival.routes.UnloadingRemarkWithoutFunctionalErrorsController.onPageLoad(arrivalIdP5, messageId).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalMessageService)
    reset(mockReferenceDataService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ArrivalMessageService].toInstance(mockArrivalMessageService))
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "UnloadingRemarkWithoutFunctionalErrorsController" - {

    "must return OK and the correct view for a GET when no Errors" in {
      forAll(arbitrary[CC057CType].map(_.copy(FunctionalError = Nil))) {
        message =>
          when(mockArrivalMessageService.getMessage[CC057CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))

          when(mockReferenceDataService.getCustomsOffice(any())(any(), any()))
            .thenReturn(Future.successful(fakeCustomsOffice))

          val unloadingNotificationErrorsViewModel =
            new UnloadingRemarkWithoutFunctionalErrorsViewModel(message.TransitOperation.MRN, fakeCustomsOffice)

          val request = FakeRequest(GET, unloadingRemarkWithErrorsController)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[UnloadingRemarkWithoutFunctionalErrorsView]

          contentAsString(result) mustEqual
            view(unloadingNotificationErrorsViewModel, arrivalIdP5, messageId)(request, messages).toString
      }
    }

    "must redirect to technical difficulties page when functionalErrors is greater than 0" in {
      forAll(listWithMaxLength[FunctionalErrorType07]()) {
        functionalErrors =>
          forAll(arbitrary[CC057CType].map(_.copy(FunctionalError = functionalErrors))) {
            message =>
              when(mockArrivalMessageService.getMessage[CC057CType](any(), any())(any(), any(), any()))
                .thenReturn(Future.successful(message))

              when(mockReferenceDataService.getCustomsOffice(any())(any(), any()))
                .thenReturn(Future.successful(Right(fakeCustomsOffice)))

              val request = FakeRequest(GET, unloadingRemarkWithErrorsController)

              val result = route(app, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
          }
      }
    }

    "must redirect to unloading remarks for a POST" in {
      forAll(arbitrary[CC057CType]) {
        message =>
          when(mockArrivalMessageService.getMessage[CC057CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))

          val request = FakeRequest(POST, unloadingRemarkWithErrorsController)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual frontendAppConfig.unloadingStart(arrivalIdP5, messageId)
      }
    }
  }

}
