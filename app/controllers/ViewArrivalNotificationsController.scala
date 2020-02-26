/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import config.FrontendAppConfig
import connectors.{DestinationConnector, ReferenceDataConnector}
import javax.inject.Inject
import models.Movement
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import viewModels.{ViewArrivalMovements, ViewMovement}

import scala.concurrent.{ExecutionContext, Future}

class ViewArrivalNotificationsController @Inject()(
  renderer: Renderer,
  val controllerComponents: MessagesControllerComponents,
  destinationConnector: DestinationConnector,
  referenceDataConnector: ReferenceDataConnector
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = Action.async { implicit request =>
    destinationConnector.getArrivalMovements().flatMap {
      movements =>
        Future.sequence(movements.map(convertToViewMovements)).map(ViewArrivalMovements.apply)
          .map(Json.toJsObject[ViewArrivalMovements])
          .flatMap(renderer.render("viewArrivalNotifications.njk", _).map(Ok(_)))
    }
  }

  private def convertToViewMovements(movement: Movement)(implicit hc: HeaderCarrier): Future[ViewMovement] = {
    referenceDataConnector.getCustomsOffice(movement.presentationOfficeId) map {
      presentationOffice =>
        ViewMovement(movement.date,
          movement.time,
          movement.movementReferenceNumber,
          movement.traderName,
          movement.presentationOfficeId,
          presentationOffice.name,
          movement.procedure
        )
    }
  }
}
