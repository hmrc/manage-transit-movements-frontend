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

import models.departureP5.IE051Data
import models.requests.{GoodsNotReleasedRequest, IdentifierRequest}
import play.api.mvc.{ActionRefiner, ActionTransformer}
import services.DepartureP5MessageService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoodsNotReleasedActionProvider @Inject() (departureP5MessageService: DepartureP5MessageService)(implicit
  ec: ExecutionContext
) {

  def apply(departureId: String, messageId: String): ActionRefiner[IdentifierRequest, GoodsNotReleasedRequest] =
    new GoodsNotReleasedAction(departureId, messageId, departureP5MessageService)
}

class GoodsNotReleasedAction(departureId: String, messageId: String, departureP5MessageService: DepartureP5MessageService)(implicit
  protected val executionContext: ExecutionContext
) extends ActionTransformer[IdentifierRequest, GoodsNotReleasedRequest] {

  override protected def transform[A](request: IdentifierRequest[A]): Future[GoodsNotReleasedRequest[A]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    departureP5MessageService.getMessageWithMessageId[IE051Data](departureId, messageId).map {
      ie051 =>
        GoodsNotReleasedRequest(request, request.eoriNumber, ie051.data)
    }
  }
}
