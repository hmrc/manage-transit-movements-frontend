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

import connectors.DepartureCacheConnector
import models.departureP5.IE056Data
import models.requests.{DepartureRejectionMessageRequest, IdentifierRequest}
import play.api.mvc.ActionTransformer
import services.DepartureP5MessageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureRejectionMessageActionProvider @Inject() (departureP5MessageService: DepartureP5MessageService, cacheConnector: DepartureCacheConnector)(implicit
  ec: ExecutionContext
) {

  def apply(departureId: String, messageId: String): ActionTransformer[IdentifierRequest, DepartureRejectionMessageRequest] =
    new DepartureRejectionMessageAction(departureId, messageId, departureP5MessageService, cacheConnector)
}

class DepartureRejectionMessageAction(
  departureId: String,
  messageId: String,
  departureP5MessageService: DepartureP5MessageService,
  cacheConnector: DepartureCacheConnector
)(implicit protected val executionContext: ExecutionContext)
    extends ActionTransformer[IdentifierRequest, DepartureRejectionMessageRequest] {

  override protected def transform[A](request: IdentifierRequest[A]): Future[DepartureRejectionMessageRequest[A]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    for {
      ie056                  <- departureP5MessageService.getMessageWithMessageId[IE056Data](departureId, messageId)
      refNumbers             <- departureP5MessageService.getDepartureReferenceNumbers(departureId)
      isDeclarationAmendable <- cacheConnector.isDeclarationAmendable(refNumbers.localReferenceNumber.value, ie056.data.functionalErrors.map(_.errorPointer))
    } yield DepartureRejectionMessageRequest(request, request.eoriNumber, ie056.data, isDeclarationAmendable, refNumbers)
  }
}
