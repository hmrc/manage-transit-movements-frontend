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

package controllers.departure.drafts

import base.{AppWithDefaultMockFixtures, SpecBase}
import models.{DepartureUserAnswerSummary, DeparturesSummary, LocalReferenceNumber}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DraftDepartureService

import java.time.LocalDateTime
import scala.concurrent.Future

class DashboardControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val draftDepartureService = mock[DraftDepartureService]

  private lazy val draftDashboardRoute = routes.DashboardController.onPageLoad().url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DraftDepartureService].toInstance(draftDepartureService)
      )

  "DraftDashboard Controller" - {

    "GET" - {
      "must return OK and the correct view" in {

        val draftDeparture =
          DeparturesSummary(
            List(
              DepartureUserAnswerSummary(LocalReferenceNumber("12345"), LocalDateTime.now(), 30),
              DepartureUserAnswerSummary(LocalReferenceNumber("67890"), LocalDateTime.now(), 29)
            )
          )

        when(draftDepartureService.getAll(any())(any())).thenReturn(Future.successful(Some(draftDeparture)))

        val request = FakeRequest(GET, draftDashboardRoute)
        val result  = route(app, request).value

        status(result) mustEqual OK
      }

      "must redirect to technical difficulties when there is an error" in {

        when(draftDepartureService.getAll(any())(any())).thenReturn(Future.successful(None))

        val request = FakeRequest(GET, draftDashboardRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
      }
    }

    "POST" - {

      "must return OK and the correct view when given a search LRN" in {

        val draftDeparture =
          DeparturesSummary(
            List(
              DepartureUserAnswerSummary(LocalReferenceNumber("12345"), LocalDateTime.now(), 30),
              DepartureUserAnswerSummary(LocalReferenceNumber("67890"), LocalDateTime.now(), 29)
            )
          )

        when(draftDepartureService.getLRNs(any(), any())(any())).thenReturn(Future.successful(Some(draftDeparture)))

        val request = FakeRequest(POST, draftDashboardRoute)
          .withFormUrlEncodedBody(("value", "lrn"))

        val result = route(app, request).value

        status(result) mustEqual OK
      }

      "must return OK and the correct view when empty search" in {

        val draftDeparture =
          DeparturesSummary(
            List(
              DepartureUserAnswerSummary(LocalReferenceNumber("12345"), LocalDateTime.now(), 30),
              DepartureUserAnswerSummary(LocalReferenceNumber("67890"), LocalDateTime.now(), 29)
            )
          )

        when(draftDepartureService.getAll(any())(any())).thenReturn(Future.successful(Some(draftDeparture)))

        val request = FakeRequest(POST, draftDashboardRoute)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(app, request).value

        status(result) mustEqual OK
      }

      "must redirect to technical difficulties when there is an error" in {

        when(draftDepartureService.getLRNs(any(), any())(any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, draftDashboardRoute)
          .withFormUrlEncodedBody(("value", "lrn"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
      }

    }
  }
}
