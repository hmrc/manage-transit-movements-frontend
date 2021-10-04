/*
 * Copyright 2021 HM Revenue & Customs
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
import models.departure.DepartureStatus.{DeclarationCancellationRequest, DepartureSubmitted, WriteOffNotification, XMLSubmissionNegativeAcknowledgement}
import models.departure.DepartureMessageMetaData
import play.api.libs.json.Json

import java.time.LocalDateTime

class DeparturesSpec extends SpecBase {

  "Departures" - {

    "must deserialize when there is a current and previous message" in {

      val localDateTime: LocalDateTime = LocalDateTime.now()

      val departuresResponseJson =
        Json.obj(
          "retrievedDepartures" -> 1,
          "totalDepartures"     -> 2,
          "totalMatched"        -> 3,
          "departures" ->
            Json.arr(
              Json.obj(
                "departureId"     -> 22,
                "updated"         -> localDateTime,
                "referenceNumber" -> "lrn",
                "messagesMetaData" -> Json.arr(
                  Json.obj(
                    "messageType" -> DeclarationCancellationRequest.toString,
                    "dateTime"    -> localDateTime
                  ),
                  Json.obj(
                    "messageType" -> DepartureSubmitted.toString,
                    "dateTime"    -> localDateTime.minusSeconds(10)
                  ),
                  Json.obj(
                    "messageType" -> WriteOffNotification.toString,
                    "dateTime"    -> localDateTime.minusMinutes(10)
                  ),
                  Json.obj(
                    "messageType" -> XMLSubmissionNegativeAcknowledgement.toString,
                    "dateTime"    -> localDateTime.minusDays(10)
                  )
                )
              )
            )
        )

      val expectedResult =
        Departures(
          1,
          2,
          Some(3),
          Seq(
            Departure(
              DepartureId(22),
              localDateTime,
              LocalReferenceNumber("lrn"),
              Seq(
                DepartureMessageMetaData(DeclarationCancellationRequest, localDateTime),
                DepartureMessageMetaData(DepartureSubmitted, localDateTime.minusSeconds(10)),
                DepartureMessageMetaData(WriteOffNotification, localDateTime.minusMinutes(10)),
                DepartureMessageMetaData(XMLSubmissionNegativeAcknowledgement, localDateTime.minusDays(10))
              )
            )
          )
        )

      departuresResponseJson.as[Departures] mustEqual expectedResult
    }

    "must fail to deserialize if there is no current message" in {

      val localDateTime: LocalDateTime = LocalDateTime.now()

      val departuresResponseJson =
        Json.obj(
          "retrievedDepartures" -> 1,
          "totalDepartures"     -> 2,
          "totalMatched"        -> 3,
          "departures" ->
            Json.arr(
              Json.obj(
                "departureId"     -> 22,
                "updated"         -> localDateTime,
                "referenceNumber" -> "lrn"
              )
            )
        )

      departuresResponseJson.validate[Departures].asOpt mustBe None
    }
  }
}
