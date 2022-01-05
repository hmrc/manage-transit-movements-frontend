/*
 * Copyright 2022 HM Revenue & Customs
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

package models

import base.SpecBase
import models.arrival.ArrivalMessageMetaData
import models.arrival.ArrivalStatus.{ArrivalNotificationSubmitted, GoodsReleased, UnloadingPermission, XMLSubmissionNegativeAcknowledgement}
import play.api.libs.json.{JsArray, Json}

import java.time.LocalDateTime

class ArrivalsSpec extends SpecBase {

  "Arrivals" - {

    "must deserialize when there is a current and previous message" in {

      val dateNow = LocalDateTime.now()

      val json = Json.obj(
        "retrievedArrivals" -> 1,
        "totalArrivals"     -> 2,
        "totalMatched"      -> 3,
        "arrivals" -> JsArray(
          Seq(
            Json.obj(
              "arrivalId" -> 123,
              "created"   -> dateNow,
              "updated"   -> dateNow,
              "messagesMetaData" -> Json.arr(
                Json.obj(
                  "messageType" -> ArrivalNotificationSubmitted.toString,
                  "dateTime"    -> dateNow
                ),
                Json.obj(
                  "messageType" -> GoodsReleased.toString,
                  "dateTime"    -> dateNow.minusSeconds(10)
                ),
                Json.obj(
                  "messageType" -> UnloadingPermission.toString,
                  "dateTime"    -> dateNow.minusMinutes(10)
                ),
                Json.obj(
                  "messageType" -> XMLSubmissionNegativeAcknowledgement.toString,
                  "dateTime"    -> dateNow.minusDays(10)
                )
              ),
              "movementReferenceNumber" -> "mrn123"
            )
          )
        )
      )

      val expectedResult =
        Arrivals(
          1,
          2,
          Some(3),
          Seq(
            Arrival(
              ArrivalId(123),
              dateNow,
              dateNow,
              Seq(
                ArrivalMessageMetaData(ArrivalNotificationSubmitted, dateNow),
                ArrivalMessageMetaData(GoodsReleased, dateNow.minusSeconds(10)),
                ArrivalMessageMetaData(UnloadingPermission, dateNow.minusMinutes(10)),
                ArrivalMessageMetaData(XMLSubmissionNegativeAcknowledgement, dateNow.minusDays(10))
              ),
              "mrn123"
            )
          )
        )

      json.validate[Arrivals].asOpt.value mustEqual expectedResult
    }

    "must fail to deserialize if there is no current message" in {

      val json = Json.obj(
        "retrievedArrivals" -> 1,
        "totalArrivals"     -> 2,
        "totalMatched"      -> 3,
        "arrivals"          -> ""
      )

      json.validate[Departures].asOpt mustBe None
    }

  }

}
