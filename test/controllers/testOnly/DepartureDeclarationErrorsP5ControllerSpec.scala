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
import connectors.DepartureCacheConnector
import controllers.actions.{DepartureRejectionMessageActionProvider, FakeDepartureRejectionMessageAction}
import generators.Generators
import models.departureP5.{FunctionalError, _}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DepartureP5MessageService
import viewModels.P5.departure.DepartureDeclarationErrorsP5ViewModel
import views.html.departure.TestOnly.DepartureDeclarationErrorsP5View

import scala.concurrent.Future

class DepartureDeclarationErrorsP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockDepartureP5MessageService             = mock[DepartureP5MessageService]
  private val mockCacheService: DepartureCacheConnector = mock[DepartureCacheConnector]
  private val mockRejectionMessageActionProvider        = mock[DepartureRejectionMessageActionProvider]
  lazy val departureDeclarationErrorsController: String = controllers.testOnly.routes.DepartureDeclarationErrorsP5Controller.onPageLoad(departureIdP5).url

  private val lrnString = "LRNAB123"

  def rejectionMessageAction(departureIdP5: String, mockDepartureP5MessageService: DepartureP5MessageService, mockCacheService: DepartureCacheConnector): Unit =
    when(mockRejectionMessageActionProvider.apply(any())) thenReturn new FakeDepartureRejectionMessageAction(departureIdP5,
                                                                                                             mockDepartureP5MessageService,
                                                                                                             mockCacheService
    )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
    reset(mockRejectionMessageActionProvider)
    reset(mockCacheService)

  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))
      .overrides(bind[DepartureCacheConnector].toInstance(mockCacheService))

  "DepartureDeclarationErrorsP5Controller" - {

    "must return OK and the correct view for a GET when no Errors" in {
      val message: IE056Data = IE056Data(
        IE056MessageData(
          TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123")),
          CustomsOfficeOfDeparture("22323323"),
          Seq.empty
        )
      )
      when(mockDepartureP5MessageService.getMessage[IE056Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(message)))
      when(mockDepartureP5MessageService.getLRNFromDeclarationMessage(any())(any(), any())).thenReturn(Future.successful(Some(lrnString)))
      when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(true))

      rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

      val departureDeclarationErrorsP5ViewModel = new DepartureDeclarationErrorsP5ViewModel(lrnString, true)

      val request = FakeRequest(GET, departureDeclarationErrorsController)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[DepartureDeclarationErrorsP5View]

      contentAsString(result) mustEqual
        view(departureDeclarationErrorsP5ViewModel)(request, messages, frontendAppConfig).toString
    }

    "must return OK and the correct view for a GET when more than 10 Errors" in {

      val message: IE056Data = IE056Data(
        IE056MessageData(
          TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123")),
          CustomsOfficeOfDeparture("22323323"),
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
      when(mockDepartureP5MessageService.getMessage[IE056Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(message)))
      when(mockDepartureP5MessageService.getLRNFromDeclarationMessage(any())(any(), any())).thenReturn(Future.successful(Some(lrnString)))
      when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(true))

      rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

      val departureDeclarationErrorsP5ViewModel = new DepartureDeclarationErrorsP5ViewModel(lrnString, false)

      val request = FakeRequest(GET, departureDeclarationErrorsController)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[DepartureDeclarationErrorsP5View]

      contentAsString(result) mustEqual
        view(departureDeclarationErrorsP5ViewModel)(request, messages, frontendAppConfig).toString
    }

    "must redirect to technical difficulties page when functionalErrors is between 1 to 10" in {
      val message: IE056Data = IE056Data(
        IE056MessageData(
          TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123")),
          CustomsOfficeOfDeparture("22323323"),
          Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
        )
      )
      when(mockDepartureP5MessageService.getMessage[IE056Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(message)))
      when(mockDepartureP5MessageService.getLRNFromDeclarationMessage(any())(any(), any())).thenReturn(Future.successful(Some("LRNAB123")))
      when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(false))

      rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

      val request = FakeRequest(GET, departureDeclarationErrorsController)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url

    }
  }
}
