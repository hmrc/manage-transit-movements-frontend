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
import generated.CC051CType
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.GoodsNotReleasedP5ViewModel.GoodsNotReleasedP5ViewModelProvider
import views.html.departureP5.GoodsNotReleasedP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import generated.Generated_CC051CTypeFormat

class GoodsNotReleasedP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: GoodsNotReleasedP5ViewModelProvider,
  view: GoodsNotReleasedP5View
)(implicit val executionContext: ExecutionContext, frontendAppConfig: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def goodsNotReleased(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen messageRetrievalAction[CC051CType](departureId, messageId)) {
      implicit request =>
        Ok(
          view(viewModelProvider.apply(request.messageData, request.referenceNumbers.localReferenceNumber))
        )
    }

}
