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
import models.arrivalP5._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}

import java.time.LocalDateTime
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

      "must return a sequence of ArrivalMovementAndMessage when given ArrivalMovements" in {

        val movement1 = ArrivalMovement("arrivalId1", "movementReferenceNo1", LocalDateTime.now(), "/locationUrl1")
        val movement2 = ArrivalMovement("arrivalId2", "movementReferenceNo2", LocalDateTime.now(), "/locationUrl2")

        val message1 = Message(LocalDateTime.now().minusDays(1), ArrivalMessageType.ArrivalNotification)
        val message2 = Message(LocalDateTime.now(), ArrivalMessageType.UnloadingPermission)

        val messages1 = MessagesForMovement(NonEmptyList(message1, List(message2)))
        val messages2 = MessagesForMovement(NonEmptyList(message1, List(message2)))

        val movementAndMessages1 = ArrivalMovementAndMessage(movement1, messages1)
        val movementAndMessages2 = ArrivalMovementAndMessage(movement2, messages2)

        when(mockConnector.getMessagesForMovement(eqTo("/locationUrl1"))(any())).thenReturn(Future.successful(messages1))
        when(mockConnector.getMessagesForMovement(eqTo("/locationUrl2"))(any())).thenReturn(Future.successful(messages2))

        val arrivalMovements = ArrivalMovements(Seq(movement1, movement2))

        val expectedResult = Seq(
          movementAndMessages1,
          movementAndMessages2
        )

        arrivalP5MessageService.getMessagesForAllMovements(arrivalMovements).futureValue mustBe expectedResult
      }
    }
  }

}
