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

import connectors.{DeparturesMovementConnector, DeparturesMovementsP5Connector}
import logging.Logging
import models.DepartureId
import models.arrival.XMLSubmissionNegativeAcknowledgementMessage
import models.departure.{ControlDecision, NoReleaseForTransitMessage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import config.{FrontendAppConfig, PaginationAppConfig}
import controllers.actions._
import controllers.departure.drafts.routes
import forms.SearchFormProvider
import models.Sort.SortByCreatedAtDesc
import models.departure.drafts.{Limit, Skip}
import models.requests.IdentifierRequest
import models.{DeparturesSummary, Sort}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import services.DraftDepartureService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.drafts.AllDraftDeparturesViewModel
import viewModels.pagination.DraftsPaginationViewModel
import views.html.departure.drafts.DashboardView

class DraftDashboardService @Inject()(connector: DraftDepartureService,
                                      paginationAppConfig: PaginationAppConfig,
                                      appConfig: FrontendAppConfig,
                                      view: DashboardView
                                     )(implicit hc: HeaderCarrier, ec: ExecutionContext) {

  private lazy val pageSize = paginationAppConfig.draftDeparturesNumberOfDrafts

   def buildView(
                         form: Form[String],
                         pageNumber: Option[Int] = None,
                         lrn: Option[String] = None,
                         sortParams: Option[Sort] = None
                       )(
                         block: HtmlFormat.Appendable => Result
                       )(implicit request: IdentifierRequest[_]): Future[Result] = {

    val limit = Limit(pageSize)
    val page = pageNumber.getOrElse(1)
    val skip = Skip(page - 1)

    def sortOrGetDrafts: Future[Option[DeparturesSummary]] = (lrn, sortParams) match {
      case (Some(lrn), Some(sortParams)) => connector.sortDraftDepartures(sortParams, limit, skip, lrn)
      case (Some(lrn), None) => connector.getLRNs(lrn, skip, limit)
      case (None, Some(sortParams)) => connector.sortDraftDepartures(sortParams, limit, skip)
      case _ => connector.getPagedDepartureSummary(limit, skip)
    }

    sortOrGetDrafts.map {
      case Some(drafts) =>
        block(view(form, present(drafts, page, lrn, sortParams.getOrElse(SortByCreatedAtDesc))))
      case None =>
        Redirect(controllers.routes.ErrorController.technicalDifficulties())
    }
  }

  private def present(drafts: DeparturesSummary, page: Int, lrn: Option[String], sortParams: Sort): AllDraftDeparturesViewModel = {

    val additionalParams = Seq(
      lrn.map(("lrn", _)),
      Some(("sortParams", sortParams.toString))
    ).flatten

    val pvm = DraftsPaginationViewModel(
      totalNumberOfMovements = drafts.totalMatchingMovements,
      currentPage = page,
      numberOfMovementsPerPage = paginationAppConfig.draftDeparturesNumberOfDrafts,
      href = routes.DashboardController.onSubmit(None).url,
      additionalParams = additionalParams,
      lrn = lrn
    )

    AllDraftDeparturesViewModel(drafts, pageSize, lrn, appConfig.draftDepartureFrontendUrl, pvm, sortParams)
  }

}
