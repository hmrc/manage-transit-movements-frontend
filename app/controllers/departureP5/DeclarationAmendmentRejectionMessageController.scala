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
import generated.{CC022CType, Generated_CC022CTypeFormat}
import models.FunctionalErrorType
import models.departureP5.Rejection.IE022Rejection
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AmendmentService, FunctionalErrorsService}
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.DeclarationAmendmentRejectionMessageViewModel
import views.html.departureP5.DeclarationAmendmentRejectionMessageView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationAmendmentRejectionMessageController @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  service: AmendmentService,
  view: DeclarationAmendmentRejectionMessageView,
  functionalErrorsService: FunctionalErrorsService,
  config: FrontendAppConfig
)(implicit val executionContext: ExecutionContext, paginationConfig: PaginationAppConfig)
    extends FrontendController(cc)
    with I18nSupport
    with Logging {

  def onPageLoad(page: Option[Int], departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC022CType](departureId, messageId)).async {
      implicit request =>
        val lrn                 = request.referenceNumbers.localReferenceNumber
        val rejection           = IE022Rejection(departureId, request.messageData)
        val messageSender       = request.messageData.messageSequence1.messageSender
        val functionalErrorsSeq = request.messageData.FunctionalError.map(FunctionalErrorType(_))

        service.isRejectionAmendable(lrn, rejection).flatMap {
          case true =>
            val functionalErrorsF = functionalErrorsService.convertErrorsWithSectionAndSender(functionalErrorsSeq, messageSender)

            functionalErrorsF.map {
              functionalErrors =>
                val viewModel = DeclarationAmendmentRejectionMessageViewModel(
                  functionalErrors = functionalErrors,
                  lrn = request.referenceNumbers.localReferenceNumber,
                  currentPage = page,
                  numberOfErrorsPerPage = paginationConfig.numberOfErrorsPerPage,
                  departureId = departureId,
                  messageId = messageId
                )

                Ok(view(viewModel, departureId, messageId, request.referenceNumbers.movementReferenceNumber))
            }
          case _ =>
            logger.warn(s"[DeclarationAmendmentRejectionMessageController] Could not proceed with amending $departureId")
            Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
        }
    }

  def onSubmit(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC022CType](departureId, messageId)).async {
      implicit request =>
        val lrn       = request.referenceNumbers.localReferenceNumber
        val rejection = IE022Rejection(departureId, request.messageData)

        service.handleErrors(lrn, rejection).map {
          case response if is2xx(response.status) =>
            Redirect(config.departureFrontendTaskListUrl(lrn))
          case _ =>
            Redirect(controllers.routes.ErrorController.technicalDifficulties())
        }
    }

}
