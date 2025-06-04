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

package controllers.departureP5.drafts

import controllers.actions.{Actions, LockActionProvider}
import forms.YesNoFormProvider
import models.LocalReferenceNumber
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.DraftDepartureService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.departureP5.drafts.DeleteDraftDepartureYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeleteDraftDepartureYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  formProvider: YesNoFormProvider,
  actions: Actions,
  lockAction: LockActionProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeleteDraftDepartureYesNoView,
  draftDepartureService: DraftDepartureService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("departure.drafts.deleteDraftDepartureYesNo")

  def onPageLoad(lrn: LocalReferenceNumber, search: Option[String], pageNumber: Int, drafts: Int): Action[AnyContent] =
    (Action andThen actions.identify() andThen lockAction(lrn.value)) {
      implicit request =>
        Ok(view(form, lrn, pageNumber, drafts, search))
    }

  def onSubmit(lrn: LocalReferenceNumber, search: Option[String], pageNumber: Int, drafts: Int): Action[AnyContent] =
    (Action andThen actions.identify() andThen lockAction(lrn.value)).async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, pageNumber, drafts, search))),
            {
              case true =>
                draftDepartureService.deleteDraftDeparture(lrn.value) map {
                  case response if response.status == OK =>
                    val redirectPageNumber = pageNumber match {
                      case 1                         => None
                      case pageNumber if drafts == 1 => Some(pageNumber - 1)
                      case pageNumber                => Some(pageNumber)
                    }

                    Redirect(controllers.departureP5.drafts.routes.DashboardController.onPageLoad(search, redirectPageNumber))
                  case _ =>
                    Redirect(controllers.routes.ErrorController.internalServerError())
                }
              case false =>
                Future.successful(Redirect(controllers.departureP5.drafts.routes.DashboardController.onPageLoad(search, Some(pageNumber))))
            }
          )
    }

}
