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
import javax.inject.Inject
import models.{CheckMode, Movement}
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import uk.gov.hmrc.viewmodels.SummaryList.{Action => sAction}
import viewModels.ViewArrivalMovement

import scala.concurrent.ExecutionContext

class ViewArrivalNotificationsController @Inject()(renderer: Renderer,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   appConfig: FrontendAppConfig)
                                                  (implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with NunjucksSupport {

  def onPageLoad: Action[AnyContent] = Action.async {
    implicit request =>

      val actions =  List(
        sAction(
          content            = msg"site.edit",
          href               = "#",
          visuallyHiddenText = Some(msg"placeOfNotification.change.hidden"),
          attributes         = Map("id" -> s"""change-place-of-notification""")
        )
      )

      val movementHeader = Movement("12:15", "19bg327457893",  "Tesco", "Dover", "Normal", "Application sent", actions)

      val json = Json.obj(
        "declareArrivalNotificationUrl" -> appConfig.declareArrivalNotificationUrl,
        "dataRows" -> Json.toJson(ViewArrivalMovement(Seq(movementHeader)))
      )

      renderer.render("viewArrivalNotifications.njk", json).map(Ok(_))
  }
}
