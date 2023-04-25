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

import config.{FrontendAppConfig, PaginationAppConfig}
import connectors.DepartureMovementP5Connector
import controllers.actions._
import forms.DeparturesSearchFormProvider
import models.requests.IdentifierRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import services.DepartureP5MessageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.{ViewAllDepartureMovementsP5ViewModel, ViewDepartureP5}
import viewModels.pagination.MovementsPaginationViewModel
import views.html.departure.TestOnly.ViewAllDeparturesP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoodsUnderControlIndexController @Inject()(
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  goodsUnderControlAction: GoodsUnderControlActionProvider,
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport {


  def onPageLoad(departureId: String): Action[AnyContent] = (Action andThen identify andThen goodsUnderControlAction(departureId)) {
    implicit request =>
      val notificationType: String = request.ie060MessageData.TransitOperation.notificationType
      if (request.ie060MessageData.requestedDocumentsToSeq.nonEmpty || notificationType == "1") {
        Redirect(controllers.testOnly.routes.GoodsUnderControlP5Controller.noRequestedDocuments(departureId))
      } else {
        Redirect(controllers.testOnly.routes.GoodsUnderControlP5Controller.requestedDocuments(departureId))
      }

  }

}
