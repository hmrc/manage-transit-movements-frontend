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

import java.time.LocalDate

import config.FrontendAppConfig
import javax.inject.Inject
import models.Movement
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import viewModels.ViewArrivalMovement

import scala.concurrent.ExecutionContext

class ViewArrivalNotificationsController @Inject()(renderer: Renderer,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   appConfig: FrontendAppConfig)
                                                  (implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = Action.async {
    implicit request =>

      val movement1  = Movement("12:15", "19bg327457893",  "Tesco", "Dover", "Normal", "Application sent", Seq("history"))
      val movement2  = Movement("12:15", "19bg327457893",  "Tesco", "Dover", "Normal", "Application sent", Seq("unloading"))
      val movement3  = Movement("12:15", "19bg327457893",  "Tesco", "Dover", "Normal", "Application sent", Seq("errors"))
      val mockDataSet =
        ViewArrivalMovement(
          Map(
            "11 May 2019" -> Seq(movement1, movement2),
            "12 May 2019" -> Seq(movement2, movement3)
          )
      )

      val json = Json.obj(
        "declareArrivalNotificationUrl" -> appConfig.declareArrivalNotificationUrl,
        "dataRows" -> Json.toJson(mockDataSet)
      )

      renderer.render("viewArrivalNotifications.njk", json).map(Ok(_))
  }
}
