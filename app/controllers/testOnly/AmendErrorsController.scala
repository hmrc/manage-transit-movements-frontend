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

import config.FrontendAppConfig
import connectors.DepartureCacheConnector
import controllers.actions._
import models.FunctionalError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.RejectionMessageP5ViewModel.RejectionMessageP5ViewModelProvider
import views.html.departure.TestOnly.RejectionMessageP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendErrorsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  cacheConnector: DepartureCacheConnector
)(implicit val executionContext: ExecutionContext, config: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  // lrn: String, xPaths: Seq[String]
  def onAmend(): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      val lrn = "12345"
      val xPaths = Seq(
        "/CC015C/Authorisation/errorPath",
        "/CC015C/Guarantee/errorPath",
        "/CC015C/CustomsOfficeOfExitForTransitDeclared/errorPath",
        "/CC015C/Consignment/errorPath",
        "/CC015C/TransitOperation/errorPath"
      )
      if (xPaths.nonEmpty) {
        cacheConnector.handleErrors(lrn, xPaths).map {
          case true =>
            Redirect(controllers.routes.SessionExpiredController.onPageLoad())
          // Redirect to departures frontend on success}
          case false =>
            Redirect(controllers.routes.SessionExpiredController.onPageLoad())
        }
      } else {
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad())) //TODO: If no errors present redirect someone better
      }

  }
}
