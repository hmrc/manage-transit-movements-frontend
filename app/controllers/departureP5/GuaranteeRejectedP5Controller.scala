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
import controllers.actions.*
import generated.{CC055CType, Generated_CC055CTypeFormat}
import models.departureP5.Rejection
import models.departureP5.Rejection.IE055Rejection
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AmendmentService
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.GuaranteeRejectedP5ViewModel.GuaranteeRejectedP5ViewModelProvider
import views.html.departureP5.GuaranteeRejectedP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GuaranteeRejectedP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  view: GuaranteeRejectedP5View,
  viewModelProvider: GuaranteeRejectedP5ViewModelProvider,
  service: AmendmentService,
  frontendAppConfig: FrontendAppConfig
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC055CType](departureId, messageId)).async {
      implicit request =>
        val lrn = request.referenceNumbers.localReferenceNumber
        for {
          isAmendable <- service.isRejectionAmendable(lrn, IE055Rejection(departureId))
          viewModel <- viewModelProvider.apply(
            request.messageData.GuaranteeReference,
            lrn,
            request.messageData.TransitOperation.MRN,
            request.messageData.TransitOperation.declarationAcceptanceDate
          )
        } yield
          if (isAmendable) {
            Ok(view(viewModel, departureId, messageId))
          } else {
            Redirect(controllers.departureP5.routes.GuaranteeRejectedNotAmendableP5Controller.onPageLoad(departureId, messageId).url)
          }
    }

  def onSubmit(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC055CType](departureId, messageId)).async {
      implicit request =>
        val lrn = request.referenceNumbers.localReferenceNumber
        service.handleErrors(lrn, IE055Rejection(departureId)).map {
          case response if is2xx(response.status) =>
            Redirect(frontendAppConfig.departureFrontendTaskListUrl(lrn))
          case _ =>
            Redirect(controllers.routes.ErrorController.technicalDifficulties())
        }
    }

}
