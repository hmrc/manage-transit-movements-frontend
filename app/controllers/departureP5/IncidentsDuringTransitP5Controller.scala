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
import generated.CC182CType
import models.RichCC182Type
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.IncidentsDuringTransitP5ViewModel.IncidentsDuringTransitP5ViewModelProvider
import views.html.departureP5.IncidentsDuringTransitP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IncidentsDuringTransitP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: IncidentsDuringTransitP5ViewModelProvider,
  referenceDataService: ReferenceDataService,
  view: IncidentsDuringTransitP5View
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen messageRetrievalAction[CC182CType](departureId, messageId)).async {
      implicit request =>
        val customsOfficeReference = request.messageData.CustomsOfficeOfDeparture.referenceNumber

        referenceDataService.getCustomsOffice(customsOfficeReference).flatMap {
          customsOffice =>
            val viewModel = viewModelProvider.apply(
              request.messageData,
              request.referenceNumbers,
              customsOffice,
              request.messageData.hasMultipleIncidents
            )

            viewModel.map {
              viewModel =>
                Ok(view(viewModel))
            }
        }
    }
}
