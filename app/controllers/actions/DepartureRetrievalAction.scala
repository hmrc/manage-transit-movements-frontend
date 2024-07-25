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

import models.requests.{DepartureRetrievalRequest, IdentifierRequest}
import play.api.mvc.ActionTransformer
import services.DepartureP5MessageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureRetrievalActionProvider @Inject() (
  departureP5MessageService: DepartureP5MessageService
)(implicit ec: ExecutionContext) {

  def apply(departureId: String): ActionTransformer[IdentifierRequest, DepartureRetrievalRequest] =
    new DepartureRetrievalAction(departureId, departureP5MessageService)
}

class DepartureRetrievalAction(departureId: String, departureP5MessageService: DepartureP5MessageService)(implicit
  protected val executionContext: ExecutionContext
) extends ActionTransformer[IdentifierRequest, DepartureRetrievalRequest] {

  override protected def transform[A](request: IdentifierRequest[A]): Future[DepartureRetrievalRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    departureP5MessageService
      .getDepartureReferenceNumbers(departureId)
      .map(DepartureRetrievalRequest(request, request.eoriNumber, _))
  }
}
