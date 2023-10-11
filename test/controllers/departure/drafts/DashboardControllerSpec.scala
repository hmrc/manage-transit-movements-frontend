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

import base.SpecBase
import forms.DeparturesSearchFormProvider
import models.Sort.{SortByCreatedAtAsc, SortByCreatedAtDesc, SortByLRNAsc, SortByLRNDesc}
import models.departure.drafts.{Limit, Skip}
import models.{DepartureUserAnswerSummary, DeparturesSummary, LocalReferenceNumber}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DraftDepartureService
import viewModels.drafts.AllDraftDeparturesViewModel
import viewModels.pagination.ListPaginationViewModel
import views.html.departure.drafts.DashboardView

import java.time.LocalDateTime
import scala.concurrent.Future

class DashboardControllerSpec extends SpecBase {

  private val draftDepartureService = mock[DraftDepartureService]

  private val formProvider                 = new DeparturesSearchFormProvider()
  private val form                         = formProvider()
  private lazy val draftDashboardGetRoute  = routes.DashboardController.onPageLoad(None, None, None).url
  private lazy val draftDashboardPostRoute = routes.DashboardController.onSubmit(None).url

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DraftDepartureService].toInstance(draftDepartureService)
      )

  "DraftDashboard Controller" - {

    "GET" - {
      "must return OK and the correct view when a pagedDepartureSummary is returned" in {

        val draftDeparture =
          DeparturesSummary(
            2,
            2,
            List(
              DepartureUserAnswerSummary(LocalReferenceNumber("12345"), LocalDateTime.now(), 30),
              DepartureUserAnswerSummary(LocalReferenceNumber("67890"), LocalDateTime.now(), 29)
            )
          )

        when(draftDepartureService.getPagedDepartureSummary(Limit(any()), Skip(any()))(any())).thenReturn(Future.successful(Option(draftDeparture)))

        val request = FakeRequest(GET, draftDashboardGetRoute)
        val result  = route(app, request).value

        val view                = injector.instanceOf[DashboardView]
        val paginationViewModel = ListPaginationViewModel(draftDeparture.userAnswers.length, 1, 2, "test")
        val viewModel =
          AllDraftDeparturesViewModel(draftDeparture, draftDeparture.userAnswers.length, None, frontendAppConfig.departureFrontendUrl, paginationViewModel)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, viewModel)(request, messages).toString
      }

      "must return OK correct VIEW for a get" - {
        "when sortParam lrn.asc is passed" in {
          val sortParam = SortByLRNAsc

          lazy val draftDashboardGetRoute = routes.DashboardController.onPageLoad(None, None, Some(sortParam.convertParams)).url

          val draftDeparture =
            DeparturesSummary(
              4,
              4,
              List(
                DepartureUserAnswerSummary(LocalReferenceNumber("4123"), LocalDateTime.now(), 27),
                DepartureUserAnswerSummary(LocalReferenceNumber("3412"), LocalDateTime.now(), 28),
                DepartureUserAnswerSummary(LocalReferenceNumber("2341"), LocalDateTime.now(), 29),
                DepartureUserAnswerSummary(LocalReferenceNumber("1234"), LocalDateTime.now(), 30)
              )
            )

          when(draftDepartureService.sortDraftDepartures(any(), Limit(any()), Skip(any()))(any())).thenReturn(Future.successful(Option(draftDeparture)))

          val request = FakeRequest(GET, draftDashboardGetRoute)
          val result  = route(app, request).value

          val view = injector.instanceOf[DashboardView]
          val paginationViewModel = ListPaginationViewModel(draftDeparture.userAnswers.length,
                                                            1,
                                                            4,
                                                            routes.DashboardController.onSubmit(None).url,
                                                            Seq(("sortParams", sortParam.convertParams))
          )
          val viewModel =
            AllDraftDeparturesViewModel(draftDeparture,
                                        draftDeparture.userAnswers.length,
                                        None,
                                        frontendAppConfig.departureFrontendUrl,
                                        paginationViewModel,
                                        sortParams = sortParam
            )

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, viewModel)(request, messages).toString
        }
        "when sortParam lrn.desc is passed" in {
          val sortParam = SortByLRNDesc

          lazy val draftDashboardGetRoute = routes.DashboardController.onPageLoad(None, None, Some(sortParam.convertParams)).url

          val draftDeparture =
            DeparturesSummary(
              4,
              4,
              List(
                DepartureUserAnswerSummary(LocalReferenceNumber("1234"), LocalDateTime.now(), 30),
                DepartureUserAnswerSummary(LocalReferenceNumber("2341"), LocalDateTime.now(), 29),
                DepartureUserAnswerSummary(LocalReferenceNumber("3412"), LocalDateTime.now(), 28),
                DepartureUserAnswerSummary(LocalReferenceNumber("4123"), LocalDateTime.now(), 27)
              )
            )

          when(draftDepartureService.sortDraftDepartures(any(), Limit(any()), Skip(any()))(any())).thenReturn(Future.successful(Option(draftDeparture)))

          val request = FakeRequest(GET, draftDashboardGetRoute)
          val result  = route(app, request).value

          val view = injector.instanceOf[DashboardView]
          val paginationViewModel = ListPaginationViewModel(draftDeparture.userAnswers.length,
                                                            1,
                                                            4,
                                                            routes.DashboardController.onSubmit(None).url,
                                                            Seq(("sortParams", sortParam.convertParams))
          )
          val viewModel =
            AllDraftDeparturesViewModel(draftDeparture,
                                        draftDeparture.userAnswers.length,
                                        None,
                                        frontendAppConfig.departureFrontendUrl,
                                        paginationViewModel,
                                        sortParams = sortParam
            )

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, viewModel)(request, messages).toString
        }
        "when sortParam createdAt.asc is passed" in {
          val sortParam = SortByCreatedAtAsc

          lazy val draftDashboardGetRoute = routes.DashboardController.onPageLoad(None, None, Some(sortParam.convertParams)).url

          val draftDeparture =
            DeparturesSummary(
              4,
              4,
              List(
                DepartureUserAnswerSummary(LocalReferenceNumber("1234"), LocalDateTime.now(), 30),
                DepartureUserAnswerSummary(LocalReferenceNumber("2341"), LocalDateTime.now(), 29),
                DepartureUserAnswerSummary(LocalReferenceNumber("3412"), LocalDateTime.now(), 28),
                DepartureUserAnswerSummary(LocalReferenceNumber("4123"), LocalDateTime.now(), 27)
              )
            )

          when(draftDepartureService.sortDraftDepartures(any(), Limit(any()), Skip(any()))(any())).thenReturn(Future.successful(Option(draftDeparture)))

          val request = FakeRequest(GET, draftDashboardGetRoute)
          val result  = route(app, request).value

          val view = injector.instanceOf[DashboardView]
          val paginationViewModel = ListPaginationViewModel(draftDeparture.userAnswers.length,
                                                            1,
                                                            4,
                                                            routes.DashboardController.onSubmit(None).url,
                                                            Seq(("sortParams", sortParam.convertParams))
          )
          val viewModel =
            AllDraftDeparturesViewModel(draftDeparture,
                                        draftDeparture.userAnswers.length,
                                        None,
                                        frontendAppConfig.departureFrontendUrl,
                                        paginationViewModel,
                                        sortParams = sortParam
            )

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, viewModel)(request, messages).toString
        }
        "when sortParam createdAt.desc is passed" in {
          val sortParam = SortByCreatedAtDesc

          lazy val draftDashboardGetRoute = routes.DashboardController.onPageLoad(None, None, Some(sortParam.convertParams)).url

          val draftDeparture =
            DeparturesSummary(
              4,
              4,
              List(
                DepartureUserAnswerSummary(LocalReferenceNumber("1234"), LocalDateTime.now(), 30),
                DepartureUserAnswerSummary(LocalReferenceNumber("2341"), LocalDateTime.now(), 29),
                DepartureUserAnswerSummary(LocalReferenceNumber("3412"), LocalDateTime.now(), 28),
                DepartureUserAnswerSummary(LocalReferenceNumber("4123"), LocalDateTime.now(), 27)
              )
            )

          when(draftDepartureService.sortDraftDepartures(any(), Limit(any()), Skip(any()))(any())).thenReturn(Future.successful(Option(draftDeparture)))

          val request = FakeRequest(GET, draftDashboardGetRoute)
          val result  = route(app, request).value

          val view = injector.instanceOf[DashboardView]
          val paginationViewModel = ListPaginationViewModel(draftDeparture.userAnswers.length,
                                                            1,
                                                            4,
                                                            routes.DashboardController.onSubmit(None).url,
                                                            Seq(("sortParams", sortParam.convertParams))
          )
          val viewModel =
            AllDraftDeparturesViewModel(draftDeparture,
                                        draftDeparture.userAnswers.length,
                                        None,
                                        frontendAppConfig.departureFrontendUrl,
                                        paginationViewModel,
                                        sortParams = sortParam
            )

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, viewModel)(request, messages).toString
        }

        "when sortParam lrn.asc is passed along with an LRN" in {
          val sortParam = SortByLRNAsc
          val lrn       = "123"

          lazy val draftDashboardGetRoute = routes.DashboardController.onPageLoad(None, Some(lrn), Some(sortParam.convertParams)).url

          val draftDeparture =
            DeparturesSummary(
              2,
              2,
              List(
                DepartureUserAnswerSummary(LocalReferenceNumber("1234"), LocalDateTime.now(), 27),
                DepartureUserAnswerSummary(LocalReferenceNumber("1235"), LocalDateTime.now(), 28)
              )
            )

          when(draftDepartureService.sortDraftDepartures(any(), Limit(any()), Skip(any()), any())(any())).thenReturn(Future.successful(Option(draftDeparture)))

          val request = FakeRequest(GET, draftDashboardGetRoute)
          val result  = route(app, request).value

          val view = injector.instanceOf[DashboardView]
          val paginationViewModel = ListPaginationViewModel(
            draftDeparture.userAnswers.length,
            1,
            4,
            routes.DashboardController.onSubmit(None).url,
            Seq(("lrn", lrn), ("sortParams", sortParam.convertParams))
          )

          val viewModel =
            AllDraftDeparturesViewModel(
              draftDeparture,
              draftDeparture.userAnswers.length,
              Some(lrn),
              frontendAppConfig.departureFrontendUrl,
              paginationViewModel,
              sortParams = sortParam
            )

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, viewModel)(request, messages).toString
        }
        "when sortParam lrn.desc is passed along with an LRN" in {
          val sortParam = SortByLRNDesc
          val lrn       = "123"

          lazy val draftDashboardGetRoute = routes.DashboardController.onPageLoad(None, Some(lrn), Some(sortParam.convertParams)).url

          val draftDeparture =
            DeparturesSummary(
              2,
              2,
              List(
                DepartureUserAnswerSummary(LocalReferenceNumber("1235"), LocalDateTime.now(), 28),
                DepartureUserAnswerSummary(LocalReferenceNumber("1234"), LocalDateTime.now(), 27)
              )
            )

          when(draftDepartureService.sortDraftDepartures(any(), Limit(any()), Skip(any()), any())(any())).thenReturn(Future.successful(Option(draftDeparture)))

          val request = FakeRequest(GET, draftDashboardGetRoute)
          val result  = route(app, request).value

          val view = injector.instanceOf[DashboardView]
          val paginationViewModel = ListPaginationViewModel(
            draftDeparture.userAnswers.length,
            1,
            4,
            routes.DashboardController.onSubmit(None).url,
            Seq(("lrn", lrn), ("sortParams", sortParam.convertParams))
          )

          val viewModel =
            AllDraftDeparturesViewModel(
              draftDeparture,
              draftDeparture.userAnswers.length,
              Some(lrn),
              frontendAppConfig.departureFrontendUrl,
              paginationViewModel,
              sortParams = sortParam
            )

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, viewModel)(request, messages).toString
        }
        "when sortParam createdAt.asc is passed along with an LRN" in {
          val sortParam = SortByCreatedAtAsc
          val lrn       = "123"

          lazy val draftDashboardGetRoute = routes.DashboardController.onPageLoad(None, Some(lrn), Some(sortParam.convertParams)).url

          val draftDeparture =
            DeparturesSummary(
              2,
              2,
              List(
                DepartureUserAnswerSummary(LocalReferenceNumber("1234"), LocalDateTime.now(), 27),
                DepartureUserAnswerSummary(LocalReferenceNumber("1235"), LocalDateTime.now(), 28)
              )
            )

          when(draftDepartureService.sortDraftDepartures(any(), Limit(any()), Skip(any()), any())(any())).thenReturn(Future.successful(Option(draftDeparture)))

          val request = FakeRequest(GET, draftDashboardGetRoute)
          val result  = route(app, request).value

          val view = injector.instanceOf[DashboardView]
          val paginationViewModel = ListPaginationViewModel(
            draftDeparture.userAnswers.length,
            1,
            4,
            routes.DashboardController.onSubmit(None).url,
            Seq(("lrn", lrn), ("sortParams", sortParam.convertParams))
          )

          val viewModel =
            AllDraftDeparturesViewModel(
              draftDeparture,
              draftDeparture.userAnswers.length,
              Some(lrn),
              frontendAppConfig.departureFrontendUrl,
              paginationViewModel,
              sortParams = sortParam
            )

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, viewModel)(request, messages).toString
        }
        "when sortParam createdAt.desc is passed along with an LRN" in {
          val sortParam = SortByCreatedAtDesc
          val lrn       = "123"

          lazy val draftDashboardGetRoute = routes.DashboardController.onPageLoad(None, Some(lrn), Some(sortParam.convertParams)).url

          val draftDeparture =
            DeparturesSummary(
              2,
              2,
              List(
                DepartureUserAnswerSummary(LocalReferenceNumber("1235"), LocalDateTime.now(), 28),
                DepartureUserAnswerSummary(LocalReferenceNumber("1234"), LocalDateTime.now(), 27)
              )
            )

          when(draftDepartureService.sortDraftDepartures(any(), Limit(any()), Skip(any()), any())(any())).thenReturn(Future.successful(Option(draftDeparture)))

          val request = FakeRequest(GET, draftDashboardGetRoute)
          val result  = route(app, request).value

          val view = injector.instanceOf[DashboardView]
          val paginationViewModel = ListPaginationViewModel(
            draftDeparture.userAnswers.length,
            1,
            4,
            routes.DashboardController.onSubmit(None).url,
            Seq(("lrn", lrn), ("sortParams", sortParam.convertParams))
          )

          val viewModel =
            AllDraftDeparturesViewModel(
              draftDeparture,
              draftDeparture.userAnswers.length,
              Some(lrn),
              frontendAppConfig.departureFrontendUrl,
              paginationViewModel,
              sortParams = sortParam
            )

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, viewModel)(request, messages).toString
        }
      }

      "must redirect to technical difficulties when there is an error" in {

        when(draftDepartureService.getPagedDepartureSummary(Limit(any()), Skip(any()))(any())).thenReturn(Future.successful(None))

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

        when(draftDepartureService.getLRNs(any(), Skip(any()), Limit(any()))(any())).thenReturn(Future.successful(Option(draftDeparture)))

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

        when(draftDepartureService.getPagedDepartureSummary(Limit(any()), Skip(any()))(any())).thenReturn(Future.successful(Option(draftDeparture)))

        val request = FakeRequest(POST, draftDashboardPostRoute)
          .withFormUrlEncodedBody(("value", ""))

        val result = route(app, request).value

        status(result) mustEqual OK
      }

      "must redirect to technical difficulties when there is an error" in {

        when(draftDepartureService.getLRNs(any(), Skip(any()), Limit(any()))(any())).thenReturn(Future.successful(None))

        val request = FakeRequest(POST, draftDashboardPostRoute)
          .withFormUrlEncodedBody(("value", "lrn"))

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
      }

    }
  }
}
