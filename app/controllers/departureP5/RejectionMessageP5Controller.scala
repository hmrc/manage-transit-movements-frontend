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
import models.RichCC056CType
import models.departureP5.BusinessRejectionType.DepartureBusinessRejectionType
import models.departureP5.Rejection
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.AmendmentService
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.departure.RejectionMessageP5ViewModel.RejectionMessageP5ViewModelProvider
import viewModels.pagination.PaginationViewModel
import views.html.departureP5.RejectionMessageP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RejectionMessageP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  messageRetrievalAction: DepartureMessageRetrievalActionProvider,
  cc: MessagesControllerComponents,
  viewModelProvider: RejectionMessageP5ViewModelProvider,
  service: AmendmentService,
  view: RejectionMessageP5View
)(implicit val executionContext: ExecutionContext, config: FrontendAppConfig, paginationConfig: PaginationAppConfig)
    extends FrontendController(cc)
    with I18nSupport
    with Logging {

  def onPageLoad(page: Option[Int], departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC056CType](departureId, messageId)).async {
      implicit request =>
        val lrn    = request.referenceNumbers.localReferenceNumber
        val xPaths = request.messageData.xPaths

        service.isRejectionAmendable(lrn, Rejection(departureId, request.messageData)).flatMap {
          case true =>
            val currentPage = page.getOrElse(1)

            val rejectionMessageP5ViewModel = viewModelProvider.apply(
              request.messageData.pagedFunctionalErrors(currentPage),
              lrn,
              DepartureBusinessRejectionType(request.messageData)
            )

            rejectionMessageP5ViewModel.map {
              viewModel =>
                val paginationViewModel = PaginationViewModel(
                  totalNumberOfItems = xPaths.length,
                  currentPage = currentPage,
                  numberOfItemsPerPage = paginationConfig.departuresNumberOfErrorsPerPage,
                  href = controllers.departureP5.routes.RejectionMessageP5Controller.onPageLoad(None, departureId, messageId).url,
                  navigationHiddenText = Some(viewModel.heading)
                )

                Ok(
                  view(
                    viewModel,
                    departureId,
                    messageId,
                    paginationViewModel,
                    request.referenceNumbers.movementReferenceNumber
                  )
                )
            }
          case _ =>
            logger.warn(s"[RejectionMessageP5Controller] Could not proceed with amending $departureId")
            Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
        }
    }

  def onSubmit(departureId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.identify() andThen messageRetrievalAction[CC056CType](departureId, messageId)).async {
      implicit request =>
        val businessRejectionType = DepartureBusinessRejectionType(request.messageData)
        val lrn                   = request.referenceNumbers.localReferenceNumber
        val xPaths                = request.messageData.xPaths
        val mrn                   = request.referenceNumbers.movementReferenceNumber

        service.handleErrors(lrn, Rejection(departureId, businessRejectionType, xPaths)).map {
          case response if is2xx(response.status) =>
            Redirect(service.nextPage(businessRejectionType, lrn, mrn))
          case _ =>
            Redirect(controllers.routes.ErrorController.technicalDifficulties())
        }
    }

}
