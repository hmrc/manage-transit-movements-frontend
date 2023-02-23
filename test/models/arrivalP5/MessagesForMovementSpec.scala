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

package models.arrivalP5

import base.SpecBase
import cats.data.NonEmptyList
import play.api.libs.json.Json

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessagesForMovementSpec extends SpecBase {

  "MessagesForMovement" - {

    "messageBeforeLatest" - {

      "must return previous message" in {

        val currentMessage = Message(
          LocalDateTime.parse("2022-11-12T13:32:52.459Z", DateTimeFormatter.ISO_DATE_TIME),
          ArrivalMessageType.GoodsReleasedNotification
        )

        val previousMessage = Message(
          LocalDateTime.parse("2022-11-12T12:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
          ArrivalMessageType.UnloadingRemarks
        )

        val lastMessage = Message(
          LocalDateTime.parse("2022-11-11T11:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
          ArrivalMessageType.UnloadingPermission
        )

        val messagesForMovement = MessagesForMovement(
          NonEmptyList(currentMessage, List(previousMessage, lastMessage))
        )

        messagesForMovement.messageBeforeLatest mustBe Some(previousMessage)

      }

      "must return None when no previous message" in {

        val currentMessage = Message(
          LocalDateTime.parse("2022-11-12T13:32:52.459Z", DateTimeFormatter.ISO_DATE_TIME),
          ArrivalMessageType.GoodsReleasedNotification
        )

        val messagesForMovement = MessagesForMovement(
          NonEmptyList(currentMessage, List.empty)
        )

        messagesForMovement.messageBeforeLatest mustBe None
      }

    }

    "must deserialize and sort by time" in {

      val json =
        Json.parse("""
            |{
            |   "messages":[
            |      {
            |         "received":"2022-11-10T11:32:51.459Z",
            |         "type":"IE043"
            |      },
            |      {
            |         "received":"2022-11-10T10:32:51.459Z",
            |         "type":"IE007"
            |      },
            |      {
            |         "received":"2022-11-10T12:32:52.459Z",
            |         "type":"IE025"
            |      },
            |      {
            |         "received":"2022-11-10T12:32:51.459Z",
            |         "type":"IE044"
            |      }
            |   ]
            |}
            |""".stripMargin)

      val expectedResult = MessagesForMovement(
        NonEmptyList(
          Message(
            LocalDateTime.parse("2022-11-10T12:32:52.459Z", DateTimeFormatter.ISO_DATE_TIME),
            ArrivalMessageType.GoodsReleasedNotification
          ),
          List(
            Message(
              LocalDateTime.parse("2022-11-10T12:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.UnloadingRemarks
            ),
            Message(
              LocalDateTime.parse("2022-11-10T11:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.UnloadingPermission
            ),
            Message(
              LocalDateTime.parse("2022-11-10T10:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.ArrivalNotification
            )
          )
        )
      )

      val result: MessagesForMovement = json.validate[MessagesForMovement].asOpt.value

      result mustBe expectedResult
    }

    "must deserialize and sort by date and time" in {

      val json =
        Json.parse("""
            |{
            |   "messages":[
            |      {
            |         "received":"2022-11-11T11:32:51.459Z",
            |         "type":"IE043"
            |      },
            |      {
            |         "received":"2022-11-10T10:32:51.459Z",
            |         "type":"IE007"
            |      },
            |      {
            |         "received":"2022-11-12T13:32:52.459Z",
            |         "type":"IE025"
            |      },
            |      {
            |         "received":"2022-11-12T12:32:51.459Z",
            |         "type":"IE044"
            |      }
            |   ]
            |}
            |""".stripMargin)

      val expectedResult = MessagesForMovement(
        NonEmptyList(
          Message(
            LocalDateTime.parse("2022-11-12T13:32:52.459Z", DateTimeFormatter.ISO_DATE_TIME),
            ArrivalMessageType.GoodsReleasedNotification
          ),
          List(
            Message(
              LocalDateTime.parse("2022-11-12T12:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.UnloadingRemarks
            ),
            Message(
              LocalDateTime.parse("2022-11-11T11:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.UnloadingPermission
            ),
            Message(
              LocalDateTime.parse("2022-11-10T10:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              ArrivalMessageType.ArrivalNotification
            )
          )
        )
      )

      val result: MessagesForMovement = json.validate[MessagesForMovement].asOpt.value

      result mustBe expectedResult
    }

  }

}
