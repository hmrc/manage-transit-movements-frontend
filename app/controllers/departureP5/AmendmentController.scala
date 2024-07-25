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

import config.FrontendAppConfig
import controllers.actions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AmendmentService
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AmendmentController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  departureRetrievalAction: DepartureRetrievalActionProvider,
  cc: MessagesControllerComponents,
  service: AmendmentService,
  frontendAppConfig: FrontendAppConfig
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def prepareForAmendment(departureId: String): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen departureRetrievalAction(departureId)).async {
      implicit request =>
        val lrn = request.referenceNumbers.localReferenceNumber
        service.prepareForAmendment(lrn, departureId).map {
          case response if is2xx(response.status) =>
            Redirect(frontendAppConfig.departureFrontendTaskListUrl(lrn))
          case _ =>
            Redirect(controllers.routes.ErrorController.technicalDifficulties())
        }
    }
}
