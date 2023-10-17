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
import services.DepartureP5MessageService
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MessageRetrievalActionProvider @Inject() (departureP5MessageService: DepartureP5MessageService)(implicit
  ec: ExecutionContext
) {

  def apply[B](departureId: String, messageId: String)(implicit
    reads: HttpReads[B]
  ): ActionTransformer[IdentifierRequest, MessageRetrievalRequestProvider[B]#MessageRetrievalRequest] =
    new MessageRetrievalAction(departureId, messageId, departureP5MessageService)
}

class MessageRetrievalAction[B](departureId: String, messageId: String, departureP5MessageService: DepartureP5MessageService)(
  implicit protected val executionContext: ExecutionContext,
  implicit protected val reads: HttpReads[B]
) extends ActionTransformer[IdentifierRequest, MessageRetrievalRequestProvider[B]#MessageRetrievalRequest] {

  override protected def transform[A](request: IdentifierRequest[A]): Future[MessageRetrievalRequestProvider[B]#MessageRetrievalRequest[A]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    for {
      data       <- departureP5MessageService.getMessageWithMessageId[B](departureId, messageId)
      refNumbers <- departureP5MessageService.getDepartureReferenceNumbers(departureId)
    } yield new MessageRetrievalRequestProvider[B].MessageRetrievalRequest(
      request,
      request.eoriNumber,
      data,
      refNumbers
    )
  }
}
