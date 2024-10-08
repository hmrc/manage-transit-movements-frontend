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

import config.PaginationAppConfig
import controllers.actions._
import generated.CC056CType
import models.RichCC056CType
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.ReviewCancellationErrorsP5ViewModel.ReviewCancellationErrorsP5ViewModelProvider
import viewModels.pagination.ListPaginationViewModel
import views.html.departureP5.ReviewCancellationErrorsP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import generated.Generated_CC056CTypeFormat

class ReviewCancellationErrorsP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: ReviewCancellationErrorsP5ViewModelProvider,
  view: ReviewCancellationErrorsP5View
)(implicit val executionContext: ExecutionContext, paginationAppConfig: PaginationAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(page: Option[Int], departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC056CType](departureId, messageId)).async {
      implicit request =>
        val currentPage      = page.getOrElse(1)
        val functionalErrors = request.messageData.FunctionalError

        val paginationViewModel = ListPaginationViewModel(
          totalNumberOfItems = functionalErrors.length,
          currentPage = currentPage,
          numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
          href = controllers.departureP5.routes.ReviewCancellationErrorsP5Controller.onPageLoad(None, departureId, messageId).url
        )

        val rejectionMessageP5ViewModel =
          viewModelProvider.apply(request.messageData.pagedFunctionalErrors(currentPage), request.referenceNumbers.localReferenceNumber)

        rejectionMessageP5ViewModel.map(
          viewModel => Ok(view(viewModel, departureId, paginationViewModel))
        )
    }

}
