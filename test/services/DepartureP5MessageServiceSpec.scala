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
import models.departureP5._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DepartureP5MessageServiceSpec extends SpecBase {

  val mockMovementConnector: DepartureMovementP5Connector = mock[DepartureMovementP5Connector]
  val mockCacheConnector: DepartureCacheConnector         = mock[DepartureCacheConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockMovementConnector); reset(mockCacheConnector)
  }

  val departureP5MessageService = new DepartureP5MessageService(mockMovementConnector, mockCacheConnector)

  private val doesDocumentStillExist = arbitrary[Boolean].sample.value

  "DepartureP5MessageService" - {

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

        when(mockMovementConnector.getMessagesForMovement(any())(any())).thenReturn(
          Future.successful(messagesForMovement)
        )

        when(mockMovementConnector.getLRN(messagesForMovement.messages.head.bodyPath)).thenReturn(
          Future.successful(LocalReferenceNumber("lrn123"))
        )

        when(mockCacheConnector.doesDocumentStillExist(any())(any())).thenReturn(
          Future.successful(doesDocumentStillExist)
        )

        val result = departureP5MessageService.getMessagesForAllMovements(departureMovements).futureValue

        val expectedResult = Seq(
          DepartureMovementAndMessage(
            departureMovements.departureMovements.head,
            messagesForMovement,
            "lrn123",
            isMovementInCache = doesDocumentStillExist
          )
        )

        result mustBe expectedResult
      }

      "must throw exception when movement doesn't contain an IE015" in {

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

        when(mockMovementConnector.getMessagesForMovement(any())(any())).thenReturn(
          Future.successful(messagesForMovement)
        )

        val result = departureP5MessageService.getMessagesForAllMovements(departureMovements).failed.futureValue

        result mustBe a[Throwable]
      }

    }

    "getGoodsUnderControlMessage" - {

      "must return a MessageMetaData when given Departure Id" in {

        val messages = Messages(
          List(
            MessageMetaData(
              LocalDateTime.parse("2022-11-11T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.DepartureNotification,
              "movements/departures/6365135ba5e821ee/message/634982098f02f00b"
            ),
            MessageMetaData(
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
        when(mockMovementConnector.getGoodsUnderControl(any())(any(), any())).thenReturn(Future.successful(ie060Data))

        departureP5MessageService.getGoodsUnderControl(departureId = "6365135ba5e821ee").futureValue mustBe Some(ie060Data)
      }
    }
  }

}
