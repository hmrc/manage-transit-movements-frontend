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

package controllers.arrivalP5

import config.{FrontendAppConfig, PaginationAppConfig}
import controllers.actions._
import generated.CC057CType
import models.RichCC057CType
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.arrival.UnloadingRemarkWithFunctionalErrorsP5ViewModel.UnloadingRemarkWithFunctionalErrorsP5ViewModelProvider
import viewModels.pagination.ListPaginationViewModel
import views.html.arrivalP5.UnloadingRemarkWithFunctionalErrorsP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UnloadingRemarkWithFunctionalErrorsP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: ArrivalMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: UnloadingRemarkWithFunctionalErrorsP5ViewModelProvider,
  view: UnloadingRemarkWithFunctionalErrorsP5View,
  config: FrontendAppConfig
)(implicit val executionContext: ExecutionContext, paginationConfig: PaginationAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(page: Option[Int], arrivalId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC057CType](arrivalId, messageId)).async {
      implicit request =>
        val currentPage = page.getOrElse(1)

        val paginationViewModel = ListPaginationViewModel(
          totalNumberOfItems = request.messageData.FunctionalError.length,
          currentPage = currentPage,
          numberOfItemsPerPage = paginationConfig.arrivalsNumberOfErrorsPerPage,
          href = controllers.arrivalP5.routes.UnloadingRemarkWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalId, messageId).url
        )

        val rejectionMessageP5ViewModel = viewModelProvider.apply(
          request.messageData.pagedFunctionalErrors(currentPage),
          request.messageData.TransitOperation.MRN
        )

        rejectionMessageP5ViewModel.map(
          viewModel =>
            if (request.messageData.FunctionalError.nonEmpty) {
              Ok(view(viewModel, arrivalId, messageId, paginationViewModel))
            } else {
              Redirect(controllers.routes.ErrorController.technicalDifficulties())
            }
        )
    }

  def onSubmit(arrivalId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC057CType](arrivalId, messageId)) {
      _ => Redirect(config.p5UnloadingStart(arrivalId, messageId))
    }
}
