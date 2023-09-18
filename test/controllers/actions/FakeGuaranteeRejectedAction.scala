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
import models.requests.{GuaranteeRejectedRequest, IdentifierRequest}
import services.DepartureP5MessageService

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FakeGuaranteeRejectedAction(departureId: String, messageId: String, departureP5MessageService: DepartureP5MessageService)
    extends GuaranteeRejectedAction(departureId = departureId, messageId = messageId, departureP5MessageService = departureP5MessageService) {

  val message: IE055MessageData =
    IE055MessageData(
      TransitOperationIE055("MRNCD3232", LocalDate.now()),
      Seq(
        GuaranteeReference(
          "AB123",
          Seq(InvalidGuaranteeReason("A", None))
        )
      )
    )

  override protected def transform[A](request: IdentifierRequest[A]): Future[GuaranteeRejectedRequest[A]] =
    Future.successful(GuaranteeRejectedRequest(request, "AB123", message))

}
