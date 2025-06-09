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

import config.PaginationAppConfig
import controllers.actions.*
import forms.DeparturesSearchFormProvider
import models.DeparturesSummary
import models.departure.drafts.{Limit, Skip}
import models.requests.IdentifierRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.DraftDepartureService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.drafts.AllDraftDeparturesViewModel
import views.html.departureP5.drafts.DashboardView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DashboardController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  draftDepartureService: DraftDepartureService,
  view: DashboardView,
  formProvider: DeparturesSearchFormProvider,
  paginationAppConfig: PaginationAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  private lazy val pageSize = paginationAppConfig.draftDeparturesNumberOfDrafts

  def onPageLoad(search: Option[String], pageNumber: Option[Int]): Action[AnyContent] =
    (Action andThen actions.identify()).async {
      implicit request =>
        buildView(form.fillAndValidate(search), search, pageNumber)
    }

  def onSubmit(): Action[AnyContent] =
    (Action andThen actions.identify()).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => buildView(formWithErrors),
            {
              case Some(lrn) if lrn.trim.nonEmpty =>
                Future.successful(Redirect(routes.DashboardController.onPageLoad(Some(lrn), None)))
              case _ =>
                Future.successful(Redirect(routes.DashboardController.onPageLoad(None, None)))
            }
          )
    }

  private def buildView(
    form: Form[Option[String]],
    search: Option[String] = None,
    pageNumber: Option[Int] = None
  )(implicit request: IdentifierRequest[?]): Future[Result] = {
    val page = pageNumber.getOrElse(1)

    if (form.hasErrors) {
      val viewModel = AllDraftDeparturesViewModel(DeparturesSummary(), search, page, pageSize)
      Future.successful(BadRequest(view(form, viewModel)))
    } else {
      val skip  = Skip(page - 1)
      val limit = Limit(pageSize)

      draftDepartureService.getDrafts(search, limit, skip).map {
        case Some(drafts) =>
          val viewModel = AllDraftDeparturesViewModel(drafts, search, page, pageSize)
          Ok(view(form, viewModel))
        case None =>
          Redirect(controllers.routes.ErrorController.technicalDifficulties())
      }
    }
  }
}
