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
import config.PaginationAppConfig
import config.FrontendAppConfig
import controllers.actions._
import forms.SearchFormProvider
import models.requests.IdentifierRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import services.DraftDepartureService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.drafts.AllDraftDeparturesViewModel
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

  def onPageLoad(): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      buildView(form)(Ok(_))
  }

  def onSubmit: Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => buildView(formWithErrors)(BadRequest(_)),
          lrn => {
            val fuzzyLrn: Option[String] = Option(lrn).filter(_.trim.nonEmpty)
            buildView(form, fuzzyLrn)(Ok(_))
          }
        )
  }

  private def buildView(form: Form[String], lrn: Option[String] = None)(
    block: HtmlFormat.Appendable => Result
  )(implicit request: IdentifierRequest[_]): Future[Result] = {

    val getDrafts = lrn match {
      case Some(value) => draftDepartureService.getLRNs(value, pageSize)
      case None        => draftDepartureService.getAll()
    }

    getDrafts.map {
      case Some(drafts) =>
        val toViewModel = AllDraftDeparturesViewModel(drafts, pageSize, lrn, appConfig.draftDepartureFrontendUrl)
        block(view(form, toViewModel))
      case None =>
        Redirect(controllers.routes.ErrorController.technicalDifficulties())
    }
  }

}
