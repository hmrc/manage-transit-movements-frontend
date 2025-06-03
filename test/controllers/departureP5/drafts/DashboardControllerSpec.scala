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

package controllers.departureP5.drafts

import base.SpecBase
import forms.DeparturesSearchFormProvider
import generators.Generators
import models.{DepartureUserAnswerSummary, DeparturesSummary, LocalReferenceNumber}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.DraftDepartureService
import viewModels.drafts.AllDraftDeparturesViewModel
import views.html.departureP5.drafts.DashboardView

import java.time.LocalDateTime
import scala.concurrent.Future

class DashboardControllerSpec extends SpecBase with Generators {

  private val draftDepartureService = mock[DraftDepartureService]

  private val formProvider                 = new DeparturesSearchFormProvider()
  private val form                         = formProvider()
  private lazy val draftDashboardGetRoute  = routes.DashboardController.onPageLoad(None, None).url
  private lazy val draftDashboardPostRoute = routes.DashboardController.onSubmit(None).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DraftDepartureService].toInstance(draftDepartureService)
      )

  "DashboardController" - {

    "GET" - {
      "must return OK and the correct view" - {
        "when no sort param is passed" in {

          val draftDeparture =
            DeparturesSummary(
              2,
              2,
              List(
                DepartureUserAnswerSummary(LocalReferenceNumber("12345"), LocalDateTime.now(), 30),
                DepartureUserAnswerSummary(LocalReferenceNumber("67890"), LocalDateTime.now(), 29)
              )
            )

          when(draftDepartureService.getDrafts(any(), any(), any())(any()))
            .thenReturn(Future.successful(Option(draftDeparture)))

          val request = FakeRequest(GET, draftDashboardGetRoute)
          val result  = route(app, request).value

          val view      = injector.instanceOf[DashboardView]
          val viewModel = AllDraftDeparturesViewModel(draftDeparture, None, 1, 2)

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, viewModel)(request, messages).toString
        }

        "must redirect to technical difficulties when there is an error" in {

          when(draftDepartureService.getDrafts(any(), any(), any())(any()))
            .thenReturn(Future.successful(None))

          val request = FakeRequest(GET, draftDashboardGetRoute)
          val result  = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
        }
      }

      "POST" - {

        "must return OK and the correct view when given a search LRN" in {

          val draftDeparture =
            DeparturesSummary(
              0,
              0,
              List(
                DepartureUserAnswerSummary(LocalReferenceNumber("12345"), LocalDateTime.now(), 30),
                DepartureUserAnswerSummary(LocalReferenceNumber("67890"), LocalDateTime.now(), 29)
              )
            )

          when(draftDepartureService.getDrafts(any(), any(), any())(any()))
            .thenReturn(Future.successful(Option(draftDeparture)))

          val request = FakeRequest(POST, draftDashboardPostRoute)
            .withFormUrlEncodedBody(("value", "lrn"))

          val result = route(app, request).value

          status(result) mustEqual OK
        }

        "must return OK and the correct view when empty search" in {

          val draftDeparture =
            DeparturesSummary(
              0,
              0,
              List(
                DepartureUserAnswerSummary(LocalReferenceNumber("12345"), LocalDateTime.now(), 30),
                DepartureUserAnswerSummary(LocalReferenceNumber("67890"), LocalDateTime.now(), 29)
              )
            )

          when(draftDepartureService.getDrafts(any(), any(), any())(any()))
            .thenReturn(Future.successful(Option(draftDeparture)))

          val request = FakeRequest(POST, draftDashboardPostRoute)
            .withFormUrlEncodedBody(("value", ""))

          val result = route(app, request).value

          status(result) mustEqual OK
        }

        "must redirect to technical difficulties when there is an error" in {

          when(draftDepartureService.getDrafts(any(), any(), any())(any()))
            .thenReturn(Future.successful(None))

          val request = FakeRequest(POST, draftDashboardPostRoute)
            .withFormUrlEncodedBody(("value", "lrn"))

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
        }

      }
    }

  }
}
