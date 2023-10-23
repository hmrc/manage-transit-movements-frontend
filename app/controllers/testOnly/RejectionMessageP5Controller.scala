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
import connectors.DepartureCacheConnector
import controllers.actions._
import models.departureP5.IE056Data
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.RejectionMessageP5ViewModel.RejectionMessageP5ViewModelProvider
import viewModels.pagination.ListPaginationViewModel
import views.html.departure.TestOnly.RejectionMessageP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RejectionMessageP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: RejectionMessageP5ViewModelProvider,
  cacheConnector: DepartureCacheConnector,
  view: RejectionMessageP5View
)(implicit val executionContext: ExecutionContext, config: FrontendAppConfig, paginationConfig: PaginationAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(page: Option[Int], departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen messageRetrievalAction[IE056Data](departureId, messageId)).async {
      implicit request =>
        val lrn              = request.referenceNumbers.localReferenceNumber
        val functionalErrors = request.messageData.data.functionalErrors

        cacheConnector.isDeclarationAmendable(lrn.value, functionalErrors.map(_.errorPointer)).flatMap {
          case true =>
            val currentPage = page.getOrElse(1)

            val paginationViewModel = ListPaginationViewModel(
              totalNumberOfItems = functionalErrors.length,
              currentPage = currentPage,
              numberOfItemsPerPage = paginationConfig.departuresNumberOfErrorsPerPage,
              href = controllers.testOnly.routes.RejectionMessageP5Controller.onPageLoad(None, departureId, messageId).url
            )

            val rejectionMessageP5ViewModel =
              viewModelProvider.apply(request.messageData.data.pagedFunctionalErrors(currentPage), lrn.value)

            rejectionMessageP5ViewModel.map(
              viewModel => Ok(view(viewModel, departureId, messageId, paginationViewModel))
            )
          case false =>
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        }
    }

  def onAmend(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen messageRetrievalAction[IE056Data](departureId, messageId)).async {
      implicit request =>
        val lrn    = request.referenceNumbers.localReferenceNumber
        val xPaths = request.messageData.data.functionalErrors.map(_.errorPointer)

        for {
          isDeclarationAmendable <- cacheConnector.isDeclarationAmendable(lrn.value, xPaths)
          handleErrors           <- cacheConnector.handleErrors(lrn.value, xPaths)
          xPathsNonEmpty = xPaths.nonEmpty
        } yield (isDeclarationAmendable, handleErrors, xPathsNonEmpty) match {
          case (true, true, true) =>
            if (request.referenceNumbers.movementReferenceNumber.isDefined) {
              Redirect(config.departureNewLocalReferenceNumberUrl(lrn.value))
            } else {
              Redirect(config.departureFrontendTaskListUrl(lrn.value))
            }
          case _ => Redirect(controllers.routes.ErrorController.technicalDifficulties())
        }
    }

}
