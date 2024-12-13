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
import controllers.actions.*
import generated.{CC056CType, Generated_CC056CTypeFormat}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FunctionalErrorsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.ReviewCancellationErrorsP5ViewModel
import views.html.departureP5.ReviewCancellationErrorsP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ReviewCancellationErrorsP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  view: ReviewCancellationErrorsP5View,
  functionalErrorsService: FunctionalErrorsService
)(implicit val executionContext: ExecutionContext, paginationConfig: PaginationAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(page: Option[Int], departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC056CType](departureId, messageId)).async {
      implicit request =>
        functionalErrorsService.convertErrorsWithoutSection(request.messageData.FunctionalError).map {
          functionalErrors =>
            val viewModel = ReviewCancellationErrorsP5ViewModel(
              functionalErrors = functionalErrors,
              lrn = request.referenceNumbers.localReferenceNumber,
              currentPage = page,
              numberOfErrorsPerPage = paginationConfig.departuresNumberOfErrorsPerPage,
              departureId = departureId,
              messageId = messageId
            )

            Ok(view(viewModel, departureId))
        }
    }
}
