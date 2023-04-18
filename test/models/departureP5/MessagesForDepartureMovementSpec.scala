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
import play.api.libs.json.Json

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessagesForDepartureMovementSpec extends SpecBase {

  "MessagesForDepartureMovement" - {

    "messageBeforeLatest" - {

      "must return previous message" in {

        val currentMessage = DepartureMessage(
          LocalDateTime.parse("2022-11-12T13:32:52.459Z", DateTimeFormatter.ISO_DATE_TIME),
          DepartureMessageType.ReleasedForTransit,
          "body/path"
        )

        val previousMessage = DepartureMessage(
          LocalDateTime.parse("2022-11-12T12:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
          DepartureMessageType.DepartureNotification,
          "body/path"
        )

        val lastMessage = DepartureMessage(
          LocalDateTime.parse("2022-11-11T11:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
          DepartureMessageType.RejectedByOfficeOfDeparture,
          "body/path"
        )

        val messagesForMovement = MessagesForDepartureMovement(
          NonEmptyList(currentMessage, List(previousMessage, lastMessage))
        )

        messagesForMovement.messageBeforeLatest mustBe Some(previousMessage)

      }

      "must return None when no previous message" in {

        val currentMessage = DepartureMessage(
          LocalDateTime.parse("2022-11-12T13:32:52.459Z", DateTimeFormatter.ISO_DATE_TIME),
          DepartureMessageType.ReleasedForTransit,
          "body/path"
        )

        val messagesForMovement = MessagesForDepartureMovement(
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
            |         "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/messages/1"
            |             }
            |         },
            |         "received":"2022-11-10T11:32:51.459Z",
            |         "type":"IE015"
            |      },
            |      {
            |       "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/messages/2"
            |             }
            |         },
            |         "received":"2022-11-10T10:32:51.459Z",
            |         "type":"IE028"
            |      },
            |      {
            |        "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/messages/3"
            |             }
            |         },
            |         "received":"2022-11-10T12:32:52.459Z",
            |         "type":"IE029"
            |      }
            |   ]
            |}
            |""".stripMargin)

      val expectedResult = MessagesForDepartureMovement(
        NonEmptyList(
          DepartureMessage(
            LocalDateTime.parse("2022-11-10T12:32:52.459Z", DateTimeFormatter.ISO_DATE_TIME),
            DepartureMessageType.ReleasedForTransit,
            "movements/departures/1/messages/3"
          ),
          List(
            DepartureMessage(
              LocalDateTime.parse("2022-11-10T11:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.DepartureNotification,
              "movements/departures/1/messages/1"
            ),
            DepartureMessage(
              LocalDateTime.parse("2022-11-10T10:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.AllocatedMRN,
              "movements/departures/1/messages/2"
            )
          )
        )
      )

      val result: MessagesForDepartureMovement = json.validate[MessagesForDepartureMovement].asOpt.value

      result mustBe expectedResult
    }

    "must deserialize and sort by date and time" in {

      val json =
        Json.parse("""
            |{
            |   "messages":[
            |      {
            |        "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/messages/1"
            |             }
            |         },
            |         "received":"2022-11-10T11:32:51.459Z",
            |         "type":"IE015"
            |      },
            |      {
            |        "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/messages/2"
            |             }
            |         },
            |         "received":"2022-11-10T10:32:51.459Z",
            |         "type":"IE028"
            |      },
            |      {
            |         "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/messages/3"
            |             }
            |         },
            |         "received":"2022-11-10T12:32:52.459Z",
            |         "type":"IE029"
            |      }
            |   ]
            |}
            |""".stripMargin)

      val expectedResult = MessagesForDepartureMovement(
        NonEmptyList(
          DepartureMessage(
            LocalDateTime.parse("2022-11-10T12:32:52.459Z", DateTimeFormatter.ISO_DATE_TIME),
            DepartureMessageType.ReleasedForTransit,
            "movements/departures/1/messages/3"
          ),
          List(
            DepartureMessage(
              LocalDateTime.parse("2022-11-10T11:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.DepartureNotification,
              "movements/departures/1/messages/1"
            ),
            DepartureMessage(
              LocalDateTime.parse("2022-11-10T10:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
              DepartureMessageType.AllocatedMRN,
              "movements/departures/1/messages/2"
            )
          )
        )
      )

      val result: MessagesForDepartureMovement = json.validate[MessagesForDepartureMovement].asOpt.value

      result mustBe expectedResult
    }

  }

}
