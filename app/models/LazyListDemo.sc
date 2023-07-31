import models.departureP5.FunctionalError
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


def max(result: LazyFunctionalErrors, itemsPerPage: Int) =
  if(result.errors.length > itemsPerPage) {
    itemsPerPage
  } else {
    result.errors.length
  }

val result = json.as[LazyFunctionalErrors]

result.errors.zipWithIndex
result.errors.take(1).head


// How to use with pagination?
// How do we persist the pagination on refresh?
// We can slice up the lazy list but will need to keep track of the current page when refreshing as a refresh will hit the API again.
// We can use a similar pagination model to the one used for drafts.
// We can modify the API call to return a LazyList of errors amd paginate over that LazyList
val itemsPerPage: Int = 5
var index: Int        = 1
val endOfStream: Int  = max(result, itemsPerPage)

while (index < endOfStream) {
  val pagedErrors: Seq[FunctionalError] = result.errors.slice(index - 1, endOfStream)
  println(pagedErrors.foreach(f => s"Error: ${f.errorCode}"))
  index *= itemsPerPage
}
