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

package models.departureP5

import base.SpecBase
import cats.data.NonEmptyList
import models.MessageStatus
import play.api.libs.json.Json

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessagesForDepartureMovementSpec extends SpecBase {

  "MessagesForDepartureMovement" - {

    "messageBeforeLatest" - {

      "must return previous message" in {

        val currentMessage = DepartureMessage(
          "messageId1",
          LocalDateTime.parse("2022-11-12T13:32:52.459Z", DateTimeFormatter.ISO_DATE_TIME),
          DepartureMessageType.ReleasedForTransit,
          MessageStatus.Success
        )

        val previousMessage = DepartureMessage(
          "messageId2",
          LocalDateTime.parse("2022-11-12T12:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
          DepartureMessageType.DepartureNotification,
          MessageStatus.Success
        )

        val lastMessage = DepartureMessage(
          "messageId3",
          LocalDateTime.parse("2022-11-11T11:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
          DepartureMessageType.RejectedByOfficeOfDeparture,
          MessageStatus.Success
        )

        val messagesForMovement = MessagesForDepartureMovement(
          NonEmptyList(currentMessage, List(previousMessage, lastMessage))
        )

        messagesForMovement.messageBeforeLatest `mustBe` Some(previousMessage)

      }

      "must return None when no previous message" in {

        val currentMessage = DepartureMessage(
          "messageId1",
          LocalDateTime.parse("2022-11-12T13:32:52.459Z", DateTimeFormatter.ISO_DATE_TIME),
          DepartureMessageType.ReleasedForTransit,
          MessageStatus.Success
        )

        val messagesForMovement = MessagesForDepartureMovement(
          NonEmptyList(currentMessage, List.empty)
        )

        messagesForMovement.messageBeforeLatest `mustBe` None
      }

    }

    "must deserialize and sort by time" in {

      val json =
        Json.parse(s"""
            |{
            |   "messages":[
            |      {
            |         "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/messages/1"
            |             }
            |         },
            |         "id":"messageId1",
            |         "received":"2022-11-10T11:32:51.459Z",
            |         "type":"IE015",
            |         "status" : "Success"
            |      },
            |      {
            |       "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/messages/2"
            |             }
            |         },
            |         "id":"messageId2",
            |         "received":"2022-11-10T10:32:51.459Z",
            |         "type":"IE028",
            |         "status" : "Success"
            |      },
            |      {
            |        "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/messages/3"
            |             }
            |         },
            |         "id":"messageId3",
            |         "received":"2022-11-10T12:32:52.459Z",
            |         "type":"IE029",
            |         "status" : "Success"
            |      }
            |   ]
            |}
            |""".stripMargin)

      val expectedResult = MessagesForDepartureMovement(
        NonEmptyList(
          DepartureMessage(
            "messageId3",
            LocalDateTime.parse("2022-11-10T12:32:52.459Z", DateTimeFormatter.ISO_DATE_TIME),
            DepartureMessageType.ReleasedForTransit,
            MessageStatus.Success
          ),
          List(
            DepartureMessage(
              "messageId1",
              LocalDateTime.parse("2022-11-10T11:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.DepartureNotification,
              MessageStatus.Success
            ),
            DepartureMessage(
              "messageId2",
              LocalDateTime.parse("2022-11-10T10:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.AllocatedMRN,
              MessageStatus.Success
            )
          )
        )
      )

      val result: MessagesForDepartureMovement = json.validate[MessagesForDepartureMovement].asOpt.value

      result `mustBe` expectedResult
    }

    "must deserialize and sort by date and time" in {

      val json =
        Json.parse(s"""
            |{
            |   "messages":[
            |      {
            |        "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/messages/1"
            |             }
            |         },
            |         "id":"messageId1",
            |         "received":"2022-11-10T11:32:51.459Z",
            |         "type":"IE015",
            |         "status" : "Success"
            |      },
            |      {
            |        "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/messages/2"
            |             }
            |         },
            |         "id":"messageId2",
            |         "received":"2022-11-10T10:32:51.459Z",
            |         "type":"IE028",
            |         "status" : "Success"
            |      },
            |      {
            |         "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/messages/3"
            |             }
            |         },
            |         "id":"messageId3",
            |         "received":"2022-11-10T12:32:52.459Z",
            |         "type":"IE029",
            |         "status" : "Success"
            |      }
            |   ]
            |}
            |""".stripMargin)

      val expectedResult = MessagesForDepartureMovement(
        NonEmptyList(
          DepartureMessage(
            "messageId3",
            LocalDateTime.parse("2022-11-10T12:32:52.459Z", DateTimeFormatter.ISO_DATE_TIME),
            DepartureMessageType.ReleasedForTransit,
            MessageStatus.Success
          ),
          List(
            DepartureMessage(
              "messageId1",
              LocalDateTime.parse("2022-11-10T11:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.DepartureNotification,
              MessageStatus.Success
            ),
            DepartureMessage(
              "messageId2",
              LocalDateTime.parse("2022-11-10T10:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.AllocatedMRN,
              MessageStatus.Success
            )
          )
        )
      )

      val result: MessagesForDepartureMovement = json.validate[MessagesForDepartureMovement].asOpt.value

      result `mustBe` expectedResult
    }

  }

}
