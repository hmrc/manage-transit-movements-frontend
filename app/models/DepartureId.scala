package models

import play.api.libs.json.{JsNumber, Reads, __}
import play.api.mvc.PathBindable

case class DepartureId(index: Int)

object DepartureId {
  implicit def reads: Reads[DepartureId] = __.read[Int] map DepartureId.apply

  implicit def writes(departureId: DepartureId): JsNumber = JsNumber(departureId.index)

  implicit lazy val pathBindable: PathBindable[DepartureId] = new PathBindable[DepartureId] {
    override def bind(key: String, value: String): Either[String, DepartureId] =
      implicitly[PathBindable[Int]].bind(key, value).right.map(DepartureId(_))

    override def unbind(key: String, value: DepartureId): String =
      value.index.toString
  }
}