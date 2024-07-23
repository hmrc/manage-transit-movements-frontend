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

package controllers.departureP5

import config.FrontendAppConfig
import controllers.actions._
import generated.CC009CType
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.DepartureNotCancelledP5ViewModel.DepartureNotCancelledP5ViewModelProvider
import views.html.departureP5.DepartureNotCancelledP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureNotCancelledP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: DepartureNotCancelledP5ViewModelProvider,
  view: DepartureNotCancelledP5View
)(implicit val executionContext: ExecutionContext, frontendAppConfig: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen messageRetrievalAction[CC009CType](departureId, messageId)).async {
      implicit request =>
        buildView(request.messageData, departureId, request.referenceNumbers.localReferenceNumber)
    }

  private def buildView(
    ie009: CC009CType,
    departureId: String,
    lrn: String
  )(implicit request: Request[_]): Future[Result] =
    viewModelProvider.apply(ie009, departureId, lrn).map {
      viewModel => Ok(view(viewModel))
    }
}
