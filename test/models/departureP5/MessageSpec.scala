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
import play.api.libs.json.Json

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessageSpec extends SpecBase {

  "Message" - {

    "must deserialize" in {

      DepartureMessageType.values.map {
        messageType =>
          val json = Json.parse(
            s"""
              |{
              |   "_links":{
              |       "self":{
              |          "href":"/customs/transits/movements/departures/63498209a2d89ad8/messages/634982098f02f00a"
              |       },
              |       "arrival":{
              |          "href":"/customs/transits/movements/departures/63498209a2d89ad8"
              |       }
              |   },
              |   "id":"634982098f02f00a",
              |   "arrivalId":"63498209a2d89ad8",
              |   "received":"2022-11-10T15:32:51.459Z",
              |   "type": "${messageType.toString}"
              |}
              |""".stripMargin
          )

          val expectedResult = DepartureMessage(
            LocalDateTime.parse("2022-11-10T15:32:51.459Z", DateTimeFormatter.ISO_DATE_TIME),
            messageType,
            "movements/departures/63498209a2d89ad8/messages/634982098f02f00a",
            Nil
          )

          val result: DepartureMessage = json.validate[DepartureMessage].asOpt.value

          result mustBe expectedResult
      }
    }
  }
}
