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
import controllers.actions.{ArrivalRejectionMessageActionProvider, FakeArrivalRejectionMessageAction}
import generators.Generators
import models.arrivalP5.{IE057Data, IE057MessageData, TransitOperationIE057}
import models.departureP5._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ArrivalP5MessageService
import viewModels.P5.arrival.UnloadingNotificationErrorsP5ViewModel
import views.html.departure.TestOnly.UnloadingNotificationErrorsP5View

import scala.concurrent.Future

class UnloadingNotificationErrorsP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockArrivalP5MessageService                = mock[ArrivalP5MessageService]
  private val mockRejectionMessageActionProvider         = mock[ArrivalRejectionMessageActionProvider]
  lazy val unloadingNotificationErrorsController: String = controllers.testOnly.routes.UnloadingNotificationErrorsP5Controller.onPageLoad(arrivalIdP5).url

  private val mrnString = "MRNAB123"

  def rejectionMessageAction(arrivalIdP5: String, mockArrivalP5MessageService: ArrivalP5MessageService): Unit =
    when(mockRejectionMessageActionProvider.apply(any())) thenReturn new FakeArrivalRejectionMessageAction(arrivalIdP5, mockArrivalP5MessageService)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalP5MessageService)
    reset(mockRejectionMessageActionProvider)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ArrivalP5MessageService].toInstance(mockArrivalP5MessageService))

  "UnloadingNotificationErrorsP5Controller" - {

    "must return OK and the correct view for a GET when no Errors" in {
      val message: IE057Data = IE057Data(
        IE057MessageData(
          TransitOperationIE057(s"$mrnString"),
          Seq.empty
        )
      )

      when(mockArrivalP5MessageService.getMessage[IE057Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(message)))

      rejectionMessageAction(arrivalIdP5, mockArrivalP5MessageService)

      val unloadingNotificationErrorsP5ViewModel = new UnloadingNotificationErrorsP5ViewModel(mrnString, true)

      val request = FakeRequest(GET, unloadingNotificationErrorsController)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[UnloadingNotificationErrorsP5View]

      contentAsString(result) mustEqual
        view(unloadingNotificationErrorsP5ViewModel)(request, messages, frontendAppConfig).toString
    }

    "must return OK and the correct view for a GET when more than 10 Errors" in {

      val message: IE057Data = IE057Data(
        IE057MessageData(
          TransitOperationIE057(s"$mrnString"),
          Seq(
            FunctionalError("1", "12", "Codelist violation", None),
            FunctionalError("2", "14", "Rule violation", None),
            FunctionalError("2", "14", "Rule violation", None),
            FunctionalError("2", "14", "Rule violation", None),
            FunctionalError("2", "14", "Rule violation", None),
            FunctionalError("2", "14", "Rule violation", None),
            FunctionalError("2", "14", "Rule violation", None),
            FunctionalError("2", "14", "Rule violation", None),
            FunctionalError("2", "14", "Rule violation", None),
            FunctionalError("2", "14", "Rule violation", None),
            FunctionalError("2", "14", "Rule violation", None)
          )
        )
      )
      when(mockArrivalP5MessageService.getMessage[IE057Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(message)))

      rejectionMessageAction(arrivalIdP5, mockArrivalP5MessageService)

      val unloadingNotificationErrorsP5ViewModel = new UnloadingNotificationErrorsP5ViewModel(mrnString, false)

      val request = FakeRequest(GET, unloadingNotificationErrorsController)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[UnloadingNotificationErrorsP5View]

      contentAsString(result) mustEqual
        view(unloadingNotificationErrorsP5ViewModel)(request, messages, frontendAppConfig).toString
    }

    "must redirect to technical difficulties page when functionalErrors is between 1 to 10" in {
      val message: IE057Data = IE057Data(
        IE057MessageData(
          TransitOperationIE057(s"$mrnString"),
          Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
        )
      )

      when(mockArrivalP5MessageService.getMessage[IE057Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(message)))

      rejectionMessageAction(arrivalIdP5, mockArrivalP5MessageService)

      val request = FakeRequest(GET, unloadingNotificationErrorsController)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url

    }
  }
}
