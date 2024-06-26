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
import connectors.DepartureCacheConnector
import controllers.actions._
import generated.CC055CType
import models.LocalReferenceNumber
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.GuaranteeRejectedP5ViewModel
import views.html.departureP5.GuaranteeRejectedP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GuaranteeRejectedP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  view: GuaranteeRejectedP5View,
  departureCacheConnector: DepartureCacheConnector,
  frontendAppConfig: FrontendAppConfig
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String, messageId: String, lrn: LocalReferenceNumber): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen messageRetrievalAction[CC055CType](departureId, messageId)).async {
      implicit request =>
        departureCacheConnector.doesDeclarationExist(lrn.value).map {
          isAmendable =>
            val viewModel: GuaranteeRejectedP5ViewModel = GuaranteeRejectedP5ViewModel(
              request.messageData.GuaranteeReference,
              lrn,
              isAmendable,
              request.messageData.TransitOperation.MRN,
              request.messageData.TransitOperation.declarationAcceptanceDate
            )

            Ok(view(viewModel, departureId, messageId))
        }
    }

  def onAmend(departureId: String, messageId: String, lrn: LocalReferenceNumber): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen messageRetrievalAction[CC055CType](departureId, messageId)).async {
      implicit request =>
        departureCacheConnector.handleGuaranteeRejection(lrn.value).map {
          case true  => Redirect(frontendAppConfig.departureGuaranteeAmendmentUrl(lrn.value, departureId))
          case false => Redirect(controllers.routes.ErrorController.technicalDifficulties())
        }
    }
}
