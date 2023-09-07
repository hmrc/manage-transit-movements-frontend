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
import models.LocalReferenceNumber
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.GuaranteeRejectedP5ViewModel
import views.html.departure.TestOnly.GuaranteeRejectedP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GuaranteeRejectedP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  guaranteeRejectedAction: GuaranteeRejectedActionProvider,
  cc: MessagesControllerComponents,
  view: GuaranteeRejectedP5View,
  departureCacheConnector: DepartureCacheConnector,
  frontendAppConfig: FrontendAppConfig
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String, messageId: String, lrn: LocalReferenceNumber): Action[AnyContent] =
    (Action andThen identify andThen guaranteeRejectedAction(departureId, messageId)).async {
      implicit request =>
        departureCacheConnector.doesDeclarationExist(lrn.value).map {
          isAmendable =>
            val viewModel: GuaranteeRejectedP5ViewModel = GuaranteeRejectedP5ViewModel(request.ie055MessageData.guaranteeReferences, lrn, isAmendable)

            Ok(view(viewModel))
        }
    }

  def onAmend(lrn: LocalReferenceNumber): Action[AnyContent] =
    (Action andThen identify).async {
      implicit request =>
        departureCacheConnector.handleGuaranteeRejection(lrn.value).map {
          case true  => Redirect(frontendAppConfig.departureNewLocalReferenceNumberUrl(lrn.value))
          case false => Redirect(controllers.routes.ErrorController.technicalDifficulties())
        }
    }
}
