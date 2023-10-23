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
import models.LocalReferenceNumber
import models.departureP5.DepartureMessageType.AllocatedMRN
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.DepartureP5MessageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.RejectionMessageP5ViewModel.RejectionMessageP5ViewModelProvider
import viewModels.pagination.ListPaginationViewModel
import views.html.departure.TestOnly.RejectionMessageP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RejectionMessageP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  rejectionMessageAction: DepartureRejectionMessageActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: RejectionMessageP5ViewModelProvider,
  cacheConnector: DepartureCacheConnector,
  departureP5MessageService: DepartureP5MessageService,
  view: RejectionMessageP5View
)(implicit val executionContext: ExecutionContext, config: FrontendAppConfig, paginationConfig: PaginationAppConfig)
    extends FrontendController(cc)
    with I18nSupport {

  def buildView(page: Option[Int], departureId: String, localReferenceNumber: LocalReferenceNumber, isAmendmentJourney: Boolean): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen rejectionMessageAction(departureId, localReferenceNumber)).async {
      implicit request =>
        if (request.isDeclarationAmendable || isAmendmentJourney) { //TODO: Additional MRN caption for amendment once merged in

          val currentPage = page.getOrElse(1)

          val paginationViewModel = ListPaginationViewModel(
            totalNumberOfItems = request.ie056MessageData.functionalErrors.length,
            currentPage = currentPage,
            numberOfItemsPerPage = paginationConfig.departuresNumberOfErrorsPerPage,
            href = controllers.testOnly.routes.RejectionMessageP5Controller.onPageLoad(None, departureId, localReferenceNumber).url
          )

          val rejectionMessageP5ViewModel =
            viewModelProvider.apply(request.ie056MessageData.pagedFunctionalErrors(currentPage), localReferenceNumber.value, isAmendmentJourney)

          rejectionMessageP5ViewModel.map(
            viewModel => Ok(view(viewModel, departureId, paginationViewModel, localReferenceNumber, request.isDeclarationAmendable))
          )
        } else {
          Future.successful(
            Redirect(controllers.routes.SessionExpiredController.onPageLoad())
          )
        }
    }

  def onPageLoad(page: Option[Int], departureId: String, localReferenceNumber: LocalReferenceNumber): Action[AnyContent] =
    buildView(page, departureId, localReferenceNumber, isAmendmentJourney = false)

  def amendmentRejectionOnPageLoad(page: Option[Int], departureId: String, localReferenceNumber: LocalReferenceNumber): Action[AnyContent] =
    buildView(page, departureId, localReferenceNumber, isAmendmentJourney = true)

  def onAmend(departureId: String, localReferenceNumber: LocalReferenceNumber): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen rejectionMessageAction(departureId, localReferenceNumber)).async {
      implicit request =>
        val xPaths = request.ie056MessageData.functionalErrors.map(_.errorPointer)
        if (request.isDeclarationAmendable && xPaths.nonEmpty) {
          cacheConnector.handleErrors(localReferenceNumber.value, xPaths).flatMap {
            case true =>
              departureP5MessageService.getSpecificMessageMetaData(departureId, AllocatedMRN).map {
                case Some(_) => Redirect(config.departureNewLocalReferenceNumberUrl(localReferenceNumber.value))
                case None    => Redirect(config.departureFrontendTaskListUrl(localReferenceNumber.value))
              }
            case false =>
              Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
          }
        } else {
          Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
        }
    }

}
