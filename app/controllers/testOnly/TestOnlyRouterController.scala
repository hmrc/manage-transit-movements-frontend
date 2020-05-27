/*
 * Copyright 2020 HM Revenue & Customs
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
import javax.inject.Inject
import play.api.Logger
import play.api.mvc.{Action, ControllerComponents, DefaultActionBuilder}
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class TestOnlyRouterController @Inject()(
  cc: ControllerComponents,
  connector: TestOnlyRouterConnector,
  action: DefaultActionBuilder
)(implicit val ec: ExecutionContext)
    extends BackendController(cc) {

  val Log: Logger = Logger(getClass)

  def fromCoreMessage: Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      Log.debug(s"From Core Request Body (Controller): ${request.body}")
      Log.debug(s"From Core Request Headers (Controller): ${request.headers}")
      connector
        .submitInboundMessage(request.body, request.headers)
        .map(
          response => {
            Log.debug(s"Got this JSON: ${response.json}")
            Log.debug(s"Got this body: ${response.body}")
            Status(response.status)
          }
        )
  }

  def arrivalNotificationMessageToCore: Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      Log.debug(s"Arrival Notification To Core Request Body (Controller): ${request.body}")
      Log.debug(s"Arrival Notification To Core Request Headers (Controller): ${request.headers}")
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

  def messageToCore: Action[NodeSeq] = action.async(parse.xml) {
    implicit request =>
      Log.debug(s"To Core Request Body (Controller): ${request.body}")
      Log.debug(s"To Core Request Headers (Controller): ${request.headers}")

      request.headers.get("arrivalId") match {
        case Some(arrivalId) =>
          connector
            .submitMessageToCore(request.body, arrivalId, request.headers)
            .map(response => Status(response.status))

        case _ => Future.successful(BadRequest("ArrivalId is missing"))
      }
  }
}
