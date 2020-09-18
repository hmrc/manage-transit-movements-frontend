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
import connectors.{ArrivalMovementConnector, DeparturesMovementConnector}
import controllers.actions.IdentifierAction
import javax.inject.Inject
import models.{Arrivals, Departures}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, RequestHeader}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(appConfig: FrontendAppConfig,
                                identify: IdentifierAction,
                                val controllerComponents: MessagesControllerComponents,
                                val arrivalMovementConnector: ArrivalMovementConnector,
                                val departuresMovementConnector: DeparturesMovementConnector,
                                renderer: Renderer)(implicit ec: ExecutionContext)
    extends FrontendBaseController
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
    val hasDepartures = departures match {
      case Some(departures) if departures.departures.nonEmpty => true
      case _                                                  => false
    }

    val hasArrivals = arrivals match {
      case Some(arrivals) if arrivals.arrivals.nonEmpty => true
      case _                                            => false
    }

    renderer
      .render(
        "index.njk",
        Json.obj(
          "declareArrivalNotificationUrl"  -> appConfig.declareArrivalNotificationStartUrl,
          "viewArrivalNotificationUrl"     -> routes.ViewArrivalsController.onPageLoad().url,
          "arrivalsAvailable"              -> arrivals.nonEmpty,
          "hasArrivals"                    -> hasArrivals,
          "showDeparture"                  -> appConfig.departureJourneyToggle,
          "declareDepartureDeclarationUrl" -> appConfig.declareDepartureStartWithLRNUrl,
          "viewDepartureNotificationUrl"   -> routes.ViewDeparturesController.onPageLoad().url,
          "departuresAvailable"            -> departures.nonEmpty,
          "hasDepartures"                  -> hasDepartures
        )
      )
  }
}
