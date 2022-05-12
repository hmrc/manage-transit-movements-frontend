/*
 * Copyright 2022 HM Revenue & Customs
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

import config.{FrontendAppConfig, PaginationAppConfig}
import connectors.DeparturesMovementConnector
import controllers.actions._
import forms.SearchFormProvider
import handlers.ErrorHandler
import models.requests.IdentifierRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.pagination.PaginationViewModel
import viewModels.{ViewAllDepartureMovementsViewModel, ViewDeparture}
import views.html.departure.ViewAllDeparturesView

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewAllDeparturesController @Inject() (
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  paginationAppConfig: PaginationAppConfig,
  departuresMovementConnector: DeparturesMovementConnector,
  formProvider: SearchFormProvider,
  view: ViewAllDeparturesView,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig, clock: Clock)
    extends FrontendController(cc)
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(page: Option[Int]): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      getViewModel(page) {
        viewModel =>
          Ok(view(form, viewModel))
      }
  }

  def onSubmit(page: Option[Int]): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            getViewModel(page) {
              viewModel =>
                BadRequest(view(formWithErrors, viewModel))
            },
          value => Future.successful(Redirect(routes.ViewDeparturesSearchResultsController.onPageLoad(value)))
        )
  }

  private def getViewModel(page: Option[Int])(block: ViewAllDepartureMovementsViewModel => Result)(implicit
    request: IdentifierRequest[_]
  ): Future[Result] = {
    val currentPage = page.getOrElse(1)
    departuresMovementConnector.getPagedDepartures(currentPage, paginationAppConfig.departuresNumberOfMovements).flatMap {
      case Some(filteredDepartures) =>
        val movements: Seq[ViewDeparture] = filteredDepartures.departures.map(ViewDeparture(_))

        val paginationViewModel = PaginationViewModel(
          totalNumberOfMovements = filteredDepartures.totalDepartures,
          currentPage = currentPage,
          numberOfMovementsPerPage = paginationAppConfig.arrivalsNumberOfMovements,
          href = routes.ViewAllDeparturesController.onPageLoad(None).url
        )

        Future.successful(block(ViewAllDepartureMovementsViewModel(movements, paginationViewModel)))
      case _ => errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
    }
  }
}
