/*
 * Copyright 2020 HM Revenue & Customs
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

package models.referenceData

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, LocalTime, OffsetDateTime}

import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.Format

case class Movement(date: LocalDate, time: LocalTime, movementReferenceNumber: String)

object Movement {

  implicit val localTimeReads: Reads[LocalTime] = new Reads[LocalTime] {
    override def reads(json: JsValue): JsResult[LocalTime] = json match {
      case JsString(value) => JsSuccess(LocalTime.parse(value, Format.timeFormatter))
      case _               => JsError()
    }
  }

  implicit val localDateReads: Reads[LocalDate] = new Reads[LocalDate] {
    override def reads(json: JsValue): JsResult[LocalDate] = json match {
      case JsString(value) => JsSuccess(LocalDate.parse(value, Format.dateFormatter))
      case _               => JsError()
    }
  }

  implicit val reads: Reads[Movement] = (
    (__ \ "date").read[LocalDate] and
      (__ \ "time").read[LocalTime] and
      (__ \ "movementReferenceNumber").read[String]
  )(Movement.apply _)
}
