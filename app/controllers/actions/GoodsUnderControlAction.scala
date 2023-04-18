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
import models.DepartureId
import models.requests.{GoodsUnderControlRequest, IdentifierRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import services.DepartureP5MessageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoodsUnderControlActionProvider @Inject() (departureP5MessageService: DepartureP5MessageService)(implicit ec: ExecutionContext) {

  def apply(departureId: String): ActionRefiner[IdentifierRequest, GoodsUnderControlRequest] =
    new GoodsUnderControlAction(departureId, departureP5MessageService)
}

class GoodsUnderControlAction(departureId: String, departureP5MessageService: DepartureP5MessageService)(implicit
  protected val executionContext: ExecutionContext
) extends ActionRefiner[IdentifierRequest, GoodsUnderControlRequest] {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, GoodsUnderControlRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    OptionT(departureP5MessageService.getGoodsUnderControl(departureId))
      .map {
        goodsUnderControl =>
          GoodsUnderControlRequest(request, request.eoriNumber, goodsUnderControl.data)
      }
      .toRight(Redirect(routes.ErrorController.technicalDifficulties()))
      .value
  }
}
