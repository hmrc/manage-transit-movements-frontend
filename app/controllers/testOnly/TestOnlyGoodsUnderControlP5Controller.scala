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
import handlers.ErrorHandler
import models.DepartureId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{DepartureMessageService, DepartureP5MessageService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.arrival.P5.TestOnlyGoodsUnderControlP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TestOnlyGoodsUnderControlP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  departureP5MessageService: DepartureP5MessageService,
  errorHandler: ErrorHandler,
  view: TestOnlyGoodsUnderControlP5View
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      departureP5MessageService
        .getGoodsUnderControl(departureId)
        .map {
          x => Ok(view(x))
        }
  }
}
