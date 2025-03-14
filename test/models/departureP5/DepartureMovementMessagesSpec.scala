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

class DepartureMovementMessagesSpec extends SpecBase {

  "DepartureMovementMessages" - {

    "must deserialize and find most recent IE015" in {

      val json =
        Json.parse(s"""
            |{
            |   "messages":[
            |     {
            |         "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/message/messageId1.1"
            |             }
            |         },
            |         "id":"messageId1.1",
            |         "received":"2022-11-10T09:32:51.459Z",
            |         "type":"IE015",
            |         "status" : "Failed"
            |      },
            |      {
            |         "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/message/messageId1.2"
            |             }
            |         },
            |         "id":"messageId1.2",
            |         "received":"2022-11-10T10:32:51.459Z",
            |         "type":"IE015",
            |         "status" : "Success"
            |      },
            |      {
            |       "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/message/messageId2"
            |             }
            |         },
            |         "id":"messageId2",
            |         "received":"2022-11-10T11:32:51.459Z",
            |         "type":"IE028",
            |         "status" : "Success"
            |      },
            |      {
            |        "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/message/messageId3"
            |             }
            |         },
            |         "id":"messageId3",
            |         "received":"2022-11-10T12:32:51.459Z",
            |         "type":"IE029",
            |         "status" : "Success"
            |      }
            |   ]
            |}
            |""".stripMargin)

      val expectedResult = DepartureMovementMessages(
        NonEmptyList.of(
          DepartureMessage(
            "messageId3",
            LocalDateTime.parse("2022-11-10T12:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
            DepartureMessageType.ReleasedForTransit,
            MessageStatus.Success
          ),
          DepartureMessage(
            "messageId2",
            LocalDateTime.parse("2022-11-10T11:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
            DepartureMessageType.AllocatedMRN,
            MessageStatus.Success
          ),
          DepartureMessage(
            "messageId1.2",
            LocalDateTime.parse("2022-11-10T10:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
            DepartureMessageType.DepartureNotification,
            MessageStatus.Success
          ),
          DepartureMessage(
            "messageId1.1",
            LocalDateTime.parse("2022-11-10T09:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
            DepartureMessageType.DepartureNotification,
            MessageStatus.Failed
          )
        ),
        ie015MessageId = "messageId1.2"
      )

      val result = json.validate[DepartureMovementMessages].asOpt.value

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
            |                "href":"/customs/transits/movements/departures/1/message/messageId1"
            |             }
            |         },
            |         "id":"messageId1",
            |         "received":"2022-11-10T10:32:51.459Z",
            |         "type":"IE015",
            |         "status" : "Success"
            |      },
            |      {
            |        "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/message/messageId2"
            |             }
            |         },
            |         "id":"messageId2",
            |         "received":"2022-11-10T11:32:51.459Z",
            |         "type":"IE028",
            |         "status" : "Success"
            |      },
            |      {
            |         "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/departures/1/message/messageId3"
            |             }
            |         },
            |         "id":"messageId3",
            |         "received":"2022-11-10T12:32:51.459Z",
            |         "type":"IE029",
            |         "status" : "Success"
            |      }
            |   ]
            |}
            |""".stripMargin)

      val expectedResult = DepartureMovementMessages(
        NonEmptyList.of(
          DepartureMessage(
            "messageId3",
            LocalDateTime.parse("2022-11-10T12:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
            DepartureMessageType.ReleasedForTransit,
            MessageStatus.Success
          ),
          DepartureMessage(
            "messageId2",
            LocalDateTime.parse("2022-11-10T11:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
            DepartureMessageType.AllocatedMRN,
            MessageStatus.Success
          ),
          DepartureMessage(
            "messageId1",
            LocalDateTime.parse("2022-11-10T10:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
            DepartureMessageType.DepartureNotification,
            MessageStatus.Success
          )
        ),
        ie015MessageId = "messageId1"
      )

      val result = json.validate[DepartureMovementMessages].asOpt.value

      result `mustBe` expectedResult
    }

    "must not deserialize when empty list" in {

      val json =
        Json.parse(
          s"""
             |{
             |   "messages":[]
             |}
             |""".stripMargin
        )

      val result = json.validate[DepartureMovementMessages].leftSide

      result.isError `mustBe` true
    }

    "must not deserialize when no IE015 is found" in {

      val json =
        Json.parse(
          s"""
             |{
             |   "messages":[
             |      {
             |        "_links":{
             |             "self":{
             |                "href":"/customs/transits/movements/departures/1/message/messageId2"
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
             |                "href":"/customs/transits/movements/departures/1/message/messageId3"
             |             }
             |         },
             |         "id":"messageId3",
             |         "received":"2022-11-10T12:32:51.459Z",
             |         "type":"IE029",
             |         "status" : "Success"
             |      }
             |   ]
             |}
             |""".stripMargin
        )

      val result = json.validate[DepartureMovementMessages].leftSide

      result.isError `mustBe` true
    }
  }

}
