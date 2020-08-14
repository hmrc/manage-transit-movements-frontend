package models

import java.time.LocalDateTime

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Reads, __}

case class Departure (departureID: DepartureId, created: LocalDateTime, localReferenceNumber: LocalReferenceNumber, officeOfDeparture: String, status: String)

object Departure {
  implicit val reads: Reads[Departure] = (
    (__ \ "departureId").read[DepartureId] and
      (__ \ "created").read[LocalDateTime] and
      (__ \ "localReferenceNumber").read[LocalReferenceNumber] and
      (__ \ "officeOfDeparture").read[String] and
      (__ \ "status").read[String]
  )(Departure.apply _)
}

case class Departures (departures: Seq[Departure])

object Departures {
  implicit val format: Reads[Departures] = Json.reads[Departures]
}
