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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.arrival.ArrivalNotificationWithoutFunctionalErrorP5ViewModel.ArrivalNotificationWithoutFunctionalErrorP5ViewModelProvider
import views.html.arrival.P5.ArrivalNotificationWithoutFunctionalErrorsP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ArrivalNotificationWithoutFunctionalErrorsP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  rejectionMessageAction: ArrivalRejectionMessageActionProvider,
  viewModelProvider: ArrivalNotificationWithoutFunctionalErrorP5ViewModelProvider,
  view: ArrivalNotificationWithoutFunctionalErrorsP5View
)(implicit val executionContext: ExecutionContext, config: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(arrivalId: String, messageId: String): Action[AnyContent] =
    (Action andThen identify andThen rejectionMessageAction(arrivalId, messageId)) {
      implicit request =>
        if (request.ie057MessageData.functionalErrors.isEmpty) {
          Ok(view(viewModelProvider.apply(request.ie057MessageData.transitOperation.MRN)))
        } else {
          Redirect(controllers.routes.ErrorController.technicalDifficulties())
        }
    }
}
