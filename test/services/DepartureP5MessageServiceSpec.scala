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
import generators.Generators
import models.RejectionType
import models.departureP5.DepartureMessageType.GoodsUnderControl
import models.departureP5._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import uk.gov.hmrc.http.HttpReads.Implicits._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

    "getMessagesForAllMovements" - {

      val dateTimeNow = LocalDateTime.now(clock)

      val departureMovements = DepartureMovements(
        departureMovements = Seq(
          DepartureMovement(
            "AB123",
            Some("MRN"),
            dateTimeNow,
            "location"
          )
        ),
        totalCount = 1
      )

      "must return departure movements with messages with an LRN" - {
        "when there isn't a IE056" in {

          val messagesForMovement =
            MessagesForDepartureMovement(
              NonEmptyList(
                DepartureMessage(
                  "messageId1",
                  dateTimeNow,
                  DepartureMessageType.DepartureNotification,
                  "body/path/1"
                ),
                List(
                  DepartureMessage(
                    "messageId2",
                    dateTimeNow,
                    DepartureMessageType.GoodsUnderControl,
                    "body/path/2"
                  )
                )
              )
            )

          when(mockMovementConnector.getMessagesForMovement(any())(any())).thenReturn(
            Future.successful(messagesForMovement)
          )

          when(mockMovementConnector.getLRN(messagesForMovement.messages.head.bodyPath)).thenReturn(
            Future.successful(lrnLocal)
          )

          when(mockMovementConnector.getMessageMetaData(any())(any(), any())).thenReturn(
            Future.successful(DepartureMessages(Nil))
          )

          val result = departureP5MessageService.getMessagesForAllMovements(departureMovements).futureValue

          val expectedResult = Seq(
            DepartureMovementAndMessage(
              departureMovements.departureMovements.head,
              messagesForMovement,
              lrnLocal.toString,
              None,
              isDeclarationAmendable = false,
              xPaths = Seq.empty
            )
          )

          result mustBe expectedResult

          verify(mockMovementConnector).getMessageMetaData(eqTo("AB123"))(any(), any())
          verify(mockCacheConnector, never()).isDeclarationAmendable(any(), any())(any())
        }

        "when there is a IE056" in {

          val isDeclarationAmendable = arbitrary[Boolean].sample.value
          val rejectionType          = arbitrary[RejectionType].sample.value

          val messagesForMovement =
            MessagesForDepartureMovement(
              NonEmptyList(
                DepartureMessage(
                  "messageId1",
                  dateTimeNow,
                  DepartureMessageType.DepartureNotification,
                  "body/path/1"
                ),
                List(
                  DepartureMessage(
                    "messageId2",
                    dateTimeNow,
                    DepartureMessageType.RejectedByOfficeOfDeparture,
                    "body/path/2"
                  )
                )
              )
            )

          when(mockMovementConnector.getMessagesForMovement(any())(any())).thenReturn(
            Future.successful(messagesForMovement)
          )

          when(mockMovementConnector.getLRN(messagesForMovement.messages.head.bodyPath)).thenReturn(
            Future.successful(lrnLocal)
          )

          val messages = DepartureMessages(
            List(
              DepartureMessageMetaData(
                LocalDateTime.parse("2022-11-10T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
                DepartureMessageType.RejectedByOfficeOfDeparture,
                s"movements/departures/$departureIdP5/message/634982098f02f00a"
              )
            )
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

          when(mockMovementConnector.getMessageMetaData(any())(any(), any())).thenReturn(
            Future.successful(messages)
          )

          when(mockMovementConnector.getSpecificMessage[IE056Data](any())(any(), any(), any())).thenReturn(
            Future.successful(ie056)
          )

          when(mockCacheConnector.isDeclarationAmendable(any(), any())(any())).thenReturn(
            Future.successful(isDeclarationAmendable)
          )

          val result = departureP5MessageService.getMessagesForAllMovements(departureMovements).futureValue

          val expectedResult = Seq(
            DepartureMovementAndMessage(
              departureMovements.departureMovements.head,
              messagesForMovement,
              lrnLocal.toString,
              Some(rejectionType),
              isDeclarationAmendable = isDeclarationAmendable,
              xPaths = ie056.data.functionalErrors.map(_.errorPointer)
            )
          )

          result mustBe expectedResult

          verify(mockCacheConnector).isDeclarationAmendable(eqTo(lrnLocal.toString), eqTo(Seq("pointer1", "pointer2")))(any())
        }
      }

      "must throw exception when movement doesn't contain an IE015" in {

        val messagesForMovement =
          MessagesForDepartureMovement(
            NonEmptyList(
              DepartureMessage(
                "messageId1",
                dateTimeNow,
                DepartureMessageType.GoodsUnderControl,
                "body/path/2"
              ),
              List.empty
            )
          )

        when(mockMovementConnector.getMessagesForMovement(any())(any())).thenReturn(
          Future.successful(messagesForMovement)
        )

        val result = departureP5MessageService.getMessagesForAllMovements(departureMovements).failed.futureValue

        result mustBe a[Throwable]
      }

    }

    "getMessage" - {

      "must return an IE060Data when given Departure Id" in {

        val messages = DepartureMessages(
          List(
            DepartureMessageMetaData(
              LocalDateTime.parse("2022-11-11T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.DepartureNotification,
              "movements/departures/6365135ba5e821ee/message/634982098f02f00b"
            ),
            DepartureMessageMetaData(
              LocalDateTime.parse("2022-11-10T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.GoodsUnderControl,
              "movements/departures/6365135ba5e821ee/message/634982098f02f00a"
            )
          )
        )

        val ie060Data = IE060Data(
          IE060MessageData(
            TransitOperation(Some("CD3232"), Some("AB123"), LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
            CustomsOfficeOfDeparture("22323323"),
            Some(Seq(TypeOfControls("1", "type1", Some("text1")), TypeOfControls("2", "type2", None))),
            Some(Seq(RequestedDocument("3", "doc1", Some("desc1")), RequestedDocument("4", "doc2", None)))
          )
        )

        when(mockMovementConnector.getMessageMetaData(any())(any(), any())).thenReturn(Future.successful(messages))
        when(mockMovementConnector.getSpecificMessage[IE060Data](any())(any(), any(), any())).thenReturn(Future.successful(ie060Data))

        departureP5MessageService.getMessage[IE060Data](departureId = "6365135ba5e821ee", GoodsUnderControl).futureValue mustBe Some(ie060Data)
      }

      "must return an IE056Data when given Departure Id" in {

        val messages = DepartureMessages(
          List(
            DepartureMessageMetaData(
              LocalDateTime.parse("2022-11-11T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.DepartureNotification,
              "movements/departures/6365135ba5e821ee/message/634982098f02f00b"
            ),
            DepartureMessageMetaData(
              LocalDateTime.parse("2022-11-10T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.GoodsUnderControl,
              "movements/departures/6365135ba5e821ee/message/634982098f02f00a"
            )
          )
        )

        val ie056Data: IE056Data = IE056Data(
          IE056MessageData(
            TransitOperationIE056(Some("CD3232"), None, RejectionType.DeclarationRejection),
            CustomsOfficeOfDeparture("AB123"),
            Seq(FunctionalError("1", "12", "Codelist violation", None), FunctionalError("2", "14", "Rule violation", None))
          )
        )

        when(mockMovementConnector.getMessageMetaData(any())(any(), any())).thenReturn(Future.successful(messages))
        when(mockMovementConnector.getSpecificMessage[IE056Data](any())(any(), any(), any())).thenReturn(Future.successful(ie056Data))

        departureP5MessageService.getMessage[IE056Data](departureId = "6365135ba5e821ee", GoodsUnderControl).futureValue mustBe Some(ie056Data)
      }
    }

    "getLRNFromDeclarationMessage" - {

      "must return a LRN when given a Departure Id" in {

        val messages = DepartureMessages(
          List(
            DepartureMessageMetaData(
              LocalDateTime.parse("2022-11-11T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.DepartureNotification,
              "movements/departures/6365135ba5e821ee/message/634982098f02f00b"
            )
          )
        )

        when(mockMovementConnector.getMessageMetaData(any())(any(), any())).thenReturn(Future.successful(messages))
        when(mockMovementConnector.getLRN(any())(any())).thenReturn(Future.successful(lrnLocal))

        departureP5MessageService.getLRNFromDeclarationMessage(departureId = "6365135ba5e821ee").futureValue mustBe Some(lrnLocal.referenceNumber)
      }

      "must return None when IE015 message not found in meta data when given a Departure Id" in {

        val messages = DepartureMessages(
          List(
            DepartureMessageMetaData(
              LocalDateTime.parse("2022-11-11T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.GoodsUnderControl,
              "movements/departures/6365135ba5e821ee/message/634982098f02f00b"
            )
          )
        )

        when(mockMovementConnector.getMessageMetaData(any())(any(), any())).thenReturn(Future.successful(messages))

        departureP5MessageService.getLRNFromDeclarationMessage(departureId = "6365135ba5e821ee").futureValue mustBe None
      }
    }
  }
}
