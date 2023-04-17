package controllers.actions

import models.DepartureId
import models.departureP5._
import models.requests.{GoodsUnderControlRequest, IdentifierRequest}
import play.api.mvc.Result
import services.DepartureP5MessageService

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FakeGoodsUnderControlAction (departureId: DepartureId, departureP5MessageService: DepartureP5MessageService)
  extends GoodsUnderControlAction(departureId, departureP5MessageService) {

  val message: IE060Data = IE060Data(
    IE060MessageData(
      TransitOperation(Some("CD3232"), Some("AB123"), LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "notification1"),
      CustomsOfficeOfDeparture("22323323"),
      Some(Seq(TypeOfControls("1", "type1", Some("text1")), TypeOfControls("2", "type2", None))),
      Some(Seq(RequestedDocument("3", "doc1", Some("desc1")), RequestedDocument("4", "doc2", None)))
    ))

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, GoodsUnderControlRequest[A]]] =
    Future.successful(Right(GoodsUnderControlRequest(request, "AB123", message.data)))

}