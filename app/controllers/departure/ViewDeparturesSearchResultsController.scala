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

package controllers.departure

import config.{FrontendAppConfig, SearchResultsAppConfig}
import connectors.DeparturesMovementConnector
import controllers.actions._
import forms.SearchFormProvider
import handlers.ErrorHandler
import models.domain.StringFieldRegex.alphaNumericRegexHyphensUnderscores
import models.requests.IdentifierRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.{ViewDeparture, ViewDepartureMovements}
import views.html.departure.ViewDeparturesSearchResultsView

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewDeparturesSearchResultsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  connector: DeparturesMovementConnector,
  searchResultsAppConfig: SearchResultsAppConfig,
  formProvider: SearchFormProvider,
  view: ViewDeparturesSearchResultsView,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext, frontendAppConfig: FrontendAppConfig, clock: Clock)
    extends FrontendController(cc)
    with I18nSupport {

  private val form = formProvider("departures.search.form.value.invalid", alphaNumericRegexHyphensUnderscores)

  private lazy val pageSize = searchResultsAppConfig.maxSearchResults

  def onPageLoad(lrn: String): Action[AnyContent] = (Action andThen identify).async {
    implicit request: IdentifierRequest[AnyContent] =>
      buildView(lrn, form.fill)(Ok(_))
  }

  def onSubmit(lrn: String): Action[AnyContent] = (Action andThen identify).async {
    implicit request: IdentifierRequest[AnyContent] =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => buildView(lrn, _ => formWithErrors)(BadRequest(_)),
          value => Future.successful(Redirect(routes.ViewDeparturesSearchResultsController.onPageLoad(value)))
        )
  }

  private def buildView(lrn: String, form: String => Form[String])(
    block: HtmlFormat.Appendable => Result
  )(implicit request: IdentifierRequest[_]): Future[Result] =
    lrn.trim match {
      case lrn if lrn.isEmpty =>
        Future.successful(Redirect(routes.ViewAllDeparturesController.onPageLoad(None)))
      case lrn =>
        connector.getDepartureSearchResults(lrn, pageSize).flatMap {
          case Some(allDepartures) =>
            val movements: Seq[ViewDeparture] = allDepartures.departures.map(ViewDeparture(_))
            Future.successful(
              block(
                view(
                  form = form(lrn),
                  lrn = lrn,
                  dataRows = ViewDepartureMovements.apply(movements).dataRows,
                  retrieved = allDepartures.retrievedDepartures,
                  tooManyResults = allDepartures.tooManyResults
                )
              )
            )
          case _ => errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
        }
    }
}
