/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import com.lucidchart.open.xtract.XmlReader
import config.FrontendAppConfig
import connectors.CustomHttpReads.rawHttpResponseHttpReads
import javax.inject.Inject
import models.arrival.{MessagesSummary, XMLSubmissionNegativeAcknowledgementMessage}
import models.{ArrivalId, Arrivals, ResponseMessage}
import play.api.http.HeaderNames
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpReadsTry, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class ArrivalMovementConnector @Inject()(config: FrontendAppConfig, http: HttpClient, ws: WSClient)(implicit ec: ExecutionContext) extends HttpReadsTry {
  private val channel: String = "web"

  def getArrivals()(implicit hc: HeaderCarrier): Future[Option[Arrivals]] = {
    val header = hc.withExtraHeaders(ChannelHeader(channel))

    val serviceUrl: String = s"${config.destinationUrl}/movements/arrivals"
    http
      .GET[Arrivals](serviceUrl)(HttpReads[Arrivals], header, ec)
      .map {
        case arrivals => Some(arrivals)
      }
      .recover {
        case _ => None
      }
  }

  def getPDF(arrivalId: ArrivalId, bearerToken: String)(implicit hc: HeaderCarrier): Future[WSResponse] = {
    val serviceUrl: String = s"${config.destinationUrl}/movements/arrivals/${arrivalId.index}/unloading-permission"

    ws.url(serviceUrl).withHttpHeaders(ChannelHeader(channel), ("Authorization", bearerToken)).get
  }

  def getSummary(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[MessagesSummary]] = {

    val serviceUrl: String = s"${config.destinationUrl}/movements/arrivals/${arrivalId.value}/messages/summary"
    val header             = hc.withExtraHeaders(ChannelHeader(channel))
    http.GET[HttpResponse](serviceUrl)(rawHttpResponseHttpReads, header, ec) map {
      case responseMessage if is2xx(responseMessage.status) => Some(responseMessage.json.as[MessagesSummary])
      case _                                                => None
    }
  }

  def getXMLSubmissionNegativeAcknowledgementMessage(rejectionLocation: String)(
    implicit hc: HeaderCarrier): Future[Option[XMLSubmissionNegativeAcknowledgementMessage]] = {
    val serviceUrl = s"${config.destinationBaseUrl}$rejectionLocation"
    val header     = hc.withExtraHeaders(ChannelHeader(channel))
    http.GET[HttpResponse](serviceUrl)(rawHttpResponseHttpReads, header, ec) map {
      case responseMessage if is2xx(responseMessage.status) =>
        val message: NodeSeq = responseMessage.json.as[ResponseMessage].message
        XmlReader.of[XMLSubmissionNegativeAcknowledgementMessage].read(message).toOption
      case _ => None
    }
  }

  object ChannelHeader {
    def apply(value: String): (String, String) = ("Channel", value)
  }

  object ContentTypeHeader {
    def apply(value: String): (String, String) = (HeaderNames.CONTENT_TYPE, value)
  }

  object AuthorizationHeader {
    def apply(value: String): (String, String) = (HeaderNames.AUTHORIZATION, value)
  }
}
