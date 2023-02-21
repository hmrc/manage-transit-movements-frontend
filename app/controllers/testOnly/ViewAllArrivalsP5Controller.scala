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
import forms.SearchFormProvider
import models.requests.IdentifierRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import services.ArrivalP5MessageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.{ViewAllArrivalMovementsP5ViewModel, ViewArrivalP5}
import viewModels.pagination.{MovementsPaginationViewModel, PaginationViewModel}
import views.html.arrival.P5.ViewAllArrivalsP5View

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewAllArrivalsP5Controller @Inject() (
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  val config: FrontendAppConfig,
  val paginationAppConfig: PaginationAppConfig,
  arrivalP5MessageService: ArrivalP5MessageService,
  arrivalMovementP5Connector: ArrivalMovementP5Connector,
  formProvider: SearchFormProvider,
  view: ViewAllArrivalsP5View
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
    arrivalMovementP5Connector.getAllMovements().flatMap {
      case Some(movements) =>
        arrivalP5MessageService.getMessagesForAllMovements(movements).map {
          movementsAndMessages =>
            val viewArrivalP5: Seq[ViewArrivalP5] = movementsAndMessages.map(ViewArrivalP5(_))

            val paginationViewModel = MovementsPaginationViewModel(
              totalNumberOfMovements = movementsAndMessages.length,
              currentPage = 1,
              numberOfMovementsPerPage = paginationAppConfig.arrivalsNumberOfMovements,
              href = controllers.testOnly.routes.ViewAllArrivalsP5Controller.onPageLoad(None).url
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
