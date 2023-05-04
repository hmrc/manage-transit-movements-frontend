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
import models.departureP5.DepartureMessageType.GoodsUnderControl
import models.departureP5.IE060Data
import models.requests.{GoodsUnderControlRequest, IdentifierRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import services.{DepartureP5MessageService, ReferenceDataService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoodsUnderControlActionProvider @Inject() (departureP5MessageService: DepartureP5MessageService, referenceDataService: ReferenceDataService)(implicit
  ec: ExecutionContext
) {

  def apply(departureId: String): ActionRefiner[IdentifierRequest, GoodsUnderControlRequest] =
    new GoodsUnderControlAction(departureId, departureP5MessageService, referenceDataService)
}

class GoodsUnderControlAction(departureId: String, departureP5MessageService: DepartureP5MessageService, referenceDataService: ReferenceDataService)(implicit
  protected val executionContext: ExecutionContext
) extends ActionRefiner[IdentifierRequest, GoodsUnderControlRequest] {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, GoodsUnderControlRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    (for {
      ie060 <- OptionT(departureP5MessageService.getMessage[IE060Data](departureId, GoodsUnderControl))
      cust  <- OptionT.liftF(referenceDataService.getCustomsOfficeByCode(code = ie060.data.CustomsOfficeOfDeparture.referenceNumber))
    } yield GoodsUnderControlRequest(request, request.eoriNumber, ie060.data, cust))
      .toRight(Redirect(routes.ErrorController.technicalDifficulties()))
      .value

  }
}
