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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.ErrorViewModel.ErrorViewModelProvider
import viewModels.P5.arrival.ArrivalNotificationWithFunctionalErrorsP5ViewModel.ArrivalNotificationWithFunctionalErrorsP5ViewModelProvider
import viewModels.pagination.ListPaginationViewModel
import views.html.arrival.P5.ArrivalNotificationWithFunctionalErrorsP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ArrivalNotificationWithFunctionalErrorsP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  rejectionMessageAction: ArrivalRejectionMessageActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: ArrivalNotificationWithFunctionalErrorsP5ViewModelProvider,
  errorViewModelProvider: ErrorViewModelProvider,
  view: ArrivalNotificationWithFunctionalErrorsP5View
)(implicit val executionContext: ExecutionContext, config: FrontendAppConfig, paginationConfig: PaginationAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(page: Option[Int], arrivalId: String): Action[AnyContent] = (Action andThen identify andThen rejectionMessageAction(arrivalId)).async {
    implicit request =>
      val currentPage = page.getOrElse(1)

      val paginationViewModel = ListPaginationViewModel(
        totalNumberOfItems = request.ie057MessageData.functionalErrors.length,
        currentPage = currentPage,
        numberOfItemsPerPage = paginationConfig.arrivalsNumberOfErrorsPerPage,
        href = controllers.testOnly.routes.ArrivalNotificationWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalId).url
      )

      val rejectionMessageP5ViewModel = viewModelProvider.apply(
        request.ie057MessageData.pagedFunctionalErrors(currentPage),
        request.ie057MessageData.transitOperation.MRN
      )
      val errorViewModel = errorViewModelProvider.apply(request.ie057MessageData.pagedFunctionalErrors(currentPage))
      errorViewModel.map {
        errorViewModel =>
          if (request.ie057MessageData.functionalErrors.nonEmpty) {
            Ok(view(rejectionMessageP5ViewModel, arrivalId, paginationViewModel, errorViewModel))
          } else {
            Redirect(controllers.routes.ErrorController.technicalDifficulties())
          }
      }
  }
}
