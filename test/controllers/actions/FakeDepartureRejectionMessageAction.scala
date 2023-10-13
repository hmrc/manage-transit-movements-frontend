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

import connectors.DepartureCacheConnector
import models.RejectionType
import models.LocalReferenceNumber
import models.departureP5._
import models.requests.{DepartureRejectionMessageRequest, IdentifierRequest}
import play.api.mvc.Result
import services.DepartureP5MessageService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FakeDepartureRejectionMessageAction(
  departureId: String,
  messageId: String,
  departureP5MessageService: DepartureP5MessageService,
  departureCacheConnector: DepartureCacheConnector
) extends DepartureRejectionMessageAction(departureId, messageId, departureP5MessageService, departureCacheConnector) {

  val message: IE056Data = IE056Data(
    IE056MessageData(
      TransitOperationIE056(Some("MRNCD3232"), Some("LRNAB123"), RejectionType.DeclarationRejection),
      CustomsOfficeOfDeparture("AB123"),
      Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
    )
  )

  override protected def transform[A](request: IdentifierRequest[A]): Future[DepartureRejectionMessageRequest[A]] =
    Future.successful(DepartureRejectionMessageRequest(request, "AB123", message.data, isDeclarationAmendable = true, LocalReferenceNumber("CD123")))
}
