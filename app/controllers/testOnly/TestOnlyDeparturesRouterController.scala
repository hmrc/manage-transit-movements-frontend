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

import connectors.testOnly.TestOnlyDeparturesRouterConnector
import javax.inject.Inject
import play.api.Logger
import play.api.mvc.{Action, DefaultActionBuilder, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext
import scala.xml.NodeSeq

class TestOnlyDeparturesRouterController @Inject()(
  cc: MessagesControllerComponents,
  connector: TestOnlyDeparturesRouterConnector,
  action: DefaultActionBuilder
)(implicit val ec: ExecutionContext)
    extends FrontendController(cc) {

  val Log: Logger = Logger(getClass)

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

  //TODO: Not yet implemented (see TestOnlyRouterController)
  def messageToCore: Action[NodeSeq] = ???
}
