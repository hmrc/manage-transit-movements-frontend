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

import config.{FrontendAppConfig, SearchResultsAppConfig}
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
  formProvider: SearchFormProvider,
  searchResultsAppConfig: SearchResultsAppConfig,
  view: DashboardView,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  private lazy val pageSize = searchResultsAppConfig.maxSearchResults

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
          lrn =>
            lrn.trim match {
              case "" => Future.successful(Redirect(controllers.departure.drafts.routes.DashboardController.onPageLoad()))
              case lrn =>
                draftDepartureService.getLRNs(lrn, pageSize).map {
                  case Some(draft) =>
                    val toViewModel = AllDraftDeparturesViewModel(draft)
                    Ok(view(form, toViewModel, Some(lrn), toViewModel.draftDepartures > pageSize, isSearch = true))
                  case None => Redirect(controllers.routes.ErrorController.technicalDifficulties())
                }
            }
        )
  }

  private def buildView(form: Form[String])(
    block: HtmlFormat.Appendable => Result
  )(implicit request: IdentifierRequest[_]): Future[Result] =
    draftDepartureService.getAll().map {
      case Some(drafts) =>
        val toViewModel = AllDraftDeparturesViewModel(drafts)

        block(view(form, toViewModel, None, tooManyResults = false, isSearch = false))

      case None =>
        Redirect(controllers.routes.ErrorController.technicalDifficulties())
    }

}
