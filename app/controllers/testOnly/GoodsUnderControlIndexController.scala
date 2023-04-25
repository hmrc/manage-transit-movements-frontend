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
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GoodsUnderControlIndexController @Inject() (
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  goodsUnderControlAction: GoodsUnderControlActionProvider
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String): Action[AnyContent] = (Action andThen identify andThen goodsUnderControlAction(departureId)) {
    implicit request =>

      val notificationType: String = request.ie060MessageData.TransitOperation.notificationType
      val call = if (request.ie060MessageData.requestedDocumentsToSeq.nonEmpty || notificationType == "1") {
        controllers.testOnly.routes.GoodsUnderControlP5Controller.requestedDocuments(departureId)
      } else {
        controllers.testOnly.routes.GoodsUnderControlP5Controller.noRequestedDocuments(departureId)
      }
      Redirect(call)
  }

}