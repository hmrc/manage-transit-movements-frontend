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
import play.api.libs.json.Json

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ArrivalMovementsSpec extends SpecBase {

  "ArrivalMovementsSpec" - {

    "must deserialize" in {

      val json = Json.parse(
        """
          |{
          |  "_links": {
          |    "self": {
          |      "href": "/customs/transits/movements/arrivals"
          |    }
          |  },
          |  "totalCount": 2,
          |  "arrivals": [
          |    {
          |      "_links": {
          |        "self": {
          |          "href": "/customs/transits/movements/arrivals/63651574c3447b12"
          |        },
          |        "messages": {
          |          "href": "/customs/transits/movements/arrivals/63651574c3447b12/messages"
          |        }
          |      },
          |      "id": "63651574c3447b12",
          |      "movementReferenceNumber": "27WF9X1FQ9RCKN0TM3",
          |      "created": "2022-11-04T13:36:52.332Z",
          |      "updated": "2022-11-04T13:36:52.332Z",
          |      "enrollmentEORINumber": "9999912345",
          |      "movementEORINumber": "GB1234567890"
          |    },
          |    {
          |      "_links": {
          |        "self": {
          |          "href": "/customs/transits/movements/arrivals/6365135ba5e821ee"
          |        },
          |        "messages": {
          |          "href": "/customs/transits/movements/arrivals/6365135ba5e821ee/messages"
          |        }
          |      },
          |      "id": "6365135ba5e821ee",
          |      "movementReferenceNumber": "27WF9X1FQ9RCKN0TM3",
          |      "created": "2022-11-04T13:27:55.522Z",
          |      "updated": "2022-11-04T13:27:55.522Z",
          |      "enrollmentEORINumber": "9999912345",
          |      "movementEORINumber": "GB1234567890"
          |    }
          |  ]
          |}
          |""".stripMargin
      )

      val expectedResult = ArrivalMovements(
        arrivalMovements = Seq(
          ArrivalMovement(
            "63651574c3447b12",
            "27WF9X1FQ9RCKN0TM3",
            LocalDateTime.parse("2022-11-04T13:36:52.332Z", DateTimeFormatter.ISO_DATE_TIME)
          ),
          ArrivalMovement(
            "6365135ba5e821ee",
            "27WF9X1FQ9RCKN0TM3",
            LocalDateTime.parse("2022-11-04T13:27:55.522Z", DateTimeFormatter.ISO_DATE_TIME)
          )
        ),
        totalCount = 2
      )

      val result: ArrivalMovements = json.validate[ArrivalMovements].asOpt.value

      result mustBe expectedResult
    }
  }
}
