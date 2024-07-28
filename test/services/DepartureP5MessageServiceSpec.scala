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
import generated._
import generators.Generators
import models.departureP5.DepartureMessageType._
import models.departureP5._
import models.{LocalReferenceNumber, RichCC015Type, RichCC182Type}
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

    "getLatestMessagesForMovement" - {

      val dateTimeNow = LocalDateTime.now()

      "must return RejectedMovementAndMessage when RejectedByOfficeOfDeparture" in {

        val isDeclarationAmendable = arbitrary[Boolean].sample.value
        val rejectionType          = arbitrary[BusinessRejectionType].sample.value

        val latestDepartureMessage = LatestDepartureMessage(
          DepartureMessage(
            "messageId1",
            dateTimeNow,
            RejectedByOfficeOfDeparture
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

        when(mockMovementConnector.getLatestMessageForMovement(any())(any())).thenReturn(
          Future.successful(latestDepartureMessage)
        )

        when(mockMovementConnector.getMessage[CC056CType](any(), any())(any(), any(), any())).thenReturn(
          Future.successful(ie056)
        )

        when(mockCacheConnector.isDeclarationAmendable(any(), any())(any())).thenReturn(
          Future.successful(isDeclarationAmendable)
        )

        val result = departureP5MessageService.getLatestMessagesForMovement(departureMovements).futureValue

        val expectedResult: Seq[MovementAndMessage] = Seq(
          RejectedMovementAndMessage(
            "AB123",
            "LRN",
            dateTimeNow,
            latestDepartureMessage,
            rejectionType,
            isDeclarationAmendable = isDeclarationAmendable,
            xPaths = ie056.FunctionalError.map(_.errorPointer)
          )
        )

        result mustBe expectedResult
      }

      "must return PrelodgedMovementAndMessage when AllocatedMRN or DeclarationAmendmentAccepted" in {

        val genStatus = Gen.oneOf(Seq(DeclarationSent, DeclarationAmendmentAccepted, GoodsUnderControl)).sample.value

        val latestDepartureMessage = LatestDepartureMessage(
          DepartureMessage(
            "messageId1",
            dateTimeNow,
            genStatus
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

        val ie015 = arbitrary[CC015CType].sample.value

        when(mockMovementConnector.getLatestMessageForMovement(any())(any())).thenReturn(
          Future.successful(latestDepartureMessage)
        )

        when(mockMovementConnector.getMessage[CC015CType](any(), any())(any(), any(), any())).thenReturn(
          Future.successful(ie015)
        )

        val result = departureP5MessageService.getLatestMessagesForMovement(departureMovements).futureValue

        val expectedResult: Seq[MovementAndMessage] = Seq(
          DepartureMovementAndMessage(
            "AB123",
            "LRN",
            dateTimeNow,
            latestDepartureMessage,
            ie015.isPreLodged
          )
        )

        result mustBe expectedResult
      }

      "must return IncidentMovementAndMessage when IncidentDuringTransit" in {

        val latestDepartureMessage = LatestDepartureMessage(
          DepartureMessage(
            "messageId",
            dateTimeNow,
            IncidentDuringTransit
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

        when(mockMovementConnector.getLatestMessageForMovement(any())(any())).thenReturn(
          Future.successful(latestDepartureMessage)
        )

        when(mockMovementConnector.getMessage[CC182CType](any(), any())(any(), any(), any())).thenReturn(
          Future.successful(ie182)
        )

        val result = departureP5MessageService.getLatestMessagesForMovement(departureMovements).futureValue

        val expectedResult: Seq[MovementAndMessage] = Seq(
          IncidentMovementAndMessage(
            "AB123",
            "LRN",
            dateTimeNow,
            latestDepartureMessage,
            ie182.hasMultipleIncidents
          )
        )

        result mustBe expectedResult
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

            status =>
              val latestDepartureMessage = LatestDepartureMessage(
                DepartureMessage(
                  "messageId1",
                  dateTimeNow,
                  status
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

              val ie015 = arbitrary[CC015CType].sample.value

              when(mockMovementConnector.getLatestMessageForMovement(any())(any())).thenReturn(
                Future.successful(latestDepartureMessage)
              )

              when(mockMovementConnector.getMessage[CC015CType](any(), any())(any(), any(), any())).thenReturn(
                Future.successful(ie015)
              )

              val result = departureP5MessageService.getLatestMessagesForMovement(departureMovements).futureValue

              val expectedResult: Seq[MovementAndMessage] = Seq(
                OtherMovementAndMessage(
                  "AB123",
                  "LRN",
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
