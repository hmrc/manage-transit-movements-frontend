/*
 * Copyright 2026 HM Revenue & Customs
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
import generated.{CC022CType, Generated_CC022CTypeFormat}
import models.FunctionalErrorType
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FunctionalErrorsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.ReviewDepartureAmendmentErrorsP5ViewModel
import views.html.departureP5.ReviewDepartureAmendmentErrorsP5View

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ReviewDepartureAmendmentErrorsP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  view: ReviewDepartureAmendmentErrorsP5View,
  functionalErrorsService: FunctionalErrorsService,
  paginationConfig: PaginationAppConfig
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(page: Option[Int], departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC022CType](departureId, messageId)).async {
      implicit request =>
        val messageSender       = request.messageData.messageSequence1.messageSender
        val functionalErrorsSeq = request.messageData.FunctionalError.map(FunctionalErrorType(_))

        val functionalErrorsF =
          functionalErrorsService.convertErrorsWithSectionAndSender(functionalErrorsSeq, messageSender)
        functionalErrorsF.map {
          functionalErrors =>
            val viewModel = ReviewDepartureAmendmentErrorsP5ViewModel(
              functionalErrors = functionalErrors,
              lrn = request.referenceNumbers.localReferenceNumber,
              currentPage = page,
              numberOfErrorsPerPage = paginationConfig.numberOfErrorsPerPage,
              departureId = departureId,
              messageId = messageId
            )

            Ok(view(viewModel, departureId, request.referenceNumbers.movementReferenceNumber))
        }
    }
}
