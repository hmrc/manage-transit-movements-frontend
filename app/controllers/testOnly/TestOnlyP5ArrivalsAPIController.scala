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

import connectors.testOnly.TestOnlyP5ArrivalsAPIConnector
import play.api.mvc.{Action, DefaultActionBuilder, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.xml.NodeSeq

class TestOnlyP5ArrivalsAPIController @Inject() (
  cc: MessagesControllerComponents,
  connector: TestOnlyP5ArrivalsAPIConnector,
  action: DefaultActionBuilder
)(implicit val ec: ExecutionContext)
    extends FrontendController(cc) {

  def outboundArrivalMessage: Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      connector
        .arrivalOutbound(request.body, request.headers)
        .map(
          x => Accepted(x.body)
        )
  }

  def outboundUnloadingMessage(arrivalId: String): Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      connector
        .unloadingOutbound(request.body, arrivalId, request.headers)
        .map(
          x => Accepted(x.body)
        )
  }

  def inboundArrivalMessage(arrivalId: String): Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      connector
        .arrivalInbound(request.body, arrivalId)
        .map(
          x => Accepted(x.body)
        )
  }
}
