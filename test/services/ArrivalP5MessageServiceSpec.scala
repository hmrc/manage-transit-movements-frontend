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
import connectors.ArrivalMovementP5Connector
import generated._
import generators.Generators
import models.arrivalP5.ArrivalMessageType.RejectionFromOfficeOfDestination
import models.arrivalP5._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ArrivalP5MessageServiceSpec extends SpecBase with Generators {

  val mockConnector: ArrivalMovementP5Connector = mock[ArrivalMovementP5Connector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConnector)
  }

  val arrivalP5MessageService = new ArrivalP5MessageService(mockConnector)

  "ArrivalP5MessageService" - {

    "getLatestMessagesForMovement" - {

      "must return RejectedMovementAndMessage when RejectedByOfficeOfDestination" in {

        val dateTimeNow = LocalDateTime.now(clock)

        val rejectionType = Gen.alphaNumStr.sample.value

        val latestArrivalMessage: LatestArrivalMessage = LatestArrivalMessage(
          ArrivalMessage(
            messageId = "messageId1",
            received = dateTimeNow,
            messageType = RejectionFromOfficeOfDestination
          ),
          arrivalIdP5
        )

        val arrivalMovements: ArrivalMovements = ArrivalMovements(
          arrivalMovements = Seq(
            ArrivalMovement(
              arrivalId = arrivalIdP5,
              movementReferenceNumber = mrn,
              updated = dateTimeNow,
              messagesLocation = "location"
            )
          ),
          totalCount = 1
        )

        val functionalErrors = Seq(
          FunctionalErrorType04("1", Number12, "Codelist violation", None),
          FunctionalErrorType04("2", Number14, "Rule violation", None)
        )

        val x = arbitrary[CC057CType].sample.value

        val ie057 = x
          .copy(TransitOperation = x.TransitOperation.copy(businessRejectionType = rejectionType))
          .copy(FunctionalError = functionalErrors)

        when(mockConnector.getMessage[CC057CType](any(), any())(any(), any(), any())).thenReturn(Future.successful(ie057))
        when(mockConnector.getLatestMessageForMovement(any())(any())).thenReturn(Future.successful(latestArrivalMessage))

        val result: Seq[ArrivalMovementAndMessage] = arrivalP5MessageService.getLatestMessagesForMovement(arrivalMovements).futureValue

        val expectedResult: Seq[RejectedMovementAndMessage] = Seq(
          RejectedMovementAndMessage(
            ArrivalMovement(
              arrivalId = arrivalIdP5,
              movementReferenceNumber = mrn,
              updated = dateTimeNow,
              messagesLocation = "location"
            ),
            latestArrivalMessage = latestArrivalMessage,
            functionalErrorCount = 2,
            rejectedType = rejectionType
          )
        )

        result mustBe expectedResult
      }
    }
  }
}
