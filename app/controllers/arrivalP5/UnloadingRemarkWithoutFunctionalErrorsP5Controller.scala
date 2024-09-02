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

package controllers.arrivalP5

import config.FrontendAppConfig
import controllers.actions._
import generated.CC057CType
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ReferenceDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewModels.P5.arrival.UnloadingRemarkWithoutFunctionalErrorsP5ViewModel._
import views.html.arrivalP5.UnloadingRemarkWithoutFunctionalErrorsP5View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarkWithoutFunctionalErrorsP5Controller @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  cc: MessagesControllerComponents,
  messageRetrievalAction: ArrivalMessageRetrievalActionProvider,
  viewModelProvider: UnloadingRemarkWithoutFunctionalErrorsP5ViewModelProvider,
  view: UnloadingRemarkWithoutFunctionalErrorsP5View,
  referenceDataService: ReferenceDataService,
  config: FrontendAppConfig
)(implicit val executionContext: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(arrivalId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen messageRetrievalAction[CC057CType](arrivalId, messageId)).async {
      implicit request =>
        val functionalErrors       = request.messageData.FunctionalError
        val customsOfficeReference = request.messageData.CustomsOfficeOfDestinationActual.referenceNumber

        if (functionalErrors.isEmpty) {
          referenceDataService.getCustomsOffice(customsOfficeReference).map {
            customsOffice =>
              Ok(
                view(
                  viewModelProvider.apply(
                    request.messageData.TransitOperation.MRN,
                    customsOffice
                  ),
                  arrivalId,
                  messageId
                )
              )
          }
        } else {
          Future.successful(Redirect(controllers.routes.ErrorController.technicalDifficulties()))
        }
    }

  def onSubmit(arrivalId: String, messageId: String): Action[AnyContent] =
    (Action andThen actions.checkP5Switch() andThen messageRetrievalAction[CC057CType](arrivalId, messageId)) {
      _ => Redirect(config.p5UnloadingStart(arrivalId, messageId))
    }
}
