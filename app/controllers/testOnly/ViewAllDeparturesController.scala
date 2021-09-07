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

package controllers.testOnly

import config.{FrontendAppConfig, PaginationAppConfig}
import connectors.DeparturesMovementConnector
import controllers.TechnicalDifficultiesPage
import controllers.actions._
import javax.inject.Inject
import models.Departure
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.pagination.PaginationViewModel
import viewModels.{ViewAllDepartureMovementsViewModel, ViewDeparture}

import scala.concurrent.ExecutionContext

class ViewAllDeparturesController @Inject() (val renderer: Renderer,
                                             identify: IdentifierAction,
                                             cc: MessagesControllerComponents,
                                             val config: FrontendAppConfig,
                                             val paginationAppConfig: PaginationAppConfig,
                                             departuresMovementConnector: DeparturesMovementConnector
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport
    with TechnicalDifficultiesPage {

  def onPageLoad(page: Option[Int]): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      val currentPage = page.getOrElse(1)

      departuresMovementConnector.getPagedDepartures(currentPage, paginationAppConfig.departuresNumberOfMovements).flatMap {
        case Some(filteredDepartures) =>
          val viewMovements: Seq[ViewDeparture] = filteredDepartures.departures.map(
            (departure: Departure) => ViewDeparture(departure)
          )

          val paginationViewModel = PaginationViewModel.apply(
            filteredDepartures.totalDepartures,
            currentPage,
            paginationAppConfig.departuresNumberOfMovements,
            routes.ViewAllDeparturesController.onPageLoad(None).url
          )

          val formatToJson: JsObject = Json.toJsObject(ViewAllDepartureMovementsViewModel(viewMovements, paginationViewModel))

          renderer
            .render("viewAllDepartures.njk", formatToJson)
            .map(Ok(_))

        case None => renderTechnicalDifficultiesPage
      }
  }
}
