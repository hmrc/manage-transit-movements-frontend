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
import play.api.libs.json._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DepartureMovementSpec extends SpecBase {

  "DepartureMovement" - {

    "must deserialize" in {

      val json = Json.parse("""
          |    {
          |      "_links": {
          |        "self": {
          |          "href": "/customs/transits/movements/departures/63651574c3447b12"
          |        },
          |        "messages": {
          |          "href": "/customs/transits/movements/departures/63651574c3447b12/messages"
          |        }
          |      },
          |      "id": "63651574c3447b12",
          |      "movementReferenceNumber": "27WF9X1FQ9RCKN0TM3",
          |      "created": "2022-11-04T13:36:52.332Z",
          |      "updated": "2022-11-04T13:36:52.332Z",
          |      "enrollmentEORINumber": "9999912345",
          |      "movementEORINumber": "GB1234567890"
          |    }""".stripMargin)

      val expectedResult = DepartureMovement(
        "63651574c3447b12",
        Some("27WF9X1FQ9RCKN0TM3"),
        LocalDateTime.parse("2022-11-04T13:36:52.332Z", DateTimeFormatter.ISO_DATE_TIME),
        "movements/departures/63651574c3447b12/messages"
      )

      val result: DepartureMovement = json.validate[DepartureMovement].asOpt.value

      result mustBe expectedResult
    }
  }
}
