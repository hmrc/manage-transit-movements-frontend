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

import akka.actor.Status.Success
import config.FrontendAppConfig
import connectors.BetaAuthorizationConnector
import controllers.actions._
import forms.WhatDoYouWantToDoFormProvider

import javax.inject.Inject
import models.{EoriNumber, WhatDoYouWantToDoOptions}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.DisplayDeparturesService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class WhatDoYouWantToDoController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  renderer: Renderer,
  formProvider: WhatDoYouWantToDoFormProvider,
  displayDeparturesService: DisplayDeparturesService,
  frontendAppConfig: FrontendAppConfig,
  betaAuthorizationConnector: BetaAuthorizationConnector
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport
    with NunjucksSupport {

  def radioButtons(eoriNumber: EoriNumber)(implicit hc: HeaderCarrier): Future[Seq[Radios.Item]] =
    if (frontendAppConfig.isPrivateBetaEnabled) {
      betaAuthorizationConnector.getBetaUser(eoriNumber) map (WhatDoYouWantToDoOptions.radios(formProvider(), _))
    } else {
      Future.successful(WhatDoYouWantToDoOptions.radios(formProvider(), true))
    }

  def onPageLoad(): Action[AnyContent] = identify async {

    implicit request =>
      radioButtons(EoriNumber(request.eoriNumber)) flatMap {
        radios =>
          val form = formProvider()
          val json = Json.obj(
            "form"        -> form,
            "radios"      -> radios,
            "warningText" -> msg"whatDoYouWantToDo.warningText"
          )
          renderer.render("whatDoYouWantToDo.njk", json).map(Ok(_))
      }
  }

  def onSubmit(): Action[AnyContent] = identify async {
    implicit request =>
      radioButtons(EoriNumber(request.eoriNumber)) flatMap {
        radios =>
          formProvider()
            .bindFromRequest()
            .fold(
              formWithErrors => {
                val json = Json.obj(
                  "form"        -> formWithErrors,
                  "radios"      -> radios,
                  "warningText" -> msg"whatDoYouWantToDo.warningText"
                )
                renderer.render("whatDoYouWantToDo.njk", json).map(BadRequest(_))
              }, {
                case WhatDoYouWantToDoOptions.ArrivalNotifications =>
                  Future.successful(Redirect(routes.IndexController.onPageLoad()))
                case WhatDoYouWantToDoOptions.DepartureViewOldDeclarations =>
                  Future.successful(Redirect(routes.OldServiceInterstitialController.onPageLoad()))
                case WhatDoYouWantToDoOptions.DepartureMakeDeclarations =>
                  frontendAppConfig.departureJourneyToggle match {
                    case true  => Future.successful(Redirect(routes.IndexController.onPageLoad()))
                    case false => Future.successful(Redirect(routes.OldServiceInterstitialController.onPageLoad()))
                  }
                case WhatDoYouWantToDoOptions.DepartureViewDeclarations =>
                  Future.successful(Redirect(routes.IndexController.onPageLoad()))
                case WhatDoYouWantToDoOptions.NorthernIrelandMovements =>
                  Future.successful(Redirect(routes.NorthernIrelandInterstitialController.onPageLoad()))
              }
            )
      }
  }
}
