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
import connectors.ArrivalMovementP5Connector
import controllers.actions._
import forms.ArrivalsSearchFormProvider
import models.requests.IdentifierRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import services.ArrivalP5MessageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.arrival.{ViewAllArrivalMovementsP5ViewModel, ViewArrivalP5}
import viewModels.pagination.ListPaginationViewModel
import views.html.arrival.P5.ViewAllArrivalsP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewAllArrivalsP5Controller @Inject() (
  actions: Actions,
  cc: MessagesControllerComponents,
  arrivalP5MessageService: ArrivalP5MessageService,
  arrivalMovementP5Connector: ArrivalMovementP5Connector,
  formProvider: ArrivalsSearchFormProvider,
  view: ViewAllArrivalsP5View
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig, paginationConfig: PaginationAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(page: Option[Int], mrn: Option[String]): Action[AnyContent] = (Action andThen actions.checkP5Switch()).async {
    implicit request =>
      val preparedForm = mrn match {
        case Some(value) => form.fill(value)
        case None        => form
      }
      buildView(preparedForm, page, mrn)(Ok(_))
  }

  def onSubmit(): Action[AnyContent] = (Action andThen actions.checkP5Switch()).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => buildView(formWithErrors, None, None)(BadRequest(_)),
          value => {
            val mrn: Option[String] = Option(value).filter(_.trim.nonEmpty)
            Future.successful(Redirect(controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None, mrn)))
          }
        )
  }

  private def buildView(form: Form[String], page: Option[Int], searchParam: Option[String])(
    block: HtmlFormat.Appendable => Result
  )(implicit request: IdentifierRequest[_]): Future[Result] = {
    val currentPage = page.getOrElse(1)
    arrivalMovementP5Connector.getAllMovementsForSearchQuery(currentPage, paginationConfig.arrivalsNumberOfMovements, searchParam).flatMap {
      case Some(movements) =>
        arrivalP5MessageService.getMessagesForAllMovements(movements).map {
          movementsAndMessages =>
            val viewArrivalP5: Seq[ViewArrivalP5] = movementsAndMessages.map(ViewArrivalP5(_))

            val paginationViewModel = ListPaginationViewModel(
              totalNumberOfItems = movements.totalCount,
              currentPage = currentPage,
              numberOfItemsPerPage = paginationConfig.arrivalsNumberOfMovements,
              href = controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None, None).url,
              additionalParams = Seq(
                searchParam.map("mrn" -> _)
              ).flatten
            )

            block(
              view(
                form = form,
                viewModel = ViewAllArrivalMovementsP5ViewModel(viewArrivalP5, paginationViewModel)
              )
            )
        }
      case None => Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
    }
  }

}
