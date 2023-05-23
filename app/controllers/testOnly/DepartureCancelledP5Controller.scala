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
import controllers.actions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.DepartureCancelledP5ViewModel.DepartureCancelledP5ViewModelProvider
import views.html.departure.TestOnly.DepartureCancelledP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DepartureCancelledP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  departureCancelledActionProvider: DepartureCancelledActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: DepartureCancelledP5ViewModelProvider,
  view: DepartureCancelledP5View,
  referenceDataService: ReferenceDataService
)(implicit val executionContext: ExecutionContext, frontendAppConfig: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String): Action[AnyContent] = (Action andThen identify andThen departureCancelledActionProvider(departureId)).async {
    implicit request =>
      val customsOfficeReferenceNumber = request.ie009MessageData.customsOfficeOfDeparture.referenceNumber
      referenceDataService.getCustomsOfficeByCode(customsOfficeReferenceNumber).flatMap {
        customsOffice =>
          viewModelProvider.apply(request.ie009MessageData, request.lrn, customsOfficeReferenceNumber, customsOffice).map {
            viewModel =>
              Ok(view(departureId, viewModel))
          }
      }
  }
}
