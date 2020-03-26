package models

import java.time.{LocalDate, LocalTime}

import play.api.libs.json.{Json, OFormat}

case class ArrivalDateTime(date: LocalDate, time: LocalTime)

object ArrivalDateTime {
  implicit val format: OFormat[ArrivalDateTime] = Json.format[ArrivalDateTime]
}
