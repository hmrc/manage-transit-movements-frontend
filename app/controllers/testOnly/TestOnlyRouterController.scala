package controllers.testOnly

import connectors.testOnly.TestOnlyRouterConnector
import javax.inject.Inject
import play.api.Logger
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class TestOnlyRouterController @Inject()(
                                   cc: ControllerComponents,
                                   connector: TestOnlyRouterConnector
                                 )(implicit val ec: ExecutionContext)
  extends BackendController(cc) {

  private lazy val logger = Logger(getClass)

  def handleMessage(): Action[NodeSeq] = Action.async(parse.xml) {
    implicit request =>
      request.headers.get("X-Message-Sender") match {
        case Some(xMessageSender) =>
          connector
            .sendMessage(xMessageSender, request.body, request.headers)
            .map(response => Status(response.status))
        case None =>
          logger.error("BadRequest: missing header key X-Message-Sender")
          Future.successful(BadRequest)
      }
  }
}

