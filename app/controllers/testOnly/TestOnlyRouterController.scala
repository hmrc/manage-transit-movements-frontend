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

package controllers.testOnly

import connectors.testOnly.TestOnlyRouterConnector
import play.api.mvc.{Action, DefaultActionBuilder, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class TestOnlyRouterController @Inject() (
  cc: MessagesControllerComponents,
  connector: TestOnlyRouterConnector,
  action: DefaultActionBuilder
)(implicit val ec: ExecutionContext)
    extends FrontendController(cc) {

  def fromCoreMessage: Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      connector
        .submitInboundMessage(request.body, request.headers)
        .map(
          response => Status(response.status)
        )
  }

  def arrivalNotificationMessageToCore: Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      connector
        .createArrivalNotificationMessage(request.body, request.headers)
        .map {
          response =>
            val location = response.header("Location").getOrElse("Location is missing")
            Status(response.status)
              .withHeaders(
                "Location"  -> location,
                "arrivalId" -> location.split("/").last
              )
        }
  }

  def resubmitArrivalNotificationMessageToCore: Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      request.headers.get("arrivalId") match {
        case Some(arrivalId) =>
          connector
            .resubmitArrivalNotificationMessage(request.body, arrivalId, request.headers)
            .map {
              response =>
                val location = response.header("Location").getOrElse("Location is missing")
                Status(response.status)
                  .withHeaders(
                    "Location"  -> location,
                    "arrivalId" -> location.split("/").last
                  )
            }

        case _ => Future.successful(BadRequest("ArrivalId is missing"))
      }
  }

  def messageToCore: Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      request.headers.get("arrivalId") match {
        case Some(arrivalId) =>
          connector
            .submitMessageToCore(request.body, arrivalId, request.headers)
            .map(
              response => Status(response.status)
            )

        case _ => Future.successful(BadRequest("ArrivalId is missing"))
      }
  }
}
