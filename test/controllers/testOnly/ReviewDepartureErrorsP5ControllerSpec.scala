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
import viewModels.P5.departure.ReviewDepartureErrorsP5ViewModel
import viewModels.P5.departure.ReviewDepartureErrorsP5ViewModel.ReviewDepartureErrorsP5ViewModelProvider
import viewModels.sections.Section
import views.html.departure.TestOnly.ReviewDepartureErrorsP5View

import scala.concurrent.Future

class ReviewDepartureErrorsP5ControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockReviewDepartureErrorMessageP5ViewModelProvider = mock[ReviewDepartureErrorsP5ViewModelProvider]
  private val mockDepartureP5MessageService                      = mock[DepartureP5MessageService]
  private val mockRejectionMessageActionProvider                 = mock[RejectionMessageActionProvider]
  private val mockCacheService: DepartureCacheConnector          = mock[DepartureCacheConnector]

  def rejectionMessageAction(departureIdP5: String, mockDepartureP5MessageService: DepartureP5MessageService, mockCacheService: DepartureCacheConnector): Unit =
    when(mockRejectionMessageActionProvider.apply(any())) thenReturn new FakeRejectionMessageAction(departureIdP5,
                                                                                                    mockDepartureP5MessageService,
                                                                                                    mockCacheService
    )

  lazy val rejectionMessageController: String = controllers.testOnly.routes.ReviewDepartureErrorsP5Controller.onPageLoad(departureIdP5).url
  val sections: Seq[Section]                  = arbitrarySections.arbitrary.sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDepartureP5MessageService)
    reset(mockReviewDepartureErrorMessageP5ViewModelProvider)
    reset(mockRejectionMessageActionProvider)
    reset(mockCacheService)
  }

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ReviewDepartureErrorsP5ViewModelProvider].toInstance(mockReviewDepartureErrorMessageP5ViewModelProvider))
      .overrides(bind[DepartureP5MessageService].toInstance(mockDepartureP5MessageService))
      .overrides(bind[DepartureCacheConnector].toInstance(mockCacheService))

  "ReviewDepartureErrorsP5Controller" - {

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
      when(mockReviewDepartureErrorMessageP5ViewModelProvider.apply(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(ReviewDepartureErrorsP5ViewModel(sections, lrn.toString, multipleErrors = true)))

      rejectionMessageAction(departureIdP5, mockDepartureP5MessageService, mockCacheService)

      val rejectionMessageP5ViewModel = new ReviewDepartureErrorsP5ViewModel(sections, lrn.toString, true)

      val request = FakeRequest(GET, rejectionMessageController)

      val result = route(app, request).value

      status(result) mustEqual OK

      val view = injector.instanceOf[ReviewDepartureErrorsP5View]

      contentAsString(result) mustEqual
        view(rejectionMessageP5ViewModel, departureIdP5)(request, messages, frontendAppConfig).toString
    }
  }
}
