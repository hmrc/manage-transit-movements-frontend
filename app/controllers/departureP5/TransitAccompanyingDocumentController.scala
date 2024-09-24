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

package controllers.departureP5

import connectors.ManageDocumentsConnector
import controllers.DocumentController
import controllers.actions.Actions
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TransitAccompanyingDocumentController @Inject() (
  actions: Actions,
  cc: MessagesControllerComponents,
  connector: ManageDocumentsConnector
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with DocumentController
    with I18nSupport {

  def getTAD(departureID: String, messageId: String): Action[AnyContent] = (Action andThen actions.identify()).async {
    implicit request =>
      connector.getTAD(departureID, messageId).map(stream)
  }

}
