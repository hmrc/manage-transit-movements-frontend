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
import javax.inject.Inject
import models.{Arrivals, Departures}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, RequestHeader}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import controllers.testOnly.{routes => testRoutes}

import scala.concurrent.ExecutionContext

class IndexController @Inject()(appConfig: FrontendAppConfig,
                                identify: IdentifierAction,
                                cc: MessagesControllerComponents,
                                val arrivalMovementConnector: ArrivalMovementConnector,
                                val departuresMovementConnector: DeparturesMovementConnector,
                                renderer: Renderer)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = identify.async {
    implicit request =>
      for {
        arrivals   <- arrivalMovementConnector.getArrivals()
        departures <- departuresMovementConnector.getDepartures()
        html       <- renderPage(arrivals, departures)
      } yield {
        Ok(html)
      }
  }

  private def renderPage(arrivals: Option[Arrivals], departures: Option[Departures])(implicit requestHeader: RequestHeader) = {
    println("-------------------------------" + arrivals)

    renderer
      .render(
        "index.njk",
        Json.obj(
          "declareArrivalNotificationUrl"  -> appConfig.declareArrivalNotificationStartUrl,
          "viewArrivalNotificationUrl"     -> routes.ViewArrivalsController.onPageLoad().url,
          "arrivalsAvailable"              -> arrivals.nonEmpty,
          "hasArrivals"                    -> arrivals.exists(_.arrivals.nonEmpty),
          "showDeparture"                  -> appConfig.departureJourneyToggle,
          "declareDepartureDeclarationUrl" -> appConfig.declareDepartureStartWithLRNUrl,
          "viewDepartureNotificationUrl"   -> testRoutes.ViewDeparturesController.onPageLoad().url,
          "departuresAvailable"            -> departures.nonEmpty,
          "hasDepartures"                  -> departures.exists(_.departures.nonEmpty)
        )
      )

  }
}
