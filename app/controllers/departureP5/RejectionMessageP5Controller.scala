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
import connectors.DepartureCacheConnector
import controllers.actions._
import generated.CC056CType
import models.RichCC056CType
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.RejectionMessageP5ViewModel.RejectionMessageP5ViewModelProvider
import viewModels.pagination.ListPaginationViewModel
import views.html.departureP5.RejectionMessageP5View

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

  def onPageLoad(page: Option[Int], departureId: String, messageId: String, isAmendmentJourney: Option[Boolean]): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen messageRetrievalAction[CC056CType](departureId, messageId)).async {
      implicit request =>
        val lrn              = request.referenceNumbers.localReferenceNumber
        val functionalErrors = request.messageData.FunctionalError

        val isDataValid: Future[Boolean] = if (isAmendmentJourney.getOrElse(false)) {
          cacheConnector.doesDeclarationExist(lrn)
        } else {
          cacheConnector.isDeclarationAmendable(lrn, functionalErrors.map(_.errorPointer))
        }

        isDataValid.flatMap {
          case true =>
            val currentPage = page.getOrElse(1)

            val paginationViewModel = ListPaginationViewModel(
              totalNumberOfItems = functionalErrors.length,
              currentPage = currentPage,
              numberOfItemsPerPage = paginationConfig.departuresNumberOfErrorsPerPage,
              href = controllers.departureP5.routes.RejectionMessageP5Controller.onPageLoad(None, departureId, messageId, None).url,
              additionalParams = Seq(("isAmendmentJourney", isAmendmentJourney.map(_.toString).getOrElse("false")))
            )

            val rejectionMessageP5ViewModel =
              viewModelProvider.apply(request.messageData.pagedFunctionalErrors(currentPage), lrn, isAmendmentJourney.getOrElse(false))

            rejectionMessageP5ViewModel.map(
              viewModel =>
                Ok(
                  view(
                    viewModel,
                    departureId,
                    messageId,
                    paginationViewModel,
                    isAmendmentJourney.getOrElse(false),
                    request.referenceNumbers.movementReferenceNumber
                  )
                )
            )
          case _ =>
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        }
    }

  def onAmend(departureId: String, messageId: String, isAmendmentJourney: Boolean): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen messageRetrievalAction[CC056CType](departureId, messageId)).async {
      implicit request =>
        val lrn         = request.referenceNumbers.localReferenceNumber
        lazy val xPaths = request.messageData.FunctionalError.map(_.errorPointer)

        if (isAmendmentJourney) {
          for {
            doesCacheExistForLrn  <- cacheConnector.doesDeclarationExist(lrn)
            handleAmendmentErrors <- cacheConnector.handleAmendmentErrors(lrn, xPaths)
          } yield (doesCacheExistForLrn, handleAmendmentErrors) match {
            case (true, true) =>
              Redirect(config.departureAmendmentUrl(lrn, departureId))
            case _ =>
              Redirect(controllers.routes.ErrorController.technicalDifficulties())
          }
        } else {
          for {
            isDeclarationAmendable <- cacheConnector.isDeclarationAmendable(lrn, xPaths)
            handleErrors           <- cacheConnector.handleErrors(lrn, xPaths)
          } yield (isDeclarationAmendable, handleErrors) match {
            case (true, true) =>
              if (xPaths.nonEmpty) {
                if (request.referenceNumbers.movementReferenceNumber.isDefined) {
                  Redirect(config.departureNewLocalReferenceNumberUrl(lrn))
                } else {
                  Redirect(config.departureFrontendTaskListUrl(lrn))
                }
              } else {
                Redirect(controllers.routes.ErrorController.technicalDifficulties())
              }
            case _ =>
              Redirect(controllers.routes.ErrorController.technicalDifficulties())
          }
        }
    }
}
