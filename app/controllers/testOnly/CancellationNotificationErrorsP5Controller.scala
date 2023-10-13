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

import controllers.actions._
import models.LocalReferenceNumber
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.CancellationNotificationErrorsP5ViewModel.CancellationNotificationErrorsP5ViewModelProvider
import views.html.departure.TestOnly.CancellationNotificationErrorsP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CancellationNotificationErrorsP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  cc: MessagesControllerComponents,
  rejectionMessageAction: DepartureRejectionMessageActionProvider,
  viewModelProvider: CancellationNotificationErrorsP5ViewModelProvider,
  view: CancellationNotificationErrorsP5View,
  referenceDataService: ReferenceDataService
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen rejectionMessageAction(departureId, messageId)).async {
      implicit request =>
        val functionalErrors       = request.ie056MessageData.functionalErrors
        val customsOfficeReference = request.ie056MessageData.customsOfficeOfDeparture.referenceNumber

        if (functionalErrors.isEmpty) {
          referenceDataService.getCustomsOffice(customsOfficeReference).map {
            customsOffice =>
              Ok(view(viewModelProvider.apply(request.lrn.value, customsOfficeReference, customsOffice)))
          }
        } else {
          Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
        }
    }
}
