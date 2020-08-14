package models

import play.api.libs.json._
import play.api.mvc.PathBindable

final case class LocalReferenceNumber(value: String) {
  override def toString: String = value
}

object LocalReferenceNumber {

  val maxLength: Int = 22
  private val lrnFormat = """^([a-zA-Z0-9-_]{1,22})$""".r

  def apply(input: String): Option[LocalReferenceNumber] =
    input match {
      case lrnFormat(input) => Some(new LocalReferenceNumber(input))
      case _ => None
    }

  implicit def reads: Reads[LocalReferenceNumber] = {
    __.read[String].map(LocalReferenceNumber.apply).flatMap{
      case Some(lrn) => Reads(_ => JsSuccess(lrn))
      case None => Reads(_ => JsError("Invalid Local Reference Number"))
    }
  }

  implicit def writes: Writes[LocalReferenceNumber] = Writes {
    lrn =>
      JsString(lrn.value)
  }

  implicit def pathBindable: PathBindable[LocalReferenceNumber] = new PathBindable[LocalReferenceNumber] {

    override def bind(key: String, value: String): Either[String, LocalReferenceNumber] =
      LocalReferenceNumber.apply(value).toRight("Invalid Local Reference Number")

    override def unbind(key: String, value: LocalReferenceNumber): String =
      value.toString
  }
}