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
import generators.Generators
import models.ArrivalRejectionType.{ArrivalNotificationRejection, UnloadingRemarkRejection}
import models.{ArrivalRejectionType, RejectionType}
import models.arrivalP5.ArrivalMessageType.{ArrivalNotification, RejectionFromOfficeOfDestination}
import models.arrivalP5._
import models.departureP5.FunctionalError
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import uk.gov.hmrc.http.HttpReads.Implicits._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
    val dateTimeNow   = LocalDateTime.now(clock)
    val rejectionType = arbitrary[ArrivalRejectionType].sample.value

    "getLatestMessagesForMovement" - {

      "must return RejectedMovementAndMessage when RejectedByOfficeOfDestination" in {

        val latestArrivalMessage = LatestArrivalMessage(
          ArrivalMessage(
            "messageId1",
            dateTimeNow,
            RejectionFromOfficeOfDestination
          ),
          arrivalIdP5
        )

        val arrivalMovements: ArrivalMovements = ArrivalMovements(
          arrivalMovements = Seq(
            ArrivalMovement(
              arrivalIdP5,
              "MRN",
              dateTimeNow,
              "location"
            )
          ),
          totalCount = 1
        )

        val ie057 = IE057Data(
          IE057MessageData(
            transitOperation = TransitOperationIE057(mrn, rejectionType),
            customsOfficeOfDestinationActual = CustomsOfficeOfDestinationActual("AB123"),
            functionalErrors = Seq(
              FunctionalError("pointer1", "code1", "reason1", None),
              FunctionalError("pointer2", "code2", "reason2", None)
            )
          )
        )

        val messages = ArrivalMessages(
          List(
            ArrivalMessageMetaData(
              LocalDateTime.parse("2022-11-10T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.RejectionFromOfficeOfDestination,
              s"movements/arrivals/$arrivalIdP5/message/634982098f02f00a"
            )
          )
        )
        when(mockConnector.getMessageMetaData(any())(any(), any())).thenReturn(Future.successful(messages))
        when(mockConnector.getSpecificMessage[IE057Data](any())(any(), any(), any())).thenReturn(Future.successful(ie057))

        val result = arrivalP5MessageService.getLatestMessagesForMovement(arrivalMovements).futureValue

        val expectedResult: Seq[RejectedMovementAndMessage] = Seq(
          RejectedMovementAndMessage(
            ArrivalMovement(
              arrivalIdP5,
              "MRN",
              dateTimeNow,
              "location"
            ),
            latestArrivalMessage,
            functionalErrorCount = 1,
            rejectionType
          )
        )

        result mustBe expectedResult

      }
    }

    "getMessage" - {

      "must return an IE057Data when given Arrival Id" in {

        val messages = ArrivalMessages(
          List(
            ArrivalMessageMetaData(
              LocalDateTime.parse("2022-11-11T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.RejectionFromOfficeOfDestination,
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
            TransitOperationIE057("CD3232", UnloadingRemarkRejection),
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
