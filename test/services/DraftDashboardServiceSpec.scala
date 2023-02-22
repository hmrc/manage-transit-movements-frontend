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

package services

import base.SpecBase
import controllers.actions.FakeIdentifierAction
import play.api.mvc.Results
import controllers.departure.drafts.routes
import forms.SearchFormProvider
import generators.Generators
import models.requests.IdentifierRequest
import models.{DepartureUserAnswerSummary, DeparturesSummary, LocalReferenceNumber}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import play.twirl.api.HtmlFormat
import viewModels.drafts.AllDraftDeparturesViewModel
import views.html.departure.drafts.DashboardView

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DraftDashboardServiceSpec extends SpecBase with Generators {

  private val mockDraftDepartureService: DraftDepartureService = mock[DraftDepartureService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDraftDepartureService)
  }

  private val formProvider = new SearchFormProvider()
  private val form         = formProvider()

  private val view: DashboardView = injector
    .instanceOf[DashboardView]
  private val DraftDashboardService = new DraftDashboardService(messagesApi, mockDraftDepartureService, paginationAppConfig, frontendAppConfig, view)

  "DraftDashboardService" - {
    "buildView must return OK when view is built" in {

      val createdAt = LocalDateTime.now()

      val expectedResult = DeparturesSummary(
        0,
        0,
        List(
          DepartureUserAnswerSummary(LocalReferenceNumber("AB123"), createdAt, 29),
          DepartureUserAnswerSummary(LocalReferenceNumber("CD123"), createdAt, 28)
        )
      )

      implicit val fakeRequest: IdentifierRequest[FakeIdentifierAction] = IdentifierRequest[FakeIdentifierAction](fakeRequest, "1234")
      implicit val fakeRequest: FakeIdentifierAction = FakeIdentifierAction.apply()

      when(mockDraftDepartureService.getLRNs(any(), any())(any())).thenReturn(Future.successful(Some(expectedResult)))
      DraftDashboardService.buildView(form)(Ok(_)) mustBe Ok
    }
    "buildView must return Error when unable to build view" ignore {}
  }
}
