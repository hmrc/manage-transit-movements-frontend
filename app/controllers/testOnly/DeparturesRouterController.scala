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

import connectors.testOnly.DeparturesRouterConnector
import play.api.libs.json.JsValue
import play.api.mvc.{Action, DefaultActionBuilder, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class DeparturesRouterController @Inject() (
  cc: MessagesControllerComponents,
  connector: DeparturesRouterConnector,
  action: DefaultActionBuilder
)(implicit val ec: ExecutionContext)
    extends FrontendController(cc) {

  def declarationMessageToCore: Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      connector
        .createDeclarationMessage(request.body, request.headers)
        .map {
          response =>
            val location = response.header("Location").getOrElse("Location is missing")
            Status(response.status)
              .withHeaders(
                "Location"    -> location,
                "departureId" -> location.split("/").last
              )
        }
  }

  def declarationCancellationMessageToCore: Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      request.headers.get("departureId") match {
        case Some(departureId) =>
          connector
            .createDeclarationCancellationMessage(request.body, departureId, request.headers)
            .map {
              response =>
                val location = response.header("Location").getOrElse("Location is missing")
                Status(response.status)
                  .withHeaders(
                    "Location"    -> location,
                    "departureId" -> location.split("/").last
                  )
            }

        case _ => Future.successful(BadRequest("DepartureId is missing"))

      }
  }

  def messageToCore: Action[JsValue] = action.async(parse.json) {
    implicit request =>
      request.headers.get("departureId") match {
        case Some(departureId) =>
          connector
            .submitMessageToCore(request.body, departureId, request.headers)
            .map(
              response => Status(response.status)
            )

        case _ => Future.successful(BadRequest("DepartureId is missing"))
      }
  }
}
