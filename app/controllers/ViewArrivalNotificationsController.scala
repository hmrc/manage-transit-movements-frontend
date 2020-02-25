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
import connectors.DestinationConnector
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.ExecutionContext

class ViewArrivalNotificationsController @Inject()(renderer: Renderer,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   appConfig: FrontendAppConfig,
                                                   destinationConnector: DestinationConnector)
                                                  (implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = Action.async {
    implicit request =>

      destinationConnector.getArrivalMovements.flatMap {
        dataRows =>

          val json = Json.obj(
            "declareArrivalNotificationUrl" -> appConfig.declareArrivalNotificationUrl,
            "homePageUrl" -> routes.IndexController.onPageLoad().url,
            "dataRows" -> dataRows
          )

          renderer.render("viewArrivalNotifications.njk", json).map(Ok(_))
      }
  }
}
