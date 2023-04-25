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
import models.departureP5.IE060MessageData
import models.referenceData.CustomsOffice
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsResult, JsSuccess, JsValue, Json, OFormat}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.CustomsOfficeContactViewModel
import viewModels.P5.departure.GoodsUnderControlP5ViewModel.GoodsUnderControlP5ViewModelProvider
import views.html.departure.TestOnly.GoodsUnderControlP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoodsUnderControlP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  goodsUnderControlAction: GoodsUnderControlActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: GoodsUnderControlP5ViewModelProvider,
  view: GoodsUnderControlP5View
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def goodsUnderControlOnPageLoad(departureId: String): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      val customsOffice = parseJson[CustomsOffice]("customsOffice") match {
        case JsSuccess(value, _) => Some(value)
        case _                   => None
      }
      parseJson[IE060MessageData]("ie060") match {
        case JsSuccess(ie060, _) =>
          val goodsUnderControlP5ViewModel = viewModelProvider.apply(ie060)
          val customsOfficeContactViewModel =
            CustomsOfficeContactViewModel(ie060.CustomsOfficeOfDeparture.referenceNumber, customsOffice)
          goodsUnderControlP5ViewModel.map {
            viewModel =>
              Ok(view(viewModel, departureId, customsOfficeContactViewModel))
          }
        case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      }
  }

  def parseJson[T](key: String)(implicit format: OFormat[T], request: Request[_]): JsResult[T] =
    Json.fromJson[T](Json.parse(request.session.data(key)))

  def noRequestedDocuments(departureId: String): Action[AnyContent] = goodsUnderControlOnPageLoad(departureId)

  def requestedDocuments(departureId: String): Action[AnyContent] = goodsUnderControlOnPageLoad(departureId)
}
