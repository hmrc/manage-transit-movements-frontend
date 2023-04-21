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
import models.referenceData.{ControlType, CustomsOffice}
import models.requests.{GoodsUnderControlRequest, IdentifierRequest}
import play.api.mvc.Result
import services.{DepartureP5MessageService, ReferenceDataService}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FakeGoodsUnderControlAction(departureId: String, departureP5MessageService: DepartureP5MessageService, referenceDataService: ReferenceDataService)
    extends GoodsUnderControlAction(departureId, departureP5MessageService, referenceDataService) {
  private val controlTypes = Some(Seq(ControlType("42", "Intrusive"), ControlType("44", "Non Intrusive")))

  val message: IE060Data = IE060Data(
    IE060MessageData(
      TransitOperation(Some("CD3232"), Some("AB123"), LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
      CustomsOfficeOfDeparture("22323323"),
      Some(Seq(TypeOfControls("1", "type1", Some("text1")), TypeOfControls("2", "type2", None))),
      Some(Seq(RequestedDocument("3", "doc1", Some("desc1")), RequestedDocument("4", "doc2", None)))
    )
  )

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, GoodsUnderControlRequest[A]]] =
    Future.successful(Right(GoodsUnderControlRequest(request, "AB123", message.data, Some(CustomsOffice("GB000060", "name", Some("999"))), controlTypes)))

}
