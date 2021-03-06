/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.unloading

import config.FrontendAppConfig
import controllers.actions._
import javax.inject.Inject
import models.ArrivalId
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.ArrivalMessageService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext

class UnloadingRemarksXmlNegativeAcknowledgementController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cc: MessagesControllerComponents,
  val frontendAppConfig: FrontendAppConfig,
  arrivalMessageService: ArrivalMessageService,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(arrivalId: ArrivalId): Action[AnyContent] = identify.async {
    implicit request =>
      arrivalMessageService.getXMLSubmissionNegativeAcknowledgementMessage(arrivalId).flatMap {
        case Some(rejectionMessage) =>
          val json = Json.obj(
            "contactUrl"                 -> frontendAppConfig.nctsEnquiriesUrl,
            "declareUnloadingRemarksUrl" -> frontendAppConfig.declareUnloadingRemarksUrl(arrivalId),
            "functionalError"            -> rejectionMessage.error
          )

          renderer.render("unloadingRemarksXmlNegativeAcknowledgement.njk", json).map(Ok(_))
        case _ =>
          val json = Json.obj("nctsEnquiries" -> frontendAppConfig.nctsEnquiriesUrl)
          renderer.render("technicalDifficulties.njk", json).map(InternalServerError(_))
      }
  }
}
