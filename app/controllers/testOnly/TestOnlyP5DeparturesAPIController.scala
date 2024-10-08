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

import connectors.testOnly.TestOnlyP5DeparturesAPIConnector
import play.api.mvc.{Action, AnyContent, DefaultActionBuilder, MessagesControllerComponents}
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.xml.NodeSeq

class TestOnlyP5DeparturesAPIController @Inject() (
  cc: MessagesControllerComponents,
  connector: TestOnlyP5DeparturesAPIConnector,
  action: DefaultActionBuilder
)(implicit val ec: ExecutionContext)
    extends FrontendController(cc) {

  def outboundDepartureMessage: Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      connector
        .departureOutbound(request.body, request.headers)
        .map {
          case response if is2xx(response.status) => Accepted(response.body)
          case response =>
            BadRequest(s"[outboundDepartureMessage] Failed to post outbound departure message with error: ${response.status} - ${response.body}")
        }

  }

  def inboundDepartureMessage(departureId: String): Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      connector
        .departureInbound(request.body, departureId, request.headers)
        .map {
          case response if is2xx(response.status) => Accepted(response.body)
          case response =>
            BadRequest(s"[inboundDepartureMessage] Failed to post inbound departure message with error: ${response.status} - ${response.body}")
        }
  }

  def addMessageToDeparture(departureId: String): Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      connector
        .departureAddMessage(request.body, departureId, request.headers)
        .map {
          case response if is2xx(response.status) => Accepted(response.body)
          case response =>
            BadRequest(s"[addMessageToDeparture] Failed to add message to departure with error: ${response.status} - ${response.body}")
        }
  }

  def getMessage(departureId: String, messageId: String): Action[AnyContent] = Action.async {
    implicit request =>
      connector
        .getMessage(departureId, messageId, request.headers)
        .map(Ok(_))
  }

}
