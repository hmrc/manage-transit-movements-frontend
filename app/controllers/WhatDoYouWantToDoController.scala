/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.{ArrivalMovementConnector, DeparturesMovementConnector}
import controllers.actions.IdentifierAction
import controllers.departure.{routes => departureRoutes}
import models.{Arrivals, Departures}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, RequestHeader}
import play.twirl.api.Html
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatDoYouWantToDoController @Inject() (appConfig: FrontendAppConfig,
                                             identify: IdentifierAction,
                                             cc: MessagesControllerComponents,
                                             val arrivalMovementConnector: ArrivalMovementConnector,
                                             val departuresMovementConnector: DeparturesMovementConnector,
                                             renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      for {
        arrivals   <- arrivalMovementConnector.getArrivals()
        departures <- departuresMovementConnector.getDepartures()
        html       <- renderPage(arrivals, departures)
      } yield Ok(html)
  }

  private def renderPage(arrivals: Option[Arrivals], departures: Option[Departures])(implicit requestHeader: RequestHeader): Future[Html] =
    renderer
      .render(
        "whatDoYouWantToDo.njk",
        Json.obj(
          "declareArrivalNotificationUrl"  -> appConfig.declareArrivalNotificationStartUrl,
          "viewArrivalNotificationUrl"     -> controllers.arrival.routes.ViewAllArrivalsController.onPageLoad(None).url,
          "arrivalsAvailable"              -> arrivals.nonEmpty,
          "hasArrivals"                    -> arrivals.exists(_.arrivals.nonEmpty),
          "declareDepartureDeclarationUrl" -> appConfig.declareDepartureStartWithLRNUrl,
          "viewDepartureNotificationUrl"   -> departureRoutes.ViewAllDeparturesController.onPageLoad(None).url,
          "departuresAvailable"            -> departures.nonEmpty,
          "hasDepartures"                  -> departures.exists(_.departures.nonEmpty),
          "isGuaranteeBalanceEnabled"      -> appConfig.isGuaranteeBalanceEnabled,
          "checkGuaranteeBalanceUrl"       -> appConfig.checkGuaranteeBalanceUrl
        )
      )
}
