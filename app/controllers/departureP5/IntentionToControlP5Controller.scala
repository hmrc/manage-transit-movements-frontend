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
import generated.{CC060CType, Generated_CC060CTypeFormat}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.IntentionToControlP5ViewModel.IntentionToControlP5ViewModelProvider
import views.html.departureP5.IntentionToControlP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class IntentionToControlP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: IntentionToControlP5ViewModelProvider,
  view: IntentionToControlP5View,
  referenceDataService: ReferenceDataService,
  config: FrontendAppConfig
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC060CType](departureId, messageId)).async {
      implicit request =>
        val customsOfficeId = request.messageData.CustomsOfficeOfDeparture.referenceNumber

        referenceDataService.getCustomsOffice(customsOfficeId).map {
          customsOffice =>
            val intentionToControlP5ViewModel = viewModelProvider.apply(request.messageData, customsOffice)

            Ok(view(intentionToControlP5ViewModel, departureId, messageId))
        }
    }

  def onSubmit(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC060CType](departureId, messageId)) {
      Redirect {
        config.presentationNotificationFrontendUrl(departureId)
      }
    }
}
