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

import config.Constants.NotificationType._
import controllers.actions._
import controllers.routes
import generated.CC060CType
import play.api.Logging
import models.RichCC060Type
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import generated.Generated_CC060CTypeFormat

class GoodsUnderControlIndexController @Inject() (
  actions: Actions,
  cc: MessagesControllerComponents,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider
) extends FrontendController(cc)
    with I18nSupport
    with Logging {

  def onPageLoad(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen messageRetrievalAction[CC060CType](departureId, messageId)) {
      implicit request =>
        val call = request.messageData.TransitOperation.notificationType match {
          case DecisionToControl =>
            controllers.departureP5.routes.GoodsUnderControlP5Controller.noRequestedDocuments(departureId, messageId)
          case AdditionalDocumentsRequest =>
            controllers.departureP5.routes.GoodsUnderControlP5Controller.requestedDocuments(departureId, messageId)
          case IntentionToControl =>
            if (request.messageData.informationRequested) {
              controllers.departureP5.routes.IntentionToControlP5Controller.informationRequested(departureId, messageId)
            } else {
              controllers.departureP5.routes.IntentionToControlP5Controller.noInformationRequested(departureId, messageId)
            }
          case x =>
            logger.warn(s"Unexpected notification type: $x")
            routes.ErrorController.technicalDifficulties()
        }
        Redirect(call)
    }

}
