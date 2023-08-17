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

import cats.data.OptionT
import connectors.DepartureCacheConnector
import controllers.routes
import models.LocalReferenceNumber
import models.departureP5.IE056Data
import models.requests.{DepartureRejectionMessageRequest, IdentifierRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import services.DepartureP5MessageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureRejectionMessageActionProvider @Inject() (departureP5MessageService: DepartureP5MessageService, cacheConnector: DepartureCacheConnector)(implicit
  ec: ExecutionContext
) {

  def apply(departureId: String,
            localReferenceNumber: LocalReferenceNumber,
            messageId: String
  ): ActionRefiner[IdentifierRequest, DepartureRejectionMessageRequest] =
    new DepartureRejectionMessageAction(departureId, localReferenceNumber, messageId, departureP5MessageService, cacheConnector)
}

class DepartureRejectionMessageAction(
  departureId: String,
  localReferenceNumber: LocalReferenceNumber,
  messageId: String,
  departureP5MessageService: DepartureP5MessageService,
  cacheConnector: DepartureCacheConnector
)(implicit protected val executionContext: ExecutionContext)
    extends ActionRefiner[IdentifierRequest, DepartureRejectionMessageRequest] {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, DepartureRejectionMessageRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    (for {
      ie056 <- OptionT(departureP5MessageService.getMessageWithMessageId[IE056Data](departureId, messageId))
      xPaths = ie056.data.functionalErrors.map(_.errorPointer)
      isDeclarationAmendable <- OptionT.liftF(cacheConnector.isDeclarationAmendable(localReferenceNumber.value, xPaths))
    } yield DepartureRejectionMessageRequest(request, request.eoriNumber, ie056.data, isDeclarationAmendable, localReferenceNumber.value))
      .toRight(Redirect(routes.ErrorController.technicalDifficulties()))
      .value

  }
}
