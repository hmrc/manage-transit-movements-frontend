/*
 * Copyright 2023 HM Revenue & Customs
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

import config.{FrontendAppConfig, PaginationAppConfig}
import controllers.actions._
import forms.SearchFormProvider
import models.domain.StringFieldRegex.alphaNumericRegexHyphensUnderscores
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.arrival.P5.ViewAllDeparturesP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ViewAllDeparturesP5Controller @Inject() (
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  val config: FrontendAppConfig,
  val paginationAppConfig: PaginationAppConfig,
  formProvider: SearchFormProvider,
  view: ViewAllDeparturesP5View
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  private val form = formProvider("departures.search.form.value.invalid", alphaNumericRegexHyphensUnderscores)

  def onPageLoad(page: Option[Int] = None): Action[AnyContent] = (Action andThen identify) {
    implicit request =>
      Ok(view(form))
  }

}
