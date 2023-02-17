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

import config.{FrontendAppConfig, PaginationAppConfig}
import controllers.actions._
import forms.SearchFormProvider
import models.DeparturesSummary
import models.departure.drafts.{Limit, Skip}
import models.requests.IdentifierRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import services.DraftDepartureService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.drafts.AllDraftDeparturesViewModel
import viewModels.pagination.DraftsPaginationViewModel
import views.html.departure.drafts.DashboardView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DashboardController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  draftDepartureService: DraftDepartureService,
  view: DashboardView,
  formProvider: SearchFormProvider,
  paginationAppConfig: PaginationAppConfig,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  private lazy val pageSize = paginationAppConfig.draftDeparturesNumberOfDrafts

  def onPageLoad(pageNumber: Option[Int], lrn: Option[String]): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      buildView(form, pageNumber, lrn)(Ok(_))
  }

  def onSubmit(): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => buildView(formWithErrors)(BadRequest(_)),
          lrn => {
            val fuzzyLrn: Option[String] = Option(lrn).filter(_.trim.nonEmpty)
            buildView(form, lrn = fuzzyLrn)(Ok(_))
          }
        )
  }

  private def buildView(form: Form[String], pageNumber: Option[Int] = None, lrn: Option[String] = None)(
    block: HtmlFormat.Appendable => Result
  )(implicit request: IdentifierRequest[_]): Future[Result] = {

    val limit = Limit(pageSize)
    val page  = pageNumber.getOrElse(1)

    val skip = Skip(page - 1)

    val getDrafts = lrn match {
      case Some(value) => draftDepartureService.getLRNs(value, skip, limit)
      case None        => draftDepartureService.getPagedDepartureSummary(limit, skip)
    }

    getDrafts.map {
      case Some(drafts) =>
        block(view(form, present(drafts, page, lrn)))
      case None =>
        Redirect(controllers.routes.ErrorController.technicalDifficulties())
    }
  }

  private def present(drafts: DeparturesSummary, page: Int, lrn: Option[String]): AllDraftDeparturesViewModel = {

    val additionalParams = lrn.fold[Seq[(String, String)]](Nil) {
      value => Seq(("lrn", value))
    }

    val pvm = DraftsPaginationViewModel(
      totalNumberOfMovements = drafts.totalMatchingMovements,
      currentPage = page,
      numberOfMovementsPerPage = paginationAppConfig.draftDeparturesNumberOfDrafts,
      href = routes.DashboardController.onSubmit().url,
      additionalParams = additionalParams,
      lrn = lrn
    )

    AllDraftDeparturesViewModel(drafts, pageSize, lrn, appConfig.draftDepartureFrontendUrl, pvm)
  }

}
