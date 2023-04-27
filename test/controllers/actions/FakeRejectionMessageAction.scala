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

import models.departureP5._
import models.referenceData.CustomsOffice
import models.requests.{GoodsUnderControlRequest, IdentifierRequest, RejectionMessageRequest}
import play.api.mvc.Result
import services.{DepartureP5MessageService, ReferenceDataService}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FakeRejectionMessageAction(departureId: String, departureP5MessageService: DepartureP5MessageService, referenceDataService: ReferenceDataService)
    extends RejectionMessageAction(departureId, departureP5MessageService, referenceDataService) {

  val message: IE056Data = IE056Data(
    IE056MessageData(
      TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123")),
      Some(Seq(FunctionalError("1", "12", "Codelist violation"), FunctionalError("2", "14", "Rule violation")))
    )
  )

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, RejectionMessageRequest[A]]] =
    Future.successful(Right(RejectionMessageRequest(request, "AB123", message.data)))

}
