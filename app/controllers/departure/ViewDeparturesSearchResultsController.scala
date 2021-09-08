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

package controllers.departure

import config.FrontendAppConfig
import connectors.DeparturesMovementConnector
import controllers.TechnicalDifficultiesPage
import controllers.actions._
import models.{Departure, Departures}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.{ViewDeparture, ViewDepartureMovements}
import javax.inject.Inject
import models.requests.IdentifierRequest

import scala.concurrent.{ExecutionContext, Future}

class ViewDeparturesSearchResultsController @Inject() (
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

  private val pageSize = config.maxSearchResults

  def onPageLoad(lrn: String): Action[AnyContent] = (Action andThen identify).async {
    implicit request: IdentifierRequest[AnyContent] =>
      val trimmedLrn = lrn.trim
      if (trimmedLrn.isEmpty) {
        Future.successful(Redirect(routes.ViewAllDeparturesController.onPageLoad(None)))
      } else {
        renderSearchResults(
          connector.getDepartureSearchResults(trimmedLrn, pageSize),
          "viewDeparturesSearchResults.njk",
          trimmedLrn
        )
      }
  }

  private def searchParams(lrn: String, retrieved: Int, matchedOption: Option[Int]) =
    matchedOption match {
      case Some(matched) if matched > 0 =>
        Json.obj(
          "lrn"            -> lrn,
          "resultsText"    -> s"Showing $retrieved results matching $lrn.",
          "tooManyResults" -> (retrieved < matched)
        )
      case _ => Json.obj("lrn" -> lrn, "resultsText" -> "", "tooManyResults" -> false)

    }

  private def renderSearchResults(results: Future[Option[Departures]], template: String, lrn: String)(implicit request: IdentifierRequest[AnyContent]) =
    results.flatMap {
      case Some(allDepartures) =>
        val viewMovements: Seq[ViewDeparture] = allDepartures.departures.map(
          (departure: Departure) => ViewDeparture(departure)
        )
        val formatToJson: JsObject = Json.toJsObject(ViewDepartureMovements.apply(viewMovements)) ++
          searchParams(lrn, allDepartures.retrievedDepartures, allDepartures.totalMatched)

        renderer
          .render(template, formatToJson)
          .map(Ok(_))

      case _ => renderTechnicalDifficultiesPage
    }
}
