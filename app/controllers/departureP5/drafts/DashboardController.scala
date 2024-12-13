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
import models.departure.drafts.{Limit, Skip}
import models.requests.IdentifierRequest
import models.{DeparturesSummary, Sort}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
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

  def onPageLoad(pageNumber: Option[Int], lrn: Option[String], sortParams: Option[String]): Action[AnyContent] =
    (Action andThen actions.identify()).async {
      implicit request =>
        buildView(form, pageNumber, lrn, Sort(sortParams))(Ok(_))
    }

  def onSubmit(sortParams: Option[String]): Action[AnyContent] =
    (Action andThen actions.identify()).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => buildView(formWithErrors)(BadRequest(_)),
            lrn => {
              val fuzzyLrn: Option[String] = Option(lrn).filter(_.trim.nonEmpty)
              buildView(form, lrn = fuzzyLrn, sortParams = Sort(sortParams))(Ok(_))
            }
          )
    }

  private def buildView(
    form: Form[String],
    pageNumber: Option[Int] = None,
    lrn: Option[String] = None,
    sortParams: Option[Sort] = None
  )(
    block: HtmlFormat.Appendable => Result
  )(implicit request: IdentifierRequest[?]): Future[Result] = {

    val page  = pageNumber.getOrElse(1)
    val skip  = Skip(page - 1)
    val limit = Limit(pageSize)

    draftDepartureService.sortOrGetDrafts(lrn, sortParams, limit, skip).map {
      case Some(drafts) =>
        block(view(form, present(drafts, page, lrn, sortParams)))
      case None =>
        Redirect(controllers.routes.ErrorController.technicalDifficulties())
    }
  }

  private def present(drafts: DeparturesSummary, page: Int, lrn: Option[String], sortParams: Option[Sort])(implicit
    request: IdentifierRequest[?]
  ): AllDraftDeparturesViewModel =
    AllDraftDeparturesViewModel(
      drafts,
      lrn,
      page,
      pageSize,
      sortParams
    )
}
