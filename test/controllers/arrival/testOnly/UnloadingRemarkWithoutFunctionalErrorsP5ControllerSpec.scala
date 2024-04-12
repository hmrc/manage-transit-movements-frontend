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

package controllers.arrival.testOnly

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
import services.{ArrivalP5MessageService, ReferenceDataService}
import viewModels.P5.arrival.UnloadingRemarkWithoutFunctionalErrorsP5ViewModel
import views.html.arrivalP5.UnloadingRemarkWithoutFunctionalErrorsP5View

import scala.concurrent.Future

class UnloadingRemarkWithoutFunctionalErrorsP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockArrivalP5MessageService = mock[ArrivalP5MessageService]
  private val mockReferenceDataService    = mock[ReferenceDataService]

  lazy val unloadingRemarkWithErrorsController: String =
    controllers.arrivalP5.routes.UnloadingRemarkWithoutFunctionalErrorsP5Controller.onPageLoad(arrivalIdP5, messageId).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalP5MessageService)
    reset(mockReferenceDataService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .p5GuiceApplicationBuilder()
      .overrides(bind[ArrivalP5MessageService].toInstance(mockArrivalP5MessageService))
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "UnloadingRemarkWithoutFunctionalErrorsP5Controller" - {

    "must return OK and the correct view for a GET when no Errors" in {
      forAll(arbitrary[CC057CType].map(_.copy(FunctionalError = Nil))) {
        message =>
          when(mockArrivalP5MessageService.getMessage[CC057CType](any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(message))

          when(mockReferenceDataService.getCustomsOffice(any())(any(), any()))
            .thenReturn(Future.successful(Right(fakeCustomsOffice)))

          val unloadingNotificationErrorsP5ViewModel =
            new UnloadingRemarkWithoutFunctionalErrorsP5ViewModel(message.TransitOperation.MRN, Right(fakeCustomsOffice))

          val request = FakeRequest(GET, unloadingRemarkWithErrorsController)

          val result = route(app, request).value

          status(result) mustEqual OK

          val view = injector.instanceOf[UnloadingRemarkWithoutFunctionalErrorsP5View]

          contentAsString(result) mustEqual
            view(unloadingNotificationErrorsP5ViewModel)(request, messages).toString
      }
    }

    "must redirect to technical difficulties page when functionalErrors is greater than 0" in {
      forAll(listWithMaxLength[FunctionalErrorType04]()) {
        functionalErrors =>
          forAll(arbitrary[CC057CType].map(_.copy(FunctionalError = functionalErrors))) {
            message =>
              when(mockArrivalP5MessageService.getMessage[CC057CType](any(), any())(any(), any(), any()))
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
  }
}
