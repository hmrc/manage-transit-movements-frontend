package models

import play.api.libs.json.{Json, OFormat}

case class Arrival(meta: ArrivalMeta, movementReferenceNumber: String)

object Arrival {
  implicit val format: OFormat[Arrival] = Json.format[Arrival]
}
