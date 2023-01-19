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

import connectors.{ArrivalMovementConnector, ArrivalMovementP5Connector, DeparturesMovementConnector}
import controllers.actions.IdentifierAction
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ArrivalP5MessageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.WhatDoYouWantToDoView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class WhatDoYouWantToDoController @Inject() (
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  val arrivalMovementConnector: ArrivalMovementConnector,
  val departuresMovementConnector: DeparturesMovementConnector,
  val arrivalMovementsP5Connector: ArrivalMovementP5Connector,
  arrivalP5MessageService: ArrivalP5MessageService,
  view: WhatDoYouWantToDoView
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      for {
        getMovements                   <- arrivalMovementsP5Connector.getAllMovements()
        getArrivalMovementsAndMessages <- arrivalP5MessageService.getMessagesForAllMovements(getMovements.get) // TODO Will need to hand this on view all layer
        arrivalsAvailability           <- arrivalMovementConnector.getArrivalsAvailability()
        departuresAvailability         <- departuresMovementConnector.getDeparturesAvailability()
      } yield {

        println(s"\n\n\n $getArrivalMovementsAndMessages \n\n\n")

        Ok(
          view(arrivalsAvailability, departuresAvailability)
        )
      }
  }
}
