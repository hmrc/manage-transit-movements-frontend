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

import models.departureP5.IE060Data
import models.requests.{GoodsUnderControlRequest, IdentifierRequest}
import play.api.mvc.{ActionRefiner, ActionTransformer}
import services.{DepartureP5MessageService, ReferenceDataService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoodsUnderControlActionProvider @Inject() (departureP5MessageService: DepartureP5MessageService, referenceDataService: ReferenceDataService)(implicit
  ec: ExecutionContext
) {

  def apply(departureId: String, messageId: String): ActionRefiner[IdentifierRequest, GoodsUnderControlRequest] =
    new GoodsUnderControlAction(departureId, messageId, departureP5MessageService, referenceDataService)
}

class GoodsUnderControlAction(
  departureId: String,
  messageId: String,
  departureP5MessageService: DepartureP5MessageService,
  referenceDataService: ReferenceDataService
)(implicit
  protected val executionContext: ExecutionContext
) extends ActionTransformer[IdentifierRequest, GoodsUnderControlRequest] {

  override protected def transform[A](request: IdentifierRequest[A]): Future[GoodsUnderControlRequest[A]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    for {
      ie060      <- departureP5MessageService.getMessageWithMessageId[IE060Data](departureId, messageId)
      custOffice <- referenceDataService.getCustomsOffice(ie060.data.CustomsOfficeOfDeparture.referenceNumber)
      lrn        <- departureP5MessageService.getLRN(departureId)
    } yield GoodsUnderControlRequest(request, request.eoriNumber, ie060.data, lrn, custOffice)
  }
}
