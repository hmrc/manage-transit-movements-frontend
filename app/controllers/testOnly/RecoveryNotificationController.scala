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

import controllers.actions._
import models.LocalReferenceNumber
import models.departureP5.{IE035Data, IE055Data}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.DepartureP5MessageService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.RecoveryNotificationViewModel.RecoveryNotificationViewModelProvider
import views.html.departure.TestOnly.RecoveryNotificationView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RecoveryNotificationController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  viewModelProvider: RecoveryNotificationViewModelProvider,
  view: RecoveryNotificationView,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen identify andThen messageRetrievalAction[IE035Data](departureId, messageId)) {
      implicit request =>
        Ok(view(viewModelProvider.apply(request.messageData.data), request.referenceNumbers.localReferenceNumber))
    }
}
