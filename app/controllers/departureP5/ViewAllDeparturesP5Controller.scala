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

package controllers.departureP5

import config.{FrontendAppConfig, PaginationAppConfig}
import connectors.DepartureMovementP5Connector
import controllers.actions.*
import forms.DeparturesSearchFormProvider
import models.requests.IdentifierRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import services.DepartureP5MessageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.{ViewAllDepartureMovementsP5ViewModel, ViewDepartureP5}
import views.html.departureP5.ViewAllDeparturesP5View

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewAllDeparturesP5Controller @Inject() (
  actions: Actions,
  cc: MessagesControllerComponents,
  formProvider: DeparturesSearchFormProvider,
  departureP5MessageService: DepartureP5MessageService,
  departureMovementP5Connector: DepartureMovementP5Connector,
  view: ViewAllDeparturesP5View
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig, paginationConfig: PaginationAppConfig, clock: Clock)
    extends FrontendController(cc)
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(page: Option[Int], lrn: Option[String]): Action[AnyContent] = (Action andThen actions.identify()).async {
    implicit request =>
      buildView(form.fillAndValidate(lrn), page, lrn)
  }

  def onSubmit(): Action[AnyContent] = (Action andThen actions.identify()).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => buildView(formWithErrors, None, None),
          value => {
            val lrn: Option[String] = value.filter(_.trim.nonEmpty)
            Future.successful(Redirect(controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, lrn)))
          }
        )
  }

  private def buildView(
    form: Form[Option[String]],
    page: Option[Int],
    searchParam: Option[String]
  )(implicit request: IdentifierRequest[?]): Future[Result] = {
    val currentPage = page.getOrElse(1)

    def getMovements(searchParam: Option[String])(block: HtmlFormat.Appendable => Result): Future[Result] =
      departureMovementP5Connector.getAllMovementsForSearchQuery(currentPage, paginationConfig.numberOfMovements, searchParam).flatMap {
        case Some(movements) =>
          departureP5MessageService.getLatestMessagesForMovements(movements).map {
            movementsAndMessages =>
              // TODO - pass `movementsAndMessages` into view model and do the mapping in there
              val departures: Seq[ViewDepartureP5] = movementsAndMessages.map(ViewDepartureP5(_))

              val viewModel = ViewAllDepartureMovementsP5ViewModel(
                departures,
                searchParam,
                currentPage,
                paginationConfig.numberOfMovements,
                movements.totalCount
              )

              block(view(form, viewModel))
          }
        case None => Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
      }

    if (form.hasErrors) {
      getMovements(None)(BadRequest(_))
    } else {
      getMovements(searchParam)(Ok(_))
    }
  }
}
