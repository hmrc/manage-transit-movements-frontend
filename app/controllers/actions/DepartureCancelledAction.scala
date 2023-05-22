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
import controllers.routes
import models.departureP5.DepartureMessageType.CancellationDecision
import models.departureP5.IE009Data
import models.requests.{DepartureCancelledRequest, IdentifierRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import services.DepartureP5MessageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureCancelledActionProvider @Inject() (departureP5MessageService: DepartureP5MessageService)(implicit
  ec: ExecutionContext
) {

  def apply(departureId: String): ActionRefiner[IdentifierRequest, DepartureCancelledRequest] =
    new DepartureCancelledAction(departureId, departureP5MessageService)
}

class DepartureCancelledAction(departureId: String, departureP5MessageService: DepartureP5MessageService)(implicit
  protected val executionContext: ExecutionContext
) extends ActionRefiner[IdentifierRequest, DepartureCancelledRequest] {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, DepartureCancelledRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    (for {
      ie009 <- OptionT(departureP5MessageService.getMessage[IE009Data](departureId, CancellationDecision))
      lrn   <- OptionT(departureP5MessageService.getLRNFromDeclarationMessage(departureId)) // TODO: Remove once LRN is exposed to use in metadata
    } yield DepartureCancelledRequest(request, request.eoriNumber, ie009.data, lrn))
      .toRight(Redirect(routes.ErrorController.technicalDifficulties()))
      .value

  }
}
