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
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DepartureMessagesSpec extends SpecBase {

  "DepartureMessages" - {

    "deserialize from JSON" in {
      val dateTimeStr1 = "2024-09-18T10:00:00"
      val dateTimeStr2 = "2024-09-19T11:30:00"
      val formatter    = DateTimeFormatter.ISO_LOCAL_DATE_TIME

      val json = Json.obj(
        "messages" -> Json.arr(
          Json.obj(
            "received" -> dateTimeStr1,
            "type"     -> "IE015",
            "_links" -> Json.obj(
              "self" -> Json.obj("href" -> "/customs/transits/message1")
            )
          ),
          Json.obj(
            "received" -> dateTimeStr2,
            "type"     -> "IE014",
            "_links" -> Json.obj(
              "self" -> Json.obj("href" -> "/customs/transits/message2")
            )
          )
        )
      )

      val expectedMessages = DepartureMessages(
        List(
          DepartureMessageMetaData(
            LocalDateTime.parse(dateTimeStr1, formatter),
            DepartureMessageType.DepartureNotification,
            "message1"
          ),
          DepartureMessageMetaData(
            LocalDateTime.parse(dateTimeStr2, formatter),
            DepartureMessageType.CancellationRequested,
            "message2"
          )
        )
      )

      json.validate[DepartureMessages] mustEqual JsSuccess(expectedMessages)
    }
  }
}
