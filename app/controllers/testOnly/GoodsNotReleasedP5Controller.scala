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
import models.departureP5.IE051MessageData
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.GoodsNotReleasedP5ViewModel.GoodsNotReleasedP5ViewModelProvider
import views.html.departure.TestOnly.GoodsNotReleasedP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GoodsNotReleasedP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  goodsNotReleasedActionProvider: GoodsNotReleasedActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: GoodsNotReleasedP5ViewModelProvider,
  view: GoodsNotReleasedP5View
)(implicit val executionContext: ExecutionContext, frontendAppConfig: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def goodsNotReleased(departureId: String, localReferenceNumber: LocalReferenceNumber, messageId: String): Action[AnyContent] =
    (Action andThen identify andThen goodsNotReleasedActionProvider(departureId, messageId)) {
      implicit request =>
        buildView(request.transitOperationIE051, localReferenceNumber)
    }

  private def buildView(iE051MessageData: IE051MessageData, lrn: LocalReferenceNumber)(implicit request: Request[_]): Result =
    Ok(view(viewModelProvider.apply(iE051MessageData, lrn.value)))
}