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

package controllers.arrival

import config.FrontendAppConfig
import connectors.ArrivalMovementConnector
import controllers.TechnicalDifficultiesPage
import controllers.actions.IdentifierAction
import models.{Arrival, Arrivals}
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.{ViewArrival, ViewArrivalMovements}

import javax.inject.Inject
import models.requests.IdentifierRequest

import scala.concurrent.{ExecutionContext, Future}

class ViewArrivalsController @Inject() (val renderer: Renderer,
                                        identify: IdentifierAction,
                                        cc: MessagesControllerComponents,
                                        val config: FrontendAppConfig,
                                        arrivalMovementConnector: ArrivalMovementConnector
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport
    with TechnicalDifficultiesPage {

  private val pageSize = config.maxSearchResults

  def onPageLoad(): Action[AnyContent] = (Action andThen identify).async {
    implicit request: IdentifierRequest[AnyContent] =>
      renderResults(arrivalMovementConnector.getArrivals(), "viewArrivals.njk")
  }

  def onPageLoadSearch(mrn: String): Action[AnyContent] = (Action andThen identify).async {
    implicit request: IdentifierRequest[AnyContent] =>
      val trimmedMrn = mrn.trim
      if (trimmedMrn.isEmpty) {
        Future.successful(Redirect(routes.ViewArrivalsController.onPageLoad()))
      } else {
        renderSearchResults(
          arrivalMovementConnector.getArrivalSearchResults(trimmedMrn, pageSize),
          "viewArrivalsSearchResults.njk",
          trimmedMrn
        )
      }
  }

  private def searchParams(mrn: String, retrieved: Int, matchedOption: Option[Int]) =
    matchedOption match {
      case Some(matched) if matched > 0 =>
        Json.obj(
          "mrn"            -> mrn,
          "resultsText"    -> s"Showing $retrieved results matching $mrn.",
          "tooManyResults" -> (retrieved < matched)
        )
      case _ => Json.obj("mrn" -> mrn, "resultsText" -> "", "tooManyResults" -> false)

    }

  private def renderSearchResults(results: Future[Option[Arrivals]], template: String, mrn: String)(implicit request: IdentifierRequest[AnyContent]) =
    results.flatMap {
      case Some(allArrivals) =>
        val viewMovements: Seq[ViewArrival] = allArrivals.arrivals.map(
          (arrival: Arrival) => ViewArrival(arrival)
        )
        val formatToJson: JsObject = Json.toJsObject(ViewArrivalMovements.apply(viewMovements)) ++
          searchParams(mrn, allArrivals.retrievedArrivals, allArrivals.totalMatched)

        renderer
          .render(template, formatToJson)
          .map(Ok(_))

      case _ => renderTechnicalDifficultiesPage
    }

  private def renderResults(results: Future[Option[Arrivals]], template: String)(implicit request: IdentifierRequest[AnyContent]) =
    results.flatMap {
      case Some(allArrivals) =>
        val viewMovements: Seq[ViewArrival] = allArrivals.arrivals.map(
          (arrival: Arrival) => ViewArrival(arrival)
        )
        val formatToJson: JsObject = Json.toJsObject(ViewArrivalMovements.apply(viewMovements))

        renderer
          .render(template, formatToJson)
          .map(Ok(_))

      case _ => renderTechnicalDifficultiesPage
    }
}
