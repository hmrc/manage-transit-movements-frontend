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
import models.LocalReferenceNumber
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.ReviewCancellationErrorsP5ViewModel.ReviewCancellationErrorsP5ViewModelProvider
import viewModels.pagination.ListPaginationViewModel
import views.html.departure.TestOnly.ReviewCancellationErrorsP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ReviewCancellationErrorsP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  rejectionMessageAction: DepartureRejectionMessageActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: ReviewCancellationErrorsP5ViewModelProvider,
  view: ReviewCancellationErrorsP5View
)(implicit val executionContext: ExecutionContext, config: FrontendAppConfig, paginationAppConfig: PaginationAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(page: Option[Int], departureId: String, localReferenceNumber: LocalReferenceNumber): Action[AnyContent] =
    (Action andThen identify andThen rejectionMessageAction(departureId, localReferenceNumber)).async {
      implicit request =>
        val currentPage = page.getOrElse(1)

        val paginationViewModel = ListPaginationViewModel(
          totalNumberOfItems = request.ie056MessageData.functionalErrors.length,
          currentPage = currentPage,
          numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
          href = controllers.testOnly.routes.ReviewCancellationErrorsP5Controller.onPageLoad(None, departureId, localReferenceNumber).url
        )

        val rejectionMessageP5ViewModel =
          viewModelProvider.apply(request.ie056MessageData.pagedFunctionalErrors(currentPage), localReferenceNumber.value)

        rejectionMessageP5ViewModel.map(
          viewModel => Ok(view(viewModel, departureId, paginationViewModel))
        )
    }
}
