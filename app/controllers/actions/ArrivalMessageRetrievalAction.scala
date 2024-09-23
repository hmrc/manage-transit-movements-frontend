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

import models.requests.{IdentifierRequest, MessageRetrievalRequestProvider}
import play.api.mvc.ActionTransformer
import scalaxb.XMLFormat
import services.ArrivalP5MessageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ArrivalMessageRetrievalActionProvider @Inject() (arrivalP5MessageService: ArrivalP5MessageService)(implicit
  ec: ExecutionContext
) {

  def apply[B](arrivalId: String, messageId: String)(implicit
    format: XMLFormat[B]
  ): ActionTransformer[IdentifierRequest, MessageRetrievalRequestProvider[B]#ArrivalMessageRetrievalRequest] =
    new ArrivalMessageRetrievalAction(arrivalId, messageId, arrivalP5MessageService)

}

class ArrivalMessageRetrievalAction[B](arrivalId: String, messageId: String, arrivalP5MessageService: ArrivalP5MessageService)(implicit
  protected val executionContext: ExecutionContext,
  protected val format: XMLFormat[B]
) extends ActionTransformer[IdentifierRequest, MessageRetrievalRequestProvider[B]#ArrivalMessageRetrievalRequest] {

  override protected def transform[A](request: IdentifierRequest[A]): Future[MessageRetrievalRequestProvider[B]#ArrivalMessageRetrievalRequest[A]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    arrivalP5MessageService.getMessage[B](arrivalId, messageId).map {
      data =>
        new MessageRetrievalRequestProvider[B].ArrivalMessageRetrievalRequest(
          request,
          request.eoriNumber,
          data
        )
    }
  }

}
