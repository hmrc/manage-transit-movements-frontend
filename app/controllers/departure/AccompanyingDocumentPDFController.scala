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

import connectors.DeparturesMovementConnector
import controllers.actions.IdentifierAction
import handlers.ErrorHandler

import javax.inject.Inject
import models.DepartureId
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSResponse
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class AccompanyingDocumentPDFController @Inject() (
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  departuresMovementConnector: DeparturesMovementConnector,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport
    with Logging {

  def getPDF(departureId: DepartureId): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      departuresMovementConnector.getPDF(departureId).flatMap {
        result =>
          result.status match {
            case OK =>
              Future.successful(Ok(result.bodyAsBytes.toArray).withHeaders(headers(result): _*))
            case _ =>
              logger.error(s"[PDF][AD] Received downstream status code of ${result.status}")
              errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
          }
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
