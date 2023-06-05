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

import connectors.ManageDocumentsConnector
import controllers.actions.IdentifierAction
import play.api.i18n.I18nSupport
import play.api.libs.ws.WSResponse
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TransitAccompanyingDocumentController @Inject() (
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  connector: ManageDocumentsConnector
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def getTAD(): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      val testMessageId   = "6479b7811089a5eb"
      val testDepartureId = "6479b7813a79be10"

      connector.getTAD(testDepartureId, testMessageId).map {
        result =>
          result.status match {
            case OK =>
              Ok(result.body).withHeaders(headers(result): _*)
            case _ =>
              InternalServerError(s"${result.body}")
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

    header(CONTENT_DISPOSITION) ++ header(CONTENT_TYPE)
  }

}
