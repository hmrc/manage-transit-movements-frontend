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

import controllers.actions.*
import generated.{CC060CType, Generated_CC060CTypeFormat}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.GoodsUnderControlP5ViewModel.GoodsUnderControlP5ViewModelProvider
import views.html.departureP5.GoodsUnderControlP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GoodsUnderControlP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: GoodsUnderControlP5ViewModelProvider,
  view: GoodsUnderControlP5View,
  referenceDataService: ReferenceDataService
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  private def goodsUnderControlOnPageLoad(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC060CType](departureId, messageId)).async {
      implicit request =>
        val customsOfficeId = request.messageData.CustomsOfficeOfDeparture.referenceNumber

        referenceDataService.getCustomsOffice(customsOfficeId).flatMap {
          customsOffice =>
            val goodsUnderControlP5ViewModel = viewModelProvider.apply(request.messageData, customsOffice)
            goodsUnderControlP5ViewModel.map {
              viewModel =>
                Ok(view(viewModel, departureId))
            }
        }
    }

  def noRequestedDocuments(departureId: String, messageId: String): Action[AnyContent] = goodsUnderControlOnPageLoad(departureId, messageId)

  def requestedDocuments(departureId: String, messageId: String): Action[AnyContent] = goodsUnderControlOnPageLoad(departureId, messageId)
}
