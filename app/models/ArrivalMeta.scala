package models

import play.api.libs.json.{Json, OFormat}

case class ArrivalMeta(created: ArrivalDateTime, updated: ArrivalDateTime)

object ArrivalMeta {
  implicit val format: OFormat[ArrivalMeta] = Json.format[ArrivalMeta]
}
