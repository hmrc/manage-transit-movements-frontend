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

package controllers.testOnly

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.actions.FakeGoodsUnderControlAction
import generators.Generators
import models.departureP5.IE060Data
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{DepartureP5MessageService, ReferenceDataService}
import viewModels.P5.departure.GoodsUnderControlP5ViewModel
import viewModels.P5.departure.GoodsUnderControlP5ViewModel.GoodsUnderControlP5ViewModelProvider
import views.html.departure.P5.TestOnlyGoodsUnderControlP5View

import scala.concurrent.Future

class TestOnlyGoodsUnderControlP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockGoodsUnderControlP5ViewModelProvider = mock[GoodsUnderControlP5ViewModelProvider]
  private val mockReferenceDataService                 = mock[ReferenceDataService]
  private val mockDepartureP5MessageService            = mock[DepartureP5MessageService]

  lazy val checkYourAnswersRoute: String = controllers.testOnly.routes.TestOnlyGoodsUnderControlP5Controller.onPageLoad(departureIdP5).url

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService);
    reset(mockDepartureP5MessageService);
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[GoodsUnderControlP5ViewModelProvider].toInstance(mockGoodsUnderControlP5ViewModelProvider))

  "UnloadingFindingsController Controller" - {

    "must return OK and the correct view for a GET" in {
      //      when(mockReferenceDataService.getCustomsOfficeByCode(any())(any(), any())).thenReturn(Future.successful(Some(message)))
      //      when(mockDepartureP5MessageService.getGoodsUnderControl(any())(any(), any())).thenReturn(Future.successful(Some(message)))

      val goodsUnderControlAction = new FakeGoodsUnderControlAction(departureIdP5, mockDepartureP5MessageService, mockReferenceDataService)

      val sections = arbitrarySections.arbitrary.sample.value

      when(mockGoodsUnderControlP5ViewModelProvider.apply(any())(any()))
        .thenReturn(GoodsUnderControlP5ViewModel(sections))

      val goodsUnderControlP5ViewModel = new GoodsUnderControlP5ViewModel(sections)

      val request = FakeRequest(GET, checkYourAnswersRoute)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[TestOnlyGoodsUnderControlP5View]

      contentAsString(result) mustEqual
        view(goodsUnderControlP5ViewModel, departureIdP5, )(request, messages).toString
    }

    //    "must redirect to the next page when valid data is submitted" ignore {
    //      checkArrivalStatus()
    //      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
    //
    //      setExistingUserAnswers(emptyUserAnswers)
    //
    //      val request =
    //        FakeRequest(POST, checkYourAnswersRoute)
    //          .withFormUrlEncodedBody(("value", "true"))
    //
    //      val result = route(app, request).value
    //
    //      status(result) mustEqual SEE_OTHER
    //
    //      redirectLocation(result).value mustEqual controllers.routes.UnloadingRemarksSentController.onPageLoad(arrivalId).url
    //    }
    //
    //    "must redirect to Session Expired for a GET if no existing data is found" in {
    //      checkArrivalStatus()
    //      setNoExistingUserAnswers()
    //
    //      val request = FakeRequest(GET, checkYourAnswersRoute)
    //
    //      val result = route(app, request).value
    //
    //      status(result) mustEqual SEE_OTHER
    //
    //      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    //    }
    //
    //    "must redirect to Session Expired for a POST if no existing data is found" ignore {
    //      checkArrivalStatus()
    //      setNoExistingUserAnswers()
    //
    //      val request =
    //        FakeRequest(POST, checkYourAnswersRoute)
    //          .withFormUrlEncodedBody(("value", "true"))
    //
    //      val result = route(app, request).value
    //
    //      status(result) mustEqual SEE_OTHER
    //
    //      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    //    }
    //  }
  }
}
