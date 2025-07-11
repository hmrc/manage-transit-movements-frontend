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

package controllers.arrivalP5

import config.{FrontendAppConfig, PaginationAppConfig}
import connectors.ArrivalMovementP5Connector
import controllers.actions.*
import forms.ArrivalsSearchFormProvider
import models.requests.IdentifierRequest
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import services.ArrivalP5MessageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.arrival.{ViewAllArrivalMovementsP5ViewModel, ViewArrivalP5}
import views.html.arrivalP5.ViewAllArrivalsP5View

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewAllArrivalsP5Controller @Inject() (
  actions: Actions,
  cc: MessagesControllerComponents,
  arrivalP5MessageService: ArrivalP5MessageService,
  arrivalMovementP5Connector: ArrivalMovementP5Connector,
  formProvider: ArrivalsSearchFormProvider,
  view: ViewAllArrivalsP5View
)(implicit ec: ExecutionContext, appConfig: FrontendAppConfig, paginationConfig: PaginationAppConfig, clock: Clock)
    extends FrontendController(cc)
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(page: Option[Int], mrn: Option[String]): Action[AnyContent] = (Action andThen actions.identify()).async {
    implicit request =>
      buildView(form.fillAndValidate(mrn), page, mrn)
  }

  def onSubmit(page: Option[Int]): Action[AnyContent] = (Action andThen actions.identify()).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => buildView(formWithErrors, page, None),
          value => {
            val mrn: Option[String] = value.filter(_.trim.nonEmpty)
            Future.successful(Redirect(routes.ViewAllArrivalsP5Controller.onPageLoad(None, mrn)))
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
      arrivalMovementP5Connector.getAllMovementsForSearchQuery(currentPage, paginationConfig.numberOfMovements, searchParam).flatMap {
        case Some(movements) =>
          arrivalP5MessageService.getLatestMessagesForMovements(movements).map {
            movementsAndMessages =>
              // TODO - pass `movementsAndMessages` into view model and do the mapping in there
              val arrivals: Seq[ViewArrivalP5] = movementsAndMessages.map(ViewArrivalP5(_))

              val viewModel = ViewAllArrivalMovementsP5ViewModel(
                arrivals,
                searchParam,
                currentPage,
                paginationConfig.numberOfMovements,
                movements.totalCount
              )

              block(view(form, viewModel))
          }
        case None =>
          Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
      }

    if (form.hasErrors) {
      getMovements(None)(BadRequest(_))
    } else {
      getMovements(searchParam)(Ok(_))
    }
  }
}
