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
import connectors.{DepartureCacheConnector, DepartureMovementP5Connector}
import generators.Generators
import models.RejectionType.DeclarationRejection
import models.departureP5.DepartureMessageType._
import models.departureP5._
import models.{LocalReferenceNumber, RejectionType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DepartureP5MessageServiceSpec extends SpecBase with Generators {

  val mockMovementConnector: DepartureMovementP5Connector = mock[DepartureMovementP5Connector]
  val mockCacheConnector: DepartureCacheConnector         = mock[DepartureCacheConnector]
  val lrnLocal: LocalReferenceNumber                      = LocalReferenceNumber("lrn123")

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMovementConnector); reset(mockCacheConnector)
  }

  val departureP5MessageService = new DepartureP5MessageService(mockMovementConnector, mockCacheConnector)

  "DepartureP5MessageService" - {

    "getMessageForMessageId" - {

      "must return a message by ID" in {

        val ie056Data: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("CD3232"), None, DeclarationRejection),
            CustomsOfficeOfDeparture("1234"),
            Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
          )
        )

        when(mockMovementConnector.getMessageForMessageId[IE056Data](any(), any())(any(), any(), any())).thenReturn(Future.successful(ie056Data))

        departureP5MessageService.getMessageWithMessageId[IE056Data](departureId = "6365135ba5e821ee", messageId = messageId).futureValue mustBe ie056Data
      }
    }

    "getLatestMessagesForMovement" - {

      val dateTimeNow = LocalDateTime.now()

      "must return RejectedMovementAndMessage when RejectedByOfficeOfDeparture" in {

        val isDeclarationAmendable = arbitrary[Boolean].sample.value
        val rejectionType          = arbitrary[RejectionType].sample.value

        val latestDepartureMessage = LatestDepartureMessage(
          DepartureMessage(
            "messageId1",
            dateTimeNow,
            RejectedByOfficeOfDeparture,
            "body/path/2"
          ),
          "messageId2"
        )

        val departureMovements = DepartureMovements(
          departureMovements = Seq(
            DepartureMovement(
              "AB123",
              Some("MRN"),
              LocalReferenceNumber("LRN"),
              dateTimeNow,
              "location"
            )
          ),
          totalCount = 1
        )

        val ie056 = IE056Data(
          IE056MessageData(
            transitOperation = TransitOperationIE056(None, None, rejectionType),
            customsOfficeOfDeparture = CustomsOfficeOfDeparture("AB123"),
            functionalErrors = Seq(
              FunctionalError("pointer1", "code1", "reason1", None),
              FunctionalError("pointer2", "code2", "reason2", None)
            )
          )
        )

        when(mockMovementConnector.getLatestMessageForMovement(any())(any())).thenReturn(
          Future.successful(latestDepartureMessage)
        )

        when(mockMovementConnector.getMessageForMessageId[IE056Data](any(), any())(any(), any(), any())).thenReturn(
          Future.successful(ie056)
        )

        when(mockCacheConnector.isDeclarationAmendable(any(), any())(any())).thenReturn(
          Future.successful(isDeclarationAmendable)
        )

        when(mockCacheConnector.doesDeclarationExist(any())(any())).thenReturn(
          Future.successful(true)
        )

        val result = departureP5MessageService.getLatestMessagesForMovement(departureMovements).futureValue

        val expectedResult: Seq[MovementAndMessage] = Seq(
          RejectedMovementAndMessage(
            "AB123",
            LocalReferenceNumber("LRN"),
            dateTimeNow,
            latestDepartureMessage,
            rejectionType,
            isDeclarationAmendable = isDeclarationAmendable,
            xPaths = ie056.data.functionalErrors.map(_.errorPointer),
            doesCacheExistForLrn = true
          )
        )

        result mustBe expectedResult
      }

      "must return PrelodgedMovementAndMessage when AllocatedMRN or DeclarationAmendmentAccepted" in {

        val prelodged = Gen.oneOf(Prelodged.values).sample.value
        val genStatus = Gen.oneOf(Seq(DeclarationSent, DeclarationAmendmentAccepted, GoodsUnderControl)).sample.value

        val latestDepartureMessage = LatestDepartureMessage(
          DepartureMessage(
            "messageId1",
            dateTimeNow,
            genStatus,
            "body/path/2"
          ),
          "messageId2"
        )

        val departureMovements = DepartureMovements(
          departureMovements = Seq(
            DepartureMovement(
              "AB123",
              Some("MRN"),
              LocalReferenceNumber("LRN"),
              dateTimeNow,
              "location"
            )
          ),
          totalCount = 1
        )

        val ie015 = IE015Data(IE015MessageData(transitOperation = TransitOperationIE015(prelodged)))

        when(mockMovementConnector.getLatestMessageForMovement(any())(any())).thenReturn(
          Future.successful(latestDepartureMessage)
        )

        when(mockMovementConnector.getMessageForMessageId[IE015Data](any(), any())(any(), any(), any())).thenReturn(
          Future.successful(ie015)
        )

        val result = departureP5MessageService.getLatestMessagesForMovement(departureMovements).futureValue

        val expectedResult: Seq[MovementAndMessage] = Seq(
          DepartureMovementAndMessage(
            "AB123",
            LocalReferenceNumber("LRN"),
            dateTimeNow,
            latestDepartureMessage,
            ie015.isPrelodged
          )
        )

        result mustBe expectedResult
      }

      "must return OtherMovementAndMessage for any other message" in {

        val prelodged = Gen.oneOf(Prelodged.values).sample.value

        DepartureMessageType.values
          .filterNot(
            value =>
              value == DeclarationAmendmentAccepted ||
                value == RejectedByOfficeOfDeparture ||
                value == GoodsUnderControl ||
                value == DeclarationSent
          )
          .map {

            status =>
              val latestDepartureMessage = LatestDepartureMessage(
                DepartureMessage(
                  "messageId1",
                  dateTimeNow,
                  status,
                  "body/path/2"
                ),
                "messageId2"
              )

              val departureMovements = DepartureMovements(
                departureMovements = Seq(
                  DepartureMovement(
                    "AB123",
                    Some("MRN"),
                    LocalReferenceNumber("LRN"),
                    dateTimeNow,
                    "location"
                  )
                ),
                totalCount = 1
              )

              val ie015 = IE015Data(IE015MessageData(transitOperation = TransitOperationIE015(prelodged)))

              when(mockMovementConnector.getLatestMessageForMovement(any())(any())).thenReturn(
                Future.successful(latestDepartureMessage)
              )

              when(mockMovementConnector.getMessageForMessageId[IE015Data](any(), any())(any(), any(), any())).thenReturn(
                Future.successful(ie015)
              )

              val result = departureP5MessageService.getLatestMessagesForMovement(departureMovements).futureValue

              val expectedResult: Seq[MovementAndMessage] = Seq(
                OtherMovementAndMessage(
                  "AB123",
                  LocalReferenceNumber("LRN"),
                  dateTimeNow,
                  latestDepartureMessage
                )
              )

              result mustBe expectedResult
          }
      }
    }
  }
}
