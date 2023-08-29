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
import controllers.actions._
import models.LocalReferenceNumber
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.DepartureDeclarationErrorsP5ViewModel._
import views.html.departure.TestOnly.DepartureDeclarationErrorsP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DepartureDeclarationErrorsP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  rejectionMessageAction: DepartureRejectionMessageActionProvider,
  viewModelProvider: DepartureDeclarationErrorsP5ViewModelProvider,
  view: DepartureDeclarationErrorsP5View
)(implicit val executionContext: ExecutionContext, config: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String, messageId: String, localReferenceNumber: LocalReferenceNumber): Action[AnyContent] =
    (Action andThen identify andThen rejectionMessageAction(departureId, localReferenceNumber, messageId)) {
      implicit request =>
        if (request.ie056MessageData.functionalErrors.isEmpty) {
          Ok(view(viewModelProvider.apply(request.lrn)))
        } else {
          Redirect(controllers.routes.ErrorController.technicalDifficulties())
        }
    }
}
