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

import models.LocalReferenceNumber
import models.departureP5._
import models.requests.{DepartureCancelledRequest, IdentifierRequest}
import services.DepartureP5MessageService

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FakeDepartureCancelledAction(departureId: String, messageId: String, departureP5MessageService: DepartureP5MessageService)
    extends DepartureCancelledAction(departureId, messageId, departureP5MessageService) {

  val message: IE009Data = IE009Data(
    IE009MessageData(
      TransitOperationIE009(
        Some("abd123")
      ),
      Invalidation(
        Some(LocalDateTime.now()),
        "0",
        "1",
        Some("some justification")
      ),
      CustomsOfficeOfDeparture(
        "1234"
      )
    )
  )

  override protected def transform[A](request: IdentifierRequest[A]): Future[DepartureCancelledRequest[A]] =
    Future.successful(DepartureCancelledRequest(request, "AB123", LocalReferenceNumber("CD123"), message.data))
}
