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
import controllers.actions.*
import generated.{CC056CType, Generated_CC056CTypeFormat}
import models.departureP5.BusinessRejectionType.DepartureBusinessRejectionType
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FunctionalErrorsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.ReviewDepartureErrorsP5ViewModel.ReviewDepartureErrorsP5ViewModelProvider
import views.html.departureP5.ReviewDepartureErrorsP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ReviewDepartureErrorsP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: ReviewDepartureErrorsP5ViewModelProvider,
  view: ReviewDepartureErrorsP5View,
  functionalErrorsService: FunctionalErrorsService
)(implicit val executionContext: ExecutionContext, config: FrontendAppConfig, paginationConfig: PaginationAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(page: Option[Int], departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC056CType](departureId, messageId)).async {
      implicit request =>
        functionalErrorsService.convertErrorsWithSection(request.messageData.FunctionalError).map {
          functionalErrors =>
            val currentPage = page.getOrElse(1)

            // TODO - do we need two view models here? Pagination involved in both. Seems strange.
            // TODO - can we consolidate some of the pagination config values into one?

            val rejectionViewModel = viewModelProvider.apply(
              functionalErrors,
              request.referenceNumbers.localReferenceNumber,
              DepartureBusinessRejectionType(request.messageData),
              currentPage,
              paginationConfig.departuresNumberOfErrorsPerPage,
              href = controllers.departureP5.routes.ReviewDepartureErrorsP5Controller.onPageLoad(None, departureId, messageId)
            )

            Ok(view(rejectionViewModel, departureId, request.referenceNumbers.movementReferenceNumber))
        }
    }
}
