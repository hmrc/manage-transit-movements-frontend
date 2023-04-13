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
import connectors.DepartureMovementP5Connector
import controllers.actions._
import forms.SearchFormProvider
import models.requests.IdentifierRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import services.DepartureP5MessageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.arrival.{ViewAllArrivalMovementsP5ViewModel, ViewArrivalP5}
import viewModels.P5.departure.{ViewAllDepartureMovementsP5ViewModel, ViewDepartureP5}
import viewModels.pagination.MovementsPaginationViewModel
import views.html.departure.P5.ViewAllDeparturesP5View

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewAllDeparturesP5Controller @Inject() (
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  val config: FrontendAppConfig,
  val paginationAppConfig: PaginationAppConfig,
  formProvider: SearchFormProvider,
  departureP5MessageService: DepartureP5MessageService,
  departureMovementP5Connector: DepartureMovementP5Connector,
  view: ViewAllDeparturesP5View
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig, clock: Clock)
    extends FrontendController(cc)
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(page: Option[Int] = None): Action[AnyContent] = (Action andThen identify).async {
    implicit request =>
      buildView(page, form)(Ok(_))
  }

  private def buildView(page: Option[Int], form: Form[String])(
    block: HtmlFormat.Appendable => Result
  )(implicit request: IdentifierRequest[_]): Future[Result] =
    departureMovementP5Connector.getAllMovements().flatMap {
      case Some(movements) =>
        departureP5MessageService.getMessagesForAllMovements(movements).map {
          movementsAndMessages =>
            val viewDepartureP5: Seq[ViewDepartureP5] = movementsAndMessages.map(ViewDepartureP5(_))

            val paginationViewModel = MovementsPaginationViewModel(
              totalNumberOfMovements = movementsAndMessages.length,
              currentPage = 1,
              numberOfMovementsPerPage = paginationAppConfig.arrivalsNumberOfMovements,
              href = controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None).url
            )

            block(
              view(
                form = form,
                viewModel = ViewAllDepartureMovementsP5ViewModel(viewDepartureP5, paginationViewModel)
              )
            )
        }
      case None => Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
    }

}
