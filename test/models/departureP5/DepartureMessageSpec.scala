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

class DepartureMessageSpec extends SpecBase {

  "must deserialise" in {

    val json = Json.parse("""
          |{
          |    "_links": {
          |        "self": {
          |            "href": "/customs/transits/movements/departures/64450051db689fad/messages/6445005176e4e834"
          |        },
          |        "departure": {
          |            "href": "/customs/transits/movements/departures/64450051db689fad"
          |        }
          |    },
          |    "id": "6445005176e4e834",
          |    "received": "2023-04-23T09:54:25.000Z",
          |    "type": "IE056",
          |    "body": {
          |        "n1:CC056C": {}
          |    }
          |}
          |""".stripMargin)

    json.as[DepartureMessage] mustBe DepartureMessage(
      messageId = "6445005176e4e834",
      received = LocalDateTime.of(2023, 4, 23, 9, 54, 25),
      messageType = DepartureMessageType.RejectedByOfficeOfDeparture,
      bodyPath = "movements/departures/64450051db689fad/messages/6445005176e4e834"
    )
  }

}
