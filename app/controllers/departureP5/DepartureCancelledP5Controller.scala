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
import generated.CC009CType
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.DepartureCancelledP5ViewModel.DepartureCancelledP5ViewModelProvider
import views.html.departureP5.DepartureCancelledP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import generated.Generated_CC009CTypeFormat

class DepartureCancelledP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: DepartureCancelledP5ViewModelProvider,
  view: DepartureCancelledP5View,
  referenceDataService: ReferenceDataService
)(implicit val executionContext: ExecutionContext, frontendAppConfig: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC009CType](departureId, messageId)).async {
      implicit request =>
        buildView(request.messageData, request.referenceNumbers.localReferenceNumber)
    }

  private def buildView(
    ie009: CC009CType,
    lrn: String
  )(implicit request: Request[?]): Future[Result] = {
    val customsOfficeReferenceNumber = ie009.CustomsOfficeOfDeparture.referenceNumber

    referenceDataService.getCustomsOffice(customsOfficeReferenceNumber).flatMap {
      customsOffice =>
        viewModelProvider.apply(ie009, lrn, customsOffice).map {
          viewModel => Ok(view(viewModel))
        }
    }
  }

}
