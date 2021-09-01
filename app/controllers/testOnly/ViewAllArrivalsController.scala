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
import connectors.ArrivalMovementConnector
import controllers.TechnicalDifficultiesPage
import controllers.actions._
import models.Arrival
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.{ViewArrivalMovements, ViewMovement}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ViewAllArrivalsController @Inject() (val renderer: Renderer,
                                           identify: IdentifierAction,
                                           cc: MessagesControllerComponents,
                                           val config: FrontendAppConfig,
                                           arrivalMovementConnector: ArrivalMovementConnector
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
    extends FrontendController(cc)
    with I18nSupport
    with TechnicalDifficultiesPage {

  def onPageLoad(page: Option[Int]): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>

      val currentPage       = page.getOrElse(1)
      val numberOfMovements = 10

      arrivalMovementConnector.getPagedArrivals(page.getOrElse(1).toString, numberOfMovements.toString).flatMap {
        case Some(filteredArrivals) =>

          val numberOfPagesFloat = filteredArrivals.totalArrivals.toFloat / numberOfMovements

          val numberOfPagesInt   = if (numberOfPagesFloat.isWhole) {
            numberOfPagesFloat.toInt
          } else {
            numberOfPagesFloat.toInt + 1
          }

          // TODO what if greater than????
          val from = if (currentPage == 1) currentPage else currentPage * numberOfMovements
          val to   = if (currentPage == 1) currentPage * numberOfMovements else (currentPage + 1) * numberOfMovements

          val items: JsArray = (1 to numberOfPagesInt).foldLeft(JsArray.empty) {
            (jsonArr, pageNumber) =>

              val item =    Json.obj(
                "text"     -> pageNumber,
                "href"     -> s"${routes.ViewAllArrivalsController.onPageLoad(Some(pageNumber)).url}" ,
                "selected" -> Json.toJson(pageNumber == currentPage)
              )
                
            jsonArr :+ item
          }

          val sampleJson = Json.obj(
            "results" -> Json.obj(
              "from"  -> from,
              "to"    -> to,
              "count" -> filteredArrivals.totalArrivals
            ),
            "previous" -> Json.obj(
              "text" -> "Previous",
              "next" -> ""
            ),
            "next" -> Json.obj(
              "text" -> "Next",
              "href" -> ""
            ),
            "items" -> items
          )

          val viewMovements: Seq[ViewMovement] = filteredArrivals.arrivals.map(
            (arrival: Arrival) => ViewMovement(arrival)
          )

          val formatToJson: JsObject = Json.toJsObject(ViewArrivalMovements.apply(viewMovements))

          val mergeMyStuff = formatToJson.deepMerge(sampleJson)

          renderer
            .render("viewAllArrivals.njk", mergeMyStuff)
            .map(Ok(_))

        case _ => renderTechnicalDifficultiesPage
      }
  }
}
