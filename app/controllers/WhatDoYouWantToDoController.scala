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

package controllers

import config.FrontendAppConfig
import connectors.{ArrivalMovementConnector, ArrivalMovementP5Connector, DeparturesMovementConnector, DeparturesMovementsP5Connector}
import controllers.actions.IdentifierAction
import models.Availability
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.WhatDoYouWantToDoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatDoYouWantToDoController @Inject() (
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  val arrivalMovementConnector: ArrivalMovementConnector,
  val departuresMovementConnector: DeparturesMovementConnector,
  val departuresMovementsP5Connector: DeparturesMovementsP5Connector,
  val arrivalMovementsP5Connector: ArrivalMovementP5Connector,
  view: WhatDoYouWantToDoView,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      for {
        arrivalsAvailability <-
          if (appConfig.phase5ArrivalEnabled) {
            arrivalMovementsP5Connector.getAllMovements().map(Availability(_)) //TODO update when we have API params
          } else {
            arrivalMovementConnector.getArrivalsAvailability()
          }
        departuresAvailability <- departuresMovementConnector.getDeparturesAvailability()
        draftDeparturesAvailability <-
          if (appConfig.phase5DepartureEnabled) {
            departuresMovementsP5Connector.getDraftDeparturesAvailability().map(Some(_))
          } else {
            Future.successful(None)
          }
        viewAllArrivalUrl =
          if (appConfig.phase5ArrivalEnabled) {
            controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None).url
          } else {
            controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url
          }
      } yield Ok(
        view(arrivalsAvailability, departuresAvailability, draftDeparturesAvailability, viewAllArrivalUrl)
      )
  }
}
