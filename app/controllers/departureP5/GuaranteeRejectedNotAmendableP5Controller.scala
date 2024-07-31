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

import controllers.actions._
import generated.CC055CType
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.GuaranteeRejectedNotAmendableP5ViewModel.GuaranteeRejectedNotAmendableP5ViewModelProvider
import views.html.departureP5.GuaranteeRejectedNotAmendableP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GuaranteeRejectedNotAmendableP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  view: GuaranteeRejectedNotAmendableP5View,
  viewModelProvider: GuaranteeRejectedNotAmendableP5ViewModelProvider
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen messageRetrievalAction[CC055CType](departureId, messageId)).async {
      implicit request =>
        val lrn = request.referenceNumbers.localReferenceNumber

        for {
          viewModel <- viewModelProvider.apply(
            request.messageData.GuaranteeReference,
            lrn,
            request.messageData.TransitOperation.MRN,
            request.messageData.TransitOperation.declarationAcceptanceDate
          )
        } yield Ok(view(viewModel, departureId, messageId))
    }

}
