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

import controllers.actions._
import forms.ConfirmCancellationFormProvider

import javax.inject.Inject
import models.{LocalReferenceNumber, Mode, NormalMode, UserAnswers, WhatDoYouWantToDoOptions}
import navigation.Navigator
import pages.{CancellationReasonPage, ConfirmCancellationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class ConfirmCancellationController @Inject()(
  override val messagesApi: MessagesApi,
  navigator: Navigator,
  identify: IdentifierAction,
  formProvider: ConfirmCancellationFormProvider,
  cc: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(lrn: LocalReferenceNumber): Action[AnyContent] = identify.async {
    implicit request =>
      val json = Json.obj(
        "form"   -> form,
        "mrn"    -> lrn,
        "radios" -> Radios.yesNo(form("value"))
      )

      renderer.render("confirmCancellation.njk", json).map(Ok(_))
  }

  def onSubmit(lrn: LocalReferenceNumber): Action[AnyContent] = identify.async {
    implicit request =>
      val userAnswers = UserAnswers(request.eoriNumber)

      form
        .bindFromRequest()
        .fold(
          formWithErrors => {

            val json = Json.obj(
              "form"   -> formWithErrors,
              "lrn"    -> lrn,
              "radios" -> Radios.yesNo(formWithErrors("value"))
            )
            renderer.render("confirmCancellation.njk", json).map(BadRequest(_))
          },
          success = value =>
            for {
              updatedAnswers <- Future.fromTry(userAnswers.set(ConfirmCancellationPage, value))
            } yield
              if (value == true) {
                Redirect(controllers.routes.CancellationReasonController.onPageLoad(lrn))
              } else {
                Redirect(controllers.routes.ViewArrivalsController.onPageLoad)
            }
        )
  }
}
