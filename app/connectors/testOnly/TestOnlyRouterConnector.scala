package connectors.testOnly

import config.FrontendAppConfig
import javax.inject.Inject
import play.api.mvc.Headers
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class TestOnlyRouterConnector @Inject()(
  val http: HttpClient,
  config: FrontendAppConfig
)(implicit ec: ExecutionContext) {

  def sendMessage(
    xMessageSender: String,
    resquestData: NodeSeq,
    headers: Headers
  )(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val serviceUrl =
      s"${config.destinationUrl}/movements/arrivals/$xMessageSender/messages/eis"

    // TODO: Determine which headers need to be sent on
    http.POSTString[HttpResponse](
      serviceUrl,
      resquestData.toString,
      headers.headers
    )
  }
}
