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
import models.MessageStatus
import play.api.libs.json.Json

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LatestArrivalMessageSpec extends SpecBase {

  "LatestArrivalMessage" - {

    "must deserialize and find most recent IE007" in {

      val json =
        Json.parse(s"""
            |{
            |   "messages":[
            |     {
            |         "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/arrivals/1/message/messageId1.1"
            |             }
            |         },
            |         "id":"messageId1.1",
            |         "received":"2022-11-10T09:32:51.459Z",
            |         "type":"IE007",
            |         "status" : "Failed"
            |      },
            |      {
            |         "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/arrivals/1/message/messageId1.2"
            |             }
            |         },
            |         "id":"messageId1.2",
            |         "received":"2022-11-10T10:32:51.459Z",
            |         "type":"IE007",
            |         "status" : "Success"
            |      },
            |      {
            |       "_links":{
            |             "self":{
            |                "href":"/customs/transits/movements/arrivals/1/message/messageId2"
            |             }
            |         },
            |         "id":"messageId2",
            |         "received":"2022-11-10T11:32:51.459Z",
            |         "type":"IE043",
            |         "status" : "Success"
            |      }
            |   ]
            |}
            |""".stripMargin)

      val expectedResult = LatestArrivalMessage(
        ArrivalMessage(
          "messageId2",
          LocalDateTime.parse("2022-11-10T11:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
          ArrivalMessageType.UnloadingPermission,
          MessageStatus.Success
        ),
        ie007Id = "messageId1.2"
      )

      val result = json.validate[LatestArrivalMessage].asOpt.value

      result mustEqual expectedResult
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

      val result = json.validate[LatestArrivalMessage].leftSide

      result.isError mustEqual true
    }

    "must not deserialize when no IE007 is found" in {

      val json =
        Json.parse(
          s"""
             |{
             |   "messages":[
             |      {
             |        "_links":{
             |             "self":{
             |                "href":"/customs/transits/movements/arrivals/1/message/messageId2"
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
             |                "href":"/customs/transits/movements/arrivals/1/message/messageId3"
             |             }
             |         },
             |         "id":"messageId3",
             |         "received":"2022-11-10T12:32:51.459Z",
             |         "type":"IE043",
             |         "status" : "Success"
             |      }
             |   ]
             |}
             |""".stripMargin
        )

      val result = json.validate[LatestArrivalMessage].leftSide

      result.isError mustEqual true
    }
  }

}
