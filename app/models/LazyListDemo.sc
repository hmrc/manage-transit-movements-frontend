import models.departureP5.{FunctionalError, LazyFunctionalErrors}
import play.api.libs.json.{Json, OFormat}

case class LazyFunctionalErrors(errors: LazyList[FunctionalError])

object LazyFunctionalErrors {

  implicit val formats: OFormat[LazyFunctionalErrors] = Json.format[LazyFunctionalErrors]
}

val json = Json.parse(
  """
    |{
    | "errors":
    |   [
    |     {
    |         "errorPointer": "/CC014C",
    |         "errorCode": "12",
    |         "errorReason": "N/A"
    |    },
    |    {
    |        "errorPointer": "/CC015C/Authorisation[1]/referenceNumber",
    |        "errorCode": "14",
    |        "errorReason": "G0033",
    |        "originalAttributeValue": "XIDEP01"
    |    }
    | ]
    |}
    |""".stripMargin)


val result = json.as[LazyFunctionalErrors]

result.errors.zipWithIndex
result.errors.take(1).head
