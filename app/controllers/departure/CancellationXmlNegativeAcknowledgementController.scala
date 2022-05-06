/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.departure

import config.FrontendAppConfig
import controllers.actions._
import handlers.ErrorHandler
import javax.inject.Inject
import models.DepartureId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.DepartureMessageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

class CancellationXmlNegativeAcknowledgementController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  val frontendAppConfig: FrontendAppConfig,
  departureMessageService: DepartureMessageService,
  renderer: Renderer,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: DepartureId): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      departureMessageService.getXMLSubmissionNegativeAcknowledgementMessage(departureId).flatMap {
        case Some(rejectionMessage) =>
          val json = Json.obj(
            "contactUrl"             -> frontendAppConfig.nctsEnquiriesUrl,
            "confirmCancellationUrl" -> frontendAppConfig.departureFrontendConfirmCancellationUrl(departureId),
            "functionalError"        -> rejectionMessage.error
          )

          renderer.render("cancellationXmlNegativeAcknowledgement.njk", json).map(Ok(_))
        case _ =>
          errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
      }
  }
}
