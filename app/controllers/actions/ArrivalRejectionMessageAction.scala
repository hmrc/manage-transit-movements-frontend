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

package controllers.actions

import controllers.routes
import models.arrivalP5.IE057Data
import models.requests.{ArrivalRejectionMessageRequest, IdentifierRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import services.ArrivalP5MessageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ArrivalRejectionMessageActionProvider @Inject() (arrivalP5MessageService: ArrivalP5MessageService)(implicit ec: ExecutionContext) {

  def apply(arrivalId: String, messageId: String): ActionRefiner[IdentifierRequest, ArrivalRejectionMessageRequest] =
    new ArrivalRejectionMessageAction(arrivalId, messageId, arrivalP5MessageService)
}

class ArrivalRejectionMessageAction(arrivalId: String, messageId: String, arrivalP5MessageService: ArrivalP5MessageService)(implicit
  protected val executionContext: ExecutionContext
) extends ActionRefiner[IdentifierRequest, ArrivalRejectionMessageRequest] {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, ArrivalRejectionMessageRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    arrivalP5MessageService
      .getMessageWithMessageId[IE057Data](arrivalId, messageId)
      .map {
        rejectionMessage =>
          Right(ArrivalRejectionMessageRequest(request, request.eoriNumber, rejectionMessage.data))
      }
      .recover {
        _ =>
          Left(Redirect(routes.ErrorController.technicalDifficulties()))
      }
  }
}
