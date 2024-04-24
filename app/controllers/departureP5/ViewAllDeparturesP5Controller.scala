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
import controllers.actions._
import forms.DeparturesSearchFormProvider
import models.requests.IdentifierRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import services.DepartureP5MessageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.{ViewAllDepartureMovementsP5ViewModel, ViewDepartureP5}
import viewModels.pagination.ListPaginationViewModel
import views.html.departureP5.ViewAllDeparturesP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewAllDeparturesP5Controller @Inject() (
  actions: Actions,
  cc: MessagesControllerComponents,
  formProvider: DeparturesSearchFormProvider,
  departureP5MessageService: DepartureP5MessageService,
  departureMovementP5Connector: DepartureMovementP5Connector,
  view: ViewAllDeparturesP5View
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig, paginationConfig: PaginationAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(page: Option[Int], lrn: Option[String]): Action[AnyContent] = (Action andThen actions.checkP5Switch()).async {
    implicit request =>
      val preparedForm = lrn match {
        case Some(value) => form.fill(value)
        case None        => form
      }
      buildView(preparedForm, page, lrn)(Ok(_))
  }

  def onSubmit(): Action[AnyContent] = (Action andThen actions.checkP5Switch()).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => buildView(formWithErrors, None, None)(BadRequest(_)),
          value => {
            val lrn: Option[String] = Option(value).filter(_.trim.nonEmpty)
            Future.successful(Redirect(controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, lrn)))
          }
        )
  }

  private def buildView(form: Form[String], page: Option[Int], searchParam: Option[String])(
    block: HtmlFormat.Appendable => Result
  )(implicit request: IdentifierRequest[_]): Future[Result] = {
    val currentPage = page.getOrElse(1)
    departureMovementP5Connector.getAllMovementsForSearchQuery(currentPage, paginationConfig.departuresNumberOfMovements, searchParam).flatMap {
      case Some(movements) =>
        departureP5MessageService.getLatestMessagesForMovement(movements).map {
          movementsAndMessages =>
            val viewDepartureP5: Seq[ViewDepartureP5] = movementsAndMessages.map(ViewDepartureP5(_))

            val paginationViewModel = ListPaginationViewModel(
              totalNumberOfItems = movements.totalCount,
              currentPage = currentPage,
              numberOfItemsPerPage = paginationConfig.departuresNumberOfMovements,
              href = controllers.departureP5.routes.ViewAllDeparturesP5Controller.onPageLoad(None, None).url,
              additionalParams = Seq(
                searchParam.map("lrn" -> _)
              ).flatten
            )

            block(
              view(
                form = form,
                viewModel = ViewAllDepartureMovementsP5ViewModel(viewDepartureP5, paginationViewModel, searchParam)
              )
            )
        }
      case None => Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
    }
  }

}
