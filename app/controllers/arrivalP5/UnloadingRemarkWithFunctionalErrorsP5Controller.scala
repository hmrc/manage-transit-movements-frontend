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
import controllers.actions.*
import generated.{CC057CType, Generated_CC057CTypeFormat}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FunctionalErrorsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.arrival.UnloadingRemarkWithFunctionalErrorsP5ViewModel
import views.html.arrivalP5.UnloadingRemarkWithFunctionalErrorsP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarkWithFunctionalErrorsP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: ArrivalMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  view: UnloadingRemarkWithFunctionalErrorsP5View,
  config: FrontendAppConfig,
  functionalErrorsService: FunctionalErrorsService
)(implicit val executionContext: ExecutionContext, paginationConfig: PaginationAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(page: Option[Int], arrivalId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC057CType](arrivalId, messageId)).async {
      implicit request =>
        if (request.messageData.FunctionalError.nonEmpty) {
          functionalErrorsService.convertErrorsWithoutSection(request.messageData.FunctionalError).map {
            functionalErrors =>
              val viewModel = UnloadingRemarkWithFunctionalErrorsP5ViewModel(
                functionalErrors = functionalErrors,
                mrn = request.messageData.TransitOperation.MRN,
                currentPage = page,
                numberOfErrorsPerPage = paginationConfig.arrivalsNumberOfErrorsPerPage,
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
      _ => Redirect(config.p5UnloadingStart(arrivalId, messageId))
    }

}
