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
import play.api.libs.functional.syntax._
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

  def onPageLoad(page: Option[String]): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>

      arrivalMovementConnector.getPagedArrivals(page.getOrElse("1"), "50").flatMap {
        case Some(filteredArrivals) =>
          val sampleJson = Json.obj(
            "results" -> Json.obj(
              "from" -> 10,
              "to" -> 20,
              "count" -> 30
            ),
            "previous" -> Json.obj(
              "text" -> "Previous",
              "next" -> ""
            ),
            "next" -> Json.obj(
              "text" -> "Next",
              "href" -> ""
            ),
            "items" -> Json.arr(
              Json.obj(
                "text" -> "1",
                "href" -> "/page=1",
                "selected" -> "true"
              ),
              Json.obj(
                "text" -> "2",
                "href" -> "/page=2"
              ),
              Json.obj(
                "text" -> "3",
                "href" -> "/page=3"
              )
            )
          )

          renderer
            .render("viewAllArrivals.njk", sampleJson)
            .map(Ok(_))

        case _ => renderTechnicalDifficultiesPage
      }
  }
}
