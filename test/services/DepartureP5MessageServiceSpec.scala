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

package services

import base.SpecBase
import cats.data.NonEmptyList
import connectors.DepartureMovementP5Connector
import models.departureP5.{
  DepartureMessage,
  DepartureMessageType,
  DepartureMovement,
  DepartureMovementAndMessage,
  DepartureMovements,
  LocalReferenceNumber,
  MessagesForDepartureMovement
}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}

import scala.concurrent.ExecutionContext.Implicits.global
import java.time.LocalDateTime
import scala.concurrent.Future

class DepartureP5MessageServiceSpec extends SpecBase {

  val mockConnector: DepartureMovementP5Connector = mock[DepartureMovementP5Connector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  val departureP5MessageService = new DepartureP5MessageService(mockConnector)

  "getMessagesForAllMovements" - {

    val dateTimeNow = LocalDateTime.now(clock)

    val departureMovements = DepartureMovements(
      Seq(
        DepartureMovement(
          "AB123",
          Some("MRN"),
          dateTimeNow,
          "location"
        )
      )
    )

    "must return departure movements with messages with an LRN" in {

      val messagesForMovement =
        MessagesForDepartureMovement(
          NonEmptyList(
            DepartureMessage(
              dateTimeNow,
              DepartureMessageType.DepartureNotification,
              "body/path/1",
              Nil
            ),
            List(
              DepartureMessage(
                dateTimeNow,
                DepartureMessageType.GoodsUnderControl,
                "body/path/2",
                Nil
              )
            )
          )
        )

      when(mockConnector.getMessagesForMovement(any())(any())).thenReturn(
        Future.successful(messagesForMovement)
      )

      when(mockConnector.getLRN(messagesForMovement.messages.head.bodyPath)).thenReturn(
        Future.successful(LocalReferenceNumber("lrn123"))
      )

      val result = departureP5MessageService.getMessagesForAllMovements(departureMovements).futureValue

      val expectedResult = Seq(
        DepartureMovementAndMessage(
          departureMovements.departureMovements.head,
          messagesForMovement,
          "lrn123"
        )
      )

      result mustBe expectedResult
    }

    "must return departure movements with messages without an LRN" in {

      val messagesForMovement =
        MessagesForDepartureMovement(
          NonEmptyList(
            DepartureMessage(
              dateTimeNow,
              DepartureMessageType.GoodsUnderControl,
              "body/path/2",
              Nil
            ),
            List.empty
          )
        )

      when(mockConnector.getMessagesForMovement(any())(any())).thenReturn(
        Future.successful(messagesForMovement)
      )

      val result = departureP5MessageService.getMessagesForAllMovements(departureMovements).futureValue

      val expectedResult = Seq(
        DepartureMovementAndMessage(
          departureMovements.departureMovements.head,
          messagesForMovement,
          ""
        )
      )

      result mustBe expectedResult
    }

  }

}
