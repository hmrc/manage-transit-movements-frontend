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
import connectors.{DepartureCacheConnector, DepartureMovementP5Connector}
import generated.*
import generators.Generators
import models.departureP5.DepartureMessageType.*
import models.departureP5.*
import models.departureP5.BusinessRejectionType.*
import models.IE015
import models.{LocalReferenceNumber, MessageStatus, RichCC182Type}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
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

    "getLatestMessagesForMovements" - {

      val dateTimeNow = LocalDateTime.now()

      "when RejectedByOfficeOfDeparture" - {
        "must return PrelodgeRejectedMovementAndMessage when rejection type is 170" in {
          val rejectionType = PresentationNotificationRejection

          val latestDepartureMessage = DepartureMovementMessages(
            NonEmptyList.one(
              DepartureMessage(
                "messageId1",
                dateTimeNow,
                RejectedByOfficeOfDeparture,
                MessageStatus.Success
              )
            ),
            "messageId2"
          )

          val departureMovements = DepartureMovements(
            departureMovements = Seq(
              DepartureMovement(
                "AB123",
                Some("MRN"),
                "LRN",
                dateTimeNow
              )
            ),
            totalCount = 1
          )

          val x = arbitrary[CC056CType].sample.value

          val ie056 = x.copy(
            TransitOperation = x.TransitOperation.copy(businessRejectionType = rejectionType.value)
          )

          when(mockMovementConnector.getMessages(any())(any())).thenReturn(
            Future.successful(latestDepartureMessage)
          )

          when(mockMovementConnector.getMessage[CC056CType](any(), any())(any(), any(), any())).thenReturn(
            Future.successful(ie056)
          )

          val result = departureP5MessageService.getLatestMessagesForMovements(departureMovements).futureValue

          val expectedResult: Seq[MovementAndMessages] = Seq(
            PrelodgeRejectedMovementAndMessages(
              "AB123",
              "LRN",
              dateTimeNow,
              latestDepartureMessage,
              xPaths = ie056.FunctionalError.map(_.errorPointer)
            )
          )

          result mustEqual expectedResult
        }

        "must return RejectedMovementAndMessage when rejection type is 014" in {
          val rejectionType = InvalidationRejection

          val latestDepartureMessage = DepartureMovementMessages(
            NonEmptyList.one(
              DepartureMessage(
                "messageId1",
                dateTimeNow,
                RejectedByOfficeOfDeparture,
                MessageStatus.Success
              )
            ),
            "messageId2"
          )

          val departureMovements = DepartureMovements(
            departureMovements = Seq(
              DepartureMovement(
                "AB123",
                Some("MRN"),
                "LRN",
                dateTimeNow
              )
            ),
            totalCount = 1
          )

          val x = arbitrary[CC056CType].sample.value

          val ie056 = x.copy(
            TransitOperation = x.TransitOperation.copy(businessRejectionType = rejectionType.value)
          )

          when(mockMovementConnector.getMessages(any())(any())).thenReturn(
            Future.successful(latestDepartureMessage)
          )

          when(mockMovementConnector.getMessage[CC056CType](any(), any())(any(), any(), any())).thenReturn(
            Future.successful(ie056)
          )

          val result = departureP5MessageService.getLatestMessagesForMovements(departureMovements).futureValue

          val expectedResult: Seq[MovementAndMessages] = Seq(
            RejectedMovementAndMessages(
              "AB123",
              "LRN",
              dateTimeNow,
              latestDepartureMessage,
              rejectionType,
              isRejectionAmendable = false,
              xPaths = ie056.FunctionalError.map(_.errorPointer)
            )
          )

          result mustEqual expectedResult
        }

        "must return RejectedMovementAndMessage when rejection type is 013 or 015" in {
          val isDeclarationAmendable = arbitrary[Boolean].sample.value
          val rejectionType          = arbitrary[DepartureBusinessRejectionType].sample.value

          val latestDepartureMessage = DepartureMovementMessages(
            NonEmptyList.one(
              DepartureMessage(
                "messageId1",
                dateTimeNow,
                RejectedByOfficeOfDeparture,
                MessageStatus.Success
              )
            ),
            "messageId2"
          )

          val departureMovements = DepartureMovements(
            departureMovements = Seq(
              DepartureMovement(
                "AB123",
                Some("MRN"),
                "LRN",
                dateTimeNow
              )
            ),
            totalCount = 1
          )

          val x = arbitrary[CC056CType].sample.value

          val ie056 = x.copy(
            TransitOperation = x.TransitOperation.copy(businessRejectionType = rejectionType.value)
          )

          when(mockMovementConnector.getMessages(any())(any())).thenReturn(
            Future.successful(latestDepartureMessage)
          )

          when(mockMovementConnector.getMessage[CC056CType](any(), any())(any(), any(), any())).thenReturn(
            Future.successful(ie056)
          )

          when(mockCacheConnector.isRejectionAmendable(any(), any())(any(), any())).thenReturn(
            Future.successful(isDeclarationAmendable)
          )

          val result = departureP5MessageService.getLatestMessagesForMovements(departureMovements).futureValue

          val expectedResult: Seq[MovementAndMessages] = Seq(
            RejectedMovementAndMessages(
              "AB123",
              "LRN",
              dateTimeNow,
              latestDepartureMessage,
              rejectionType,
              isRejectionAmendable = isDeclarationAmendable,
              xPaths = ie056.FunctionalError.map(_.errorPointer)
            )
          )

          result mustEqual expectedResult
        }

      }

      "must return PrelodgedMovementAndMessage when AllocatedMRN or DeclarationAmendmentAccepted" in {

        val genStatus = Gen.oneOf(Seq(DeclarationSent, DeclarationAmendmentAccepted, GoodsUnderControl)).sample.value

        val latestDepartureMessage = DepartureMovementMessages(
          NonEmptyList.one(
            DepartureMessage(
              "messageId1",
              dateTimeNow,
              genStatus,
              MessageStatus.Success
            )
          ),
          "messageId2"
        )

        val departureMovements = DepartureMovements(
          departureMovements = Seq(
            DepartureMovement(
              "AB123",
              Some("MRN"),
              "LRN",
              dateTimeNow
            )
          ),
          totalCount = 1
        )

        val ie015 = arbitrary[IE015].sample.value

        when(mockMovementConnector.getMessages(any())(any())).thenReturn(
          Future.successful(latestDepartureMessage)
        )

        when(mockMovementConnector.getMessage[IE015](any(), any())(any(), any(), any())).thenReturn(
          Future.successful(ie015)
        )

        val result = departureP5MessageService.getLatestMessagesForMovements(departureMovements).futureValue

        val expectedResult: Seq[MovementAndMessages] = Seq(
          DepartureMovementAndMessages(
            "AB123",
            "LRN",
            dateTimeNow,
            latestDepartureMessage,
            ie015.isPreLodged
          )
        )

        result mustEqual expectedResult
      }

      "must return IncidentMovementAndMessage when IncidentDuringTransit" in {

        val latestDepartureMessage = DepartureMovementMessages(
          NonEmptyList.one(
            DepartureMessage(
              "messageId",
              dateTimeNow,
              IncidentDuringTransit,
              MessageStatus.Success
            )
          ),
          "messageId"
        )

        val departureMovements = DepartureMovements(
          departureMovements = Seq(
            DepartureMovement(
              "AB123",
              Some("MRN"),
              "LRN",
              dateTimeNow
            )
          ),
          totalCount = 1
        )

        val ie182 = arbitrary[CC182CType].sample.value

        when(mockMovementConnector.getMessages(any())(any())).thenReturn(
          Future.successful(latestDepartureMessage)
        )

        when(mockMovementConnector.getMessage[CC182CType](any(), any())(any(), any(), any())).thenReturn(
          Future.successful(ie182)
        )

        val result = departureP5MessageService.getLatestMessagesForMovements(departureMovements).futureValue

        val expectedResult: Seq[MovementAndMessages] = Seq(
          IncidentMovementAndMessages(
            "AB123",
            "LRN",
            dateTimeNow,
            latestDepartureMessage,
            ie182.hasMultipleIncidents
          )
        )

        result mustEqual expectedResult
      }

      "must return OtherMovementAndMessage for any other message" in {

        DepartureMessageType.values
          .filterNot(
            value =>
              value == DeclarationAmendmentAccepted ||
                value == RejectedByOfficeOfDeparture ||
                value == GoodsUnderControl ||
                value == DeclarationSent ||
                value == IncidentDuringTransit
          )
          .map {

            messageType =>
              val latestDepartureMessage = DepartureMovementMessages(
                NonEmptyList.one(
                  DepartureMessage(
                    "messageId1",
                    dateTimeNow,
                    messageType,
                    MessageStatus.Success
                  )
                ),
                "messageId2"
              )

              val departureMovements = DepartureMovements(
                departureMovements = Seq(
                  DepartureMovement(
                    "AB123",
                    Some("MRN"),
                    "LRN",
                    dateTimeNow
                  )
                ),
                totalCount = 1
              )

              val ie015 = arbitrary[IE015].sample.value

              when(mockMovementConnector.getMessages(any())(any())).thenReturn(
                Future.successful(latestDepartureMessage)
              )

              when(mockMovementConnector.getMessage[IE015](any(), any())(any(), any(), any())).thenReturn(
                Future.successful(ie015)
              )

              val result = departureP5MessageService.getLatestMessagesForMovements(departureMovements).futureValue

              val expectedResult: Seq[MovementAndMessages] = Seq(
                OtherMovementAndMessages(
                  "AB123",
                  "LRN",
                  dateTimeNow,
                  latestDepartureMessage
                )
              )

              result mustEqual expectedResult
          }
      }
    }

    "getDepartureReferenceNumbers" - {
      "return DepartureReferenceNumbers when the connector call is successful" in {

        val departureId              = "testDepartureId"
        val expectedReferenceNumbers = DepartureReferenceNumbers("ref1", None)

        when(mockMovementConnector.getDepartureReferenceNumbers(any())(any(), any()))
          .thenReturn(Future.successful(expectedReferenceNumbers))

        val result = departureP5MessageService.getDepartureReferenceNumbers(departureId).futureValue

        result mustEqual expectedReferenceNumbers

        verify(mockMovementConnector).getDepartureReferenceNumbers(departureId)
      }
    }
  }
}
