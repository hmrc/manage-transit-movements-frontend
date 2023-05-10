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
import controllers.actions.{FakeRejectionMessageAction, RejectionMessageActionProvider}
import generators.Generators
import models.departureP5._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DepartureP5MessageService
import viewModels.P5.departure.RejectionMessageP5ViewModel
import viewModels.P5.departure.RejectionMessageP5ViewModel.RejectionMessageP5ViewModelProvider
import viewModels.sections.Section
import views.html.departure.TestOnly.RejectionMessageP5View

import scala.concurrent.Future

class RejectionMessageP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockRejectionMessageP5ViewModelProvider   = mock[RejectionMessageP5ViewModelProvider]
  private val mockDepartureP5MessageService             = mock[DepartureP5MessageService]
  private val mockRejectionMessageActionProvider        = mock[RejectionMessageActionProvider]
  private val mockCacheService: DepartureCacheConnector = mock[DepartureCacheConnector]

  def rejectionMessageAction(departureIdP5: String, mockDepartureP5MessageService: DepartureP5MessageService, mockCacheService: DepartureCacheConnector): Unit =
    when(mockRejectionMessageActionProvider.apply(any())) thenReturn new FakeRejectionMessageAction(departureIdP5,
                                                                                                    mockDepartureP5MessageService,
                                                                                                    mockCacheService
    )

  lazy val rejectionMessageController: String = controllers.testOnly.routes.RejectionMessageP5Controller.onPageLoad(departureIdP5).url
  lazy val rejectionMessageOnAmend: String    = controllers.testOnly.routes.RejectionMessageP5Controller.onAmend(departureIdP5).url
  val sections: Seq[Section]                  = arbitrarySections.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
    reset(mockRejectionMessageP5ViewModelProvider)
    reset(mockRejectionMessageActionProvider)
    reset(mockCacheService)

  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[RejectionMessageP5ViewModelProvider].toInstance(mockRejectionMessageP5ViewModelProvider))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))
      .overrides(bind[DepartureCacheConnector].toInstance(mockCacheService))

  "RejectionMessageP5Controller" - {

    "must return OK and the correct view for a GET" in {
      val message: IE056Data = IE056Data(
        IE056MessageData(
          TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123")),
          Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
        )
      )
      when(mockDepartureP5MessageService.getMessage[IE056Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(message)))
      when(mockDepartureP5MessageService.getLRNFromDeclarationMessage(any())(any(), any())).thenReturn(Future.successful(Some("LRNAB123")))
      when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(true))
      when(mockRejectionMessageP5ViewModelProvider.apply(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(RejectionMessageP5ViewModel(sections, lrn.toString, multipleErrors = true)))

      rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

      val rejectionMessageP5ViewModel = new RejectionMessageP5ViewModel(sections, lrn.toString, true)

      val request = FakeRequest(GET, rejectionMessageController)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[RejectionMessageP5View]

      contentAsString(result) mustEqual
        view(rejectionMessageP5ViewModel, departureIdP5)(request, messages, frontendAppConfig).toString
    }

    "must redirect to session expired when declaration amendable is false" in {
      val message: IE056Data = IE056Data(
        IE056MessageData(
          TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123")),
          Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
        )
      )
      when(mockDepartureP5MessageService.getMessage[IE056Data](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(message)))
      when(mockDepartureP5MessageService.getLRNFromDeclarationMessage(any())(any(), any())).thenReturn(Future.successful(Some("LRNAB123")))
      when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(false))

      rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

      val request = FakeRequest(GET, rejectionMessageController)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url // TODO: Change to generic error page

    }

    "onAmend" - {

      "must redirect to technical difficulties when declaration is not amendable" in {

        val message: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123")),
            Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
          )
        )
        when(mockDepartureP5MessageService.getMessage[IE056Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(message)))
        when(mockDepartureP5MessageService.getLRNFromDeclarationMessage(any())(any(), any())).thenReturn(Future.successful(Some("LRNAB123")))
        when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(false))

        rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

        val request = FakeRequest(GET, rejectionMessageOnAmend)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url

      }

      "must redirect to technical difficulties when there are no errors" in {

        val message: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123")),
            Seq.empty
          )
        )
        when(mockDepartureP5MessageService.getMessage[IE056Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(message)))
        when(mockDepartureP5MessageService.getLRNFromDeclarationMessage(any())(any(), any())).thenReturn(Future.successful(Some("LRNAB123")))
        when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(true))

        rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

        val request = FakeRequest(GET, rejectionMessageOnAmend)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url

      }

      "must redirect to departure task-list on success of handleErrors" in {

        val message: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123")),
            Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
          )
        )
        when(mockDepartureP5MessageService.getMessage[IE056Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(message)))
        when(mockDepartureP5MessageService.getLRNFromDeclarationMessage(any())(any(), any())).thenReturn(Future.successful(Some("LRNAB123")))
        when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(true))
        when(mockCacheService.handleErrors(any(), any())(any())).thenReturn(Future.successful(true))

        rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

        val request = FakeRequest(GET, rejectionMessageOnAmend)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual frontendAppConfig.departureFrontendTaskListUrl("LRNAB123")

      }

      "must redirect to technical difficulties on failure of handleErrors" in {

        val message: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123")),
            Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
          )
        )
        when(mockDepartureP5MessageService.getMessage[IE056Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(Some(message)))
        when(mockDepartureP5MessageService.getLRNFromDeclarationMessage(any())(any(), any())).thenReturn(Future.successful(Some("LRNAB123")))
        when(mockCacheService.isDeclarationAmendable(any(), any())(any())).thenReturn(Future.successful(true))
        when(mockCacheService.handleErrors(any(), any())(any())).thenReturn(Future.successful(false))

        rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

        val request = FakeRequest(GET, rejectionMessageOnAmend)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url

      }
    }

  }
}
