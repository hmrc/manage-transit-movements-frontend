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

package controllers.arrival

import config.{FrontendAppConfig, PaginationAppConfig}
import controllers.actions.*
import generated.{CC057CType, Generated_CC057CTypeFormat}
import models.FunctionalErrorType
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FunctionalErrorsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.arrival.UnloadingRemarkWithFunctionalErrorsViewModel
import views.html.arrival.UnloadingRemarkWithFunctionalErrorsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarkWithFunctionalErrorsController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: ArrivalMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  view: UnloadingRemarkWithFunctionalErrorsView,
  config: FrontendAppConfig,
  functionalErrorsService: FunctionalErrorsService
)(implicit val executionContext: ExecutionContext, paginationConfig: PaginationAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(page: Option[Int], arrivalId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC057CType](arrivalId, messageId)).async {
      implicit request =>
        if (request.messageData.FunctionalError.nonEmpty) {
          functionalErrorsService.convertErrorsWithoutSection(request.messageData.FunctionalError.map(FunctionalErrorType(_))).map {
            functionalErrors =>
              val viewModel = UnloadingRemarkWithFunctionalErrorsViewModel(
                functionalErrors = functionalErrors,
                mrn = request.messageData.TransitOperation.MRN,
                currentPage = page,
                numberOfErrorsPerPage = paginationConfig.numberOfErrorsPerPage,
                arrivalId = arrivalId,
                messageId = messageId
              )

              Ok(view(viewModel, arrivalId, messageId))
          }
        } else {
          Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
        }
    }

  def onSubmit(arrivalId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC057CType](arrivalId, messageId)) {
      _ => Redirect(config.unloadingStart(arrivalId, messageId))
    }

}
