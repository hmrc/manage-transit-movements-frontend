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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import forms.WhatDoYouWantToDoFormProvider
import models.WhatDoYouWantToDoOptions
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatDoYouWantToDoController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  renderer: Renderer,
  appConfig: FrontendAppConfig,
  formProvider: WhatDoYouWantToDoFormProvider
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(): Action[AnyContent] = identify async {
    implicit request =>
      val json = Json.obj(
        "form"        -> form,
        "radios"      -> WhatDoYouWantToDoOptions.radios(form),
        "warningText" -> msg"whatDoYouWantToDo.warningText"
      )
      renderer.render("whatDoYouWantToDo.njk", json).map(Ok(_))
  }

  def onSubmit(): Action[AnyContent] = identify async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val json = Json.obj(
              "form"        -> formWithErrors,
              "radios"      -> WhatDoYouWantToDoOptions.radios(formWithErrors),
              "warningText" -> msg"whatDoYouWantToDo.warningText"
            )
            renderer.render("whatDoYouWantToDo.njk", json).map(BadRequest(_))
          }, {
            case WhatDoYouWantToDoOptions.MakeArrivalNotification =>
              Future.successful(Redirect(routes.IndexController.onPageLoad()))
            case WhatDoYouWantToDoOptions.ViewArrivalNotificationAfter =>
              Future.successful(Redirect(routes.IndexController.onPageLoad()))
            case WhatDoYouWantToDoOptions.ViewArrivalNotificationBefore =>
              Future.successful(Redirect(routes.OldServiceInterstitialController.onPageLoad()))
            case WhatDoYouWantToDoOptions.MakeDepartureDeclaration =>
              Future.successful(Redirect(routes.OldServiceInterstitialController.onPageLoad()))
            case WhatDoYouWantToDoOptions.ViewDepartureDeclaration =>
              Future.successful(Redirect(routes.ViewDeparturesController.onPageLoad()))
            case WhatDoYouWantToDoOptions.ViewNorthernIreland =>
              Future.successful(Redirect(routes.NorthernIrelandInterstitialController.onPageLoad()))
          }
        )
  }
}
