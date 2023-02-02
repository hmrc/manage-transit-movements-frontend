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
import handlers.ErrorHandler
import models.requests.IdentifierRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.DraftDepartureService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.drafts.AllDraftDeparturesViewModel
import views.html.departure.drafts.DraftDeparturesSearchResultsView

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DraftDeparturesSearchResultsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  draftDepartureService: DraftDepartureService,
  searchResultsAppConfig: SearchResultsAppConfig,
  formProvider: SearchFormProvider,
  view: DraftDeparturesSearchResultsView,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext, frontendAppConfig: FrontendAppConfig, clock: Clock)
    extends FrontendController(cc)
    with I18nSupport {

  private val form = formProvider()

  private lazy val pageSize = searchResultsAppConfig.maxSearchResults

  def onPageLoad(lrn: String): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      draftDepartureService.getLRNs(lrn, pageSize).map {
        case Some(draft) =>
          val toViewModel = AllDraftDeparturesViewModel(draft)
          Ok(view(form, lrn, toViewModel, toViewModel.draftDepartures > pageSize))
        case None => Redirect(controllers.routes.ErrorController.technicalDifficulties())
      }
  }

  def onSubmit(lrn: String): Action[AnyContent] = ???

}
