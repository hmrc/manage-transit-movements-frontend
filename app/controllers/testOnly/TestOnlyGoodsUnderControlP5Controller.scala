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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.CustomsOfficeContactViewModel
import viewModels.P5.departure.GoodsUnderControlP5ViewModel.GoodsUnderControlP5ViewModelProvider
import views.html.departure.P5.TestOnlyGoodsUnderControlP5View

import javax.inject.Inject

class TestOnlyGoodsUnderControlP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  goodsUnderControlAction: GoodsUnderControlActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: GoodsUnderControlP5ViewModelProvider,
  view: TestOnlyGoodsUnderControlP5View
) extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(departureId: String): Action[AnyContent] = (Action andThen identify andThen goodsUnderControlAction(departureId)) {
    implicit request =>
      val goodsUnderControlP5ViewModel = viewModelProvider.apply(request.ie060MessageData)
      val customsOfficeContactViewModel =
        CustomsOfficeContactViewModel(request.ie060MessageData.CustomsOfficeOfDeparture.referenceNumber, request.customsOffice)
      Ok(view(goodsUnderControlP5ViewModel, departureId, customsOfficeContactViewModel))
  }
}
