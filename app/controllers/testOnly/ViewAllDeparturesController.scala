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

import config.FrontendAppConfig
import connectors.DeparturesMovementConnector
import controllers.TechnicalDifficultiesPage
import controllers.actions._
import javax.inject.Inject
import models.Departure
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.{PaginationViewModel, ViewDeparture, ViewDepartureMovements}

import scala.concurrent.ExecutionContext

class ViewAllDeparturesController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  connector: DeparturesMovementConnector,
  val config: FrontendAppConfig,
  val renderer: Renderer
)(implicit ec: ExecutionContext, frontendAppConfig: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport
    with TechnicalDifficultiesPage {

  def onPageLoad(page: Option[Int]): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      val currentPage       = page.getOrElse(1)
      val numberOfMovements = 50

      connector.getPagedDepartures(page.getOrElse(1).toString, numberOfMovements.toString).flatMap {
        case Some(filteredDepartures) =>

          val viewMovements: Seq[ViewDeparture] = filteredDepartures.departures.map(
            (departure: Departure) => ViewDeparture(departure)
          )

          val paginationViewModel = PaginationViewModel.apply(filteredDepartures.totalDepartures, currentPage, numberOfMovements)

          val formatToJson: JsObject = Json.toJsObject(ViewDepartureMovements.apply(viewMovements))

          val mergeMyStuff = formatToJson.deepMerge(paginationViewModel)

          renderer
            .render("viewAllDepartures.njk", mergeMyStuff)
            .map(Ok(_))

        case None => renderTechnicalDifficultiesPage
      }
  }
}
