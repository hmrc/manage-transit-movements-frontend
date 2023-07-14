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
import controllers.actions.{ArrivalRejectionMessageActionProvider, FakeArrivalRejectionMessageAction}
import generators.Generators
import models.arrivalP5.{CustomsOfficeOfDestinationActual, IE057Data, IE057MessageData, TransitOperationIE057}
import models.departureP5._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ArrivalP5MessageService
import viewModels.P5.arrival.ArrivalNotificationWithFunctionalErrorsP5ViewModel
import viewModels.P5.arrival.ArrivalNotificationWithFunctionalErrorsP5ViewModel.ArrivalNotificationWithFunctionalErrorsP5ViewModelProvider
import viewModels.sections.Section
import views.html.arrival.P5.ArrivalNotificationWithFunctionalErrorsP5View

import scala.concurrent.Future

class ArrivalNotificationWithFunctionalErrorsP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockArrivalNotificationWithFuncationalErrorsP5ViewModelProvider = mock[ArrivalNotificationWithFunctionalErrorsP5ViewModelProvider]
  private val mockArrivalP5MessageService                                     = mock[ArrivalP5MessageService]
  private val mockRejectionMessageActionProvider                              = mock[ArrivalRejectionMessageActionProvider]

  def rejectionMessageAction(departureIdP5: String, mockArrivalP5MessageService: ArrivalP5MessageService): Unit =
    when(mockRejectionMessageActionProvider.apply(any())) thenReturn new FakeArrivalRejectionMessageAction(departureIdP5, mockArrivalP5MessageService)

  lazy val rejectionMessageController: String = controllers.testOnly.routes.ArrivalNotificationWithFunctionalErrorsP5Controller.onPageLoad(departureIdP5).url
  val sections: Seq[Section]                  = arbitrarySections.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockArrivalP5MessageService)
    reset(mockArrivalNotificationWithFuncationalErrorsP5ViewModelProvider)
    reset(mockRejectionMessageActionProvider)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ArrivalNotificationWithFunctionalErrorsP5ViewModelProvider].toInstance(mockArrivalNotificationWithFuncationalErrorsP5ViewModelProvider))
      .overrides(bind[ArrivalP5MessageService].toInstance(mockArrivalP5MessageService))

  "ArrivalNotificationWithFunctionalErrorsP5Controller" - {

    "must return OK and the correct view for a GET" in {
      val message: IE057Data = IE057Data(
        IE057MessageData(
          TransitOperationIE057("MRNCD3232"),
          CustomsOfficeOfDestinationActual("1234"),
          Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
        )
      )
      when(mockArrivalP5MessageService.getMessage[IE057Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(message)))
      when(mockArrivalNotificationWithFuncationalErrorsP5ViewModelProvider.apply(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(ArrivalNotificationWithFunctionalErrorsP5ViewModel(sections, mrn, multipleErrors = true)))

      rejectionMessageAction(departureIdP5, mockArrivalP5MessageService)

      val rejectionMessageP5ViewModel = new ArrivalNotificationWithFunctionalErrorsP5ViewModel(sections, mrn, true)

      val request = FakeRequest(GET, rejectionMessageController)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[ArrivalNotificationWithFunctionalErrorsP5View]

      contentAsString(result) mustEqual
        view(rejectionMessageP5ViewModel, departureIdP5)(request, messages, frontendAppConfig).toString
    }

    "must redirect to technical difficulties page when functionalErrors is 0" in {
      val message: IE057Data = IE057Data(
        IE057MessageData(
          TransitOperationIE057("MRNCD3232"),
          CustomsOfficeOfDestinationActual("1234"),
          Seq.empty
        )
      )
      when(mockArrivalP5MessageService.getMessage[IE057Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(message)))
      when(mockArrivalNotificationWithFuncationalErrorsP5ViewModelProvider.apply(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(ArrivalNotificationWithFunctionalErrorsP5ViewModel(sections, mrn, multipleErrors = true)))

      rejectionMessageAction(departureIdP5, mockArrivalP5MessageService)

      val request = FakeRequest(GET, rejectionMessageController)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url

    }
  }
}
