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
import connectors.ArrivalMovementP5Connector
import models.arrivalP5.ArrivalMessageType.RejectionFromOfficeOfDestination
import models.arrivalP5._
import models.departureP5.FunctionalError
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import uk.gov.hmrc.http.HttpReads.Implicits._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ArrivalP5MessageServiceSpec extends SpecBase {

  val mockConnector: ArrivalMovementP5Connector = mock[ArrivalMovementP5Connector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  val arrivalP5MessageService = new ArrivalP5MessageService(mockConnector)

  "ArrivalP5MessageService" - {

    "getMessagesForAllMovements" - {

      val dateTime = LocalDateTime.now(clock)

      "must return a sequence of ArrivalMovementAndMessage when given ArrivalMovements" - {
        "when there isn't a IE057" in {

          val movement1 = ArrivalMovement("arrivalId1", "movementReferenceNo1", dateTime, "/locationUrl1")
          val movement2 = ArrivalMovement("arrivalId2", "movementReferenceNo2", dateTime, "/locationUrl2")

          val message1 = ArrivalMessage(dateTime.minusDays(1), ArrivalMessageType.ArrivalNotification)
          val message2 = ArrivalMessage(dateTime, ArrivalMessageType.UnloadingPermission)

          val messages1 = MessagesForArrivalMovement(NonEmptyList(message1, List(message2)))
          val messages2 = MessagesForArrivalMovement(NonEmptyList(message1, List(message2)))

          val movementAndMessages1 = ArrivalMovementAndMessage(movement1, messages1, 0)
          val movementAndMessages2 = ArrivalMovementAndMessage(movement2, messages2, 0)

          when(mockConnector.getMessagesForMovement(eqTo("/locationUrl1"))(any())).thenReturn(Future.successful(messages1))
          when(mockConnector.getMessagesForMovement(eqTo("/locationUrl2"))(any())).thenReturn(Future.successful(messages2))

          when(mockConnector.getMessageMetaData(eqTo(movement1.arrivalId))(any(), any())).thenReturn(Future.successful(ArrivalMessages(Nil)))
          when(mockConnector.getMessageMetaData(eqTo(movement2.arrivalId))(any(), any())).thenReturn(Future.successful(ArrivalMessages(Nil)))

          val arrivalMovements = ArrivalMovements(arrivalMovements = Seq(movement1, movement2), totalCount = 2)

          val expectedResult = Seq(
            movementAndMessages1,
            movementAndMessages2
          )

          arrivalP5MessageService.getMessagesForAllMovements(arrivalMovements).futureValue mustBe expectedResult
        }
        "when there is a IE057" in {

          val movement1 = ArrivalMovement("arrivalId1", "movementReferenceNo1", dateTime, "/locationUrl1")
          val movement2 = ArrivalMovement("arrivalId2", "movementReferenceNo2", dateTime, "/locationUrl2")

          val message1 = ArrivalMessage(dateTime.minusDays(1), ArrivalMessageType.ArrivalNotification)
          val message2 = ArrivalMessage(dateTime, ArrivalMessageType.UnloadingPermission)
          val message3 = ArrivalMessage(dateTime, ArrivalMessageType.RejectionFromOfficeOfDestination)

          val messages1 = MessagesForArrivalMovement(NonEmptyList(message1, List(message3)))
          val messages2 = MessagesForArrivalMovement(NonEmptyList(message1, List(message2, message3)))

          val movementAndMessages1 = ArrivalMovementAndMessage(movement1, messages1, 2)
          val movementAndMessages2 = ArrivalMovementAndMessage(movement2, messages2, 2)

          when(mockConnector.getMessagesForMovement(eqTo("/locationUrl1"))(any())).thenReturn(Future.successful(messages1))
          when(mockConnector.getMessagesForMovement(eqTo("/locationUrl2"))(any())).thenReturn(Future.successful(messages2))

          val arrivalMessages1 = ArrivalMessages(
            List(
              ArrivalMessageMetaData(
                LocalDateTime.parse("2022-11-10T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
                ArrivalMessageType.RejectionFromOfficeOfDestination,
                s"movements/arrivals/${movement1.arrivalId}/message/634982098f02f00a"
              )
            )
          )

          val arrivalMessages2 = ArrivalMessages(
            List(
              ArrivalMessageMetaData(
                LocalDateTime.parse("2022-11-10T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
                ArrivalMessageType.RejectionFromOfficeOfDestination,
                s"movements/arrivals/${movement2.arrivalId}/message/634982098f02f00b"
              )
            )
          )

          when(mockConnector.getMessageMetaData(eqTo(movement1.arrivalId))(any(), any())).thenReturn(Future.successful(arrivalMessages1))
          when(mockConnector.getMessageMetaData(eqTo(movement2.arrivalId))(any(), any())).thenReturn(Future.successful(arrivalMessages2))

          val ie057: IE057Data = IE057Data(
            IE057MessageData(
              TransitOperationIE057("CD3232"),
              CustomsOfficeOfDestinationActual("1234"),
              Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
            )
          )

          when(mockConnector.getSpecificMessage[IE057Data](eqTo(arrivalMessages1.messages.head.path))(any(), any(), any())).thenReturn(Future.successful(ie057))
          when(mockConnector.getSpecificMessage[IE057Data](eqTo(arrivalMessages2.messages.head.path))(any(), any(), any())).thenReturn(Future.successful(ie057))

          val arrivalMovements = ArrivalMovements(arrivalMovements = Seq(movement1, movement2), totalCount = 2)

          val expectedResult = Seq(
            movementAndMessages1,
            movementAndMessages2
          )

          arrivalP5MessageService.getMessagesForAllMovements(arrivalMovements).futureValue mustBe expectedResult
        }

      }
      "must throw exception when movement doesn't contain an IE007" in {

        val movement1 = ArrivalMovement("arrivalId1", "movementReferenceNo1", dateTime, "/locationUrl1")
        val movement2 = ArrivalMovement("arrivalId2", "movementReferenceNo2", dateTime, "/locationUrl2")

        val message2 = ArrivalMessage(dateTime, ArrivalMessageType.UnloadingPermission)
        val message3 = ArrivalMessage(dateTime, ArrivalMessageType.RejectionFromOfficeOfDestination)

        val messages1 = MessagesForArrivalMovement(NonEmptyList(message2, List.empty))
        val messages2 = MessagesForArrivalMovement(NonEmptyList(message3, List.empty))

        when(mockConnector.getMessagesForMovement(eqTo("/locationUrl1"))(any())).thenReturn(
          Future.successful(messages1)
        )

        when(mockConnector.getMessagesForMovement(eqTo("/locationUrl2"))(any())).thenReturn(
          Future.successful(messages2)
        )

        val arrivalMovements = ArrivalMovements(arrivalMovements = Seq(movement1, movement2), totalCount = 2)

        val result = arrivalP5MessageService.getMessagesForAllMovements(arrivalMovements).failed.futureValue

        result mustBe a[Throwable]
      }
    }

    "getMessage" - {

      "must return an IE057Data when given Arrival Id" in {

        val messages = ArrivalMessages(
          List(
            ArrivalMessageMetaData(
              LocalDateTime.parse("2022-11-11T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.ArrivalNotification,
              "movements/arrivals/6365135ba5e821ee/message/634982098f02f00b"
            ),
            ArrivalMessageMetaData(
              LocalDateTime.parse("2022-11-10T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.RejectionFromOfficeOfDestination,
              "movements/arrivals/6365135ba5e821ee/message/634982098f02f00a"
            )
          )
        )

        val ie057Data: IE057Data = IE057Data(
          IE057MessageData(
            TransitOperationIE057("CD3232"),
            CustomsOfficeOfDestinationActual("1234"),
            Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
          )
        )

        when(mockConnector.getMessageMetaData(any())(any(), any())).thenReturn(Future.successful(messages))
        when(mockConnector.getSpecificMessage[IE057Data](any())(any(), any(), any())).thenReturn(Future.successful(ie057Data))

        arrivalP5MessageService.getMessage[IE057Data](arrivalId = "6365135ba5e821ee", RejectionFromOfficeOfDestination).futureValue mustBe Some(ie057Data)
      }
    }

  }

}
