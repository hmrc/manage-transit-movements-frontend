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
import forms.CancellationReasonFormProvider
import models.{LocalReferenceNumber, NormalMode, UserAnswers}
import navigation.Navigator
import pages.CancellationReasonPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CancellationReasonController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  formProvider: CancellationReasonFormProvider,
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
        "form" -> form,
        "lrn"  -> lrn,
      )

      renderer.render("cancellationReason.njk", json).map(Ok(_))
  }

  def onSubmit(lrn: LocalReferenceNumber): Action[AnyContent] = identify.async {
    implicit request =>
      val userAnswers = UserAnswers(request.eoriNumber)
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {

            val json = Json.obj(
              "form" -> formWithErrors,
              "lrn"  -> lrn
            )

            renderer.render("cancellationReason.njk", json).map(BadRequest(_))
          },
          value =>
            for {
              updatedAnswers <- Future.fromTry(userAnswers.set(CancellationReasonPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(CancellationReasonPage, NormalMode, updatedAnswers))
        )
  }
}
