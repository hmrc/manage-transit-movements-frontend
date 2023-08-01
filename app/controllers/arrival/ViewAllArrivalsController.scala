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

package controllers.arrival

import config.{FrontendAppConfig, PaginationAppConfig}
import connectors.ArrivalMovementConnector
import controllers.actions._
import forms.ArrivalsSearchFormProvider
import handlers.ErrorHandler
import models.requests.IdentifierRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.pagination.ListPaginationViewModel
import viewModels.{ViewAllArrivalMovementsViewModel, ViewArrival}
import views.html.arrival.ViewAllArrivalsView

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewAllArrivalsController @Inject() (
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  val config: FrontendAppConfig,
  val paginationAppConfig: PaginationAppConfig,
  arrivalMovementConnector: ArrivalMovementConnector,
  formProvider: ArrivalsSearchFormProvider,
  view: ViewAllArrivalsView,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig, clock: Clock)
    extends FrontendController(cc)
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(page: Option[Int] = None): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      buildView(page, form)(Ok(_))
  }

  def onSubmit(page: Option[Int] = None): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => buildView(page, formWithErrors)(BadRequest(_)),
          value => Future.successful(Redirect(routes.ViewArrivalsSearchResultsController.onPageLoad(value)))
        )
  }

  private def buildView(page: Option[Int], form: Form[String])(
    block: HtmlFormat.Appendable => Result
  )(implicit request: IdentifierRequest[_]): Future[Result] = {
    val currentPage = page.getOrElse(1)
    arrivalMovementConnector.getPagedArrivals(currentPage, paginationAppConfig.arrivalsNumberOfMovements).flatMap {
      case Some(filteredArrivals) =>
        val movements: Seq[ViewArrival] = filteredArrivals.arrivals.map(ViewArrival(_))

        val paginationViewModel = ListPaginationViewModel(
          totalNumberOfMovements = filteredArrivals.totalArrivals,
          currentPage = currentPage,
          numberOfMovementsPerPage = paginationAppConfig.arrivalsNumberOfMovements,
          href = routes.ViewAllArrivalsController.onPageLoad(None).url
        )

        Future.successful(
          block(
            view(
              form = form,
              viewModel = ViewAllArrivalMovementsViewModel(movements, paginationViewModel)
            )
          )
        )
      case _ => errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
    }
  }
}
