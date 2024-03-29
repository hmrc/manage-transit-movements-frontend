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

package controllers.arrival

import connectors.ArrivalMovementConnector
import controllers.actions.IdentifierAction
import handlers.ErrorHandler
import models.ArrivalId
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSResponse
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnloadingPermissionPDFController @Inject() (
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  arrivalMovementConnector: ArrivalMovementConnector,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport
    with Logging {

  def getPDF(arrivalId: ArrivalId): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      hc.authorization
        .map {
          token =>
            arrivalMovementConnector.getPDF(arrivalId, token.value).flatMap {
              result =>
                result.status match {
                  case OK =>
                    Future.successful(Ok(result.bodyAsBytes.toArray).withHeaders(headers(result): _*))
                  case _ =>
                    logger.error(s"[PDF][UP] Received downstream status code of ${result.status}")
                    errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
                }
            }
        }
        .getOrElse {
          Future.successful(Redirect(controllers.routes.UnauthorisedController.onPageLoad()))
        }
  }

  private def headers(result: WSResponse): Seq[(String, String)] = {
    def header(key: String): Seq[(String, String)] =
      result.headers
        .get(key)
        .flatMap {
          _.headOption.map((key, _))
        }
        .toSeq

    header(CONTENT_DISPOSITION) ++
      header(CONTENT_TYPE)
  }
}
