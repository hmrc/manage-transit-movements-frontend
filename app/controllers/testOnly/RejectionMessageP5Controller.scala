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

package controllers.testOnly

import config.FrontendAppConfig
import connectors.DepartureCacheConnector
import controllers.actions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.RejectionMessageP5ViewModel.RejectionMessageP5ViewModelProvider
import views.html.departure.TestOnly.RejectionMessageP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RejectionMessageP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  rejectionMessageAction: RejectionMessageActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: RejectionMessageP5ViewModelProvider,
  cacheConnector: DepartureCacheConnector,
  view: RejectionMessageP5View
)(implicit val executionContext: ExecutionContext, config: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String): Action[AnyContent] = (Action andThen identify andThen rejectionMessageAction(departureId)).async {
    implicit request =>
      if (request.isDeclarationAmendable) {
        val rejectionMessageP5ViewModel = viewModelProvider.apply(request.ie056MessageData, request.lrn)
        rejectionMessageP5ViewModel.map(
          vmp => Ok(view(vmp, departureId))
        )
      } else {
        Future.successful(
          Redirect(controllers.routes.SessionExpiredController.onPageLoad())
        ) // TODO: Redirect to generic error page with link back to dashboard?
      }
  }

  def onAmend(departureId: String): Action[AnyContent] = (Action andThen identify andThen rejectionMessageAction(departureId)).async {
    implicit request =>
      val xPaths = request.ie056MessageData.functionalErrors.map(_.errorPointer)
      if (request.isDeclarationAmendable && xPaths.nonEmpty) {
        cacheConnector.handleErrors(request.lrn, xPaths).map {
          case true =>
            Redirect(config.departureFrontendTaskListUrl(request.lrn))
          case false =>
            Redirect(controllers.routes.ErrorController.technicalDifficulties())
        }
      } else {
        Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
      }
  }

}
