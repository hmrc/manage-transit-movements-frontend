/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.actions.{Actions, DepartureMessageRetrievalActionProvider}
import generated.CC182CType
import models.Index
import models.RichCC182Type
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.IncidentP5ViewModel.IncidentP5ViewModelProvider
import views.html.departureP5.IncidentP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class IncidentP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: IncidentP5ViewModelProvider,
  referenceDataService: ReferenceDataService,
  view: IncidentP5View
)(implicit val executionContext: ExecutionContext, frontendAppConfig: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String, incidentIndex: Index, messageId: String): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen messageRetrievalAction[CC182CType](departureId, messageId)).async {
      implicit request =>
        val customsOfficeId = request.messageData.CustomsOfficeOfDeparture.referenceNumber

        referenceDataService.getCustomsOffice(customsOfficeId).map {
          customsOffice =>
            val incidentP5ViewModel =
              viewModelProvider.apply(request.messageData, request.referenceNumbers, customsOffice, request.messageData.hasMultipleIncidents, incidentIndex)

            Ok(view(incidentP5ViewModel, departureId, messageId))
        }
    }

}
