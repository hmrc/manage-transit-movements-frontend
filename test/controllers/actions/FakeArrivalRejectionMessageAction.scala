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

import models.arrivalP5.{IE057Data, IE057MessageData, TransitOperationIE057}
import models.departureP5._
import models.requests.{ArrivalRejectionMessageRequest, IdentifierRequest}
import play.api.mvc.Result
import services.ArrivalP5MessageService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FakeArrivalRejectionMessageAction(
  arrivalId: String,
  arrivalP5MessageService: ArrivalP5MessageService
) extends ArrivalRejectionMessageAction(arrivalId, arrivalP5MessageService) {

  val message: IE057Data = IE057Data(
    IE057MessageData(
      TransitOperationIE057("MRNCD3232"),
      Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
    )
  )

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, ArrivalRejectionMessageRequest[A]]] =
    Future.successful(Right(ArrivalRejectionMessageRequest(request, "MRNCD3232", message.data)))

}
