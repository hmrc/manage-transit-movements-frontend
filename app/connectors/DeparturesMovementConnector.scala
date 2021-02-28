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

import config.FrontendAppConfig

import javax.inject.Inject
import models.departure.{ControlDecision, MessagesSummary, NoReleaseForTransitMessage}
import models.{DepartureId, Departures, ResponseMessage}
import play.api.http.HeaderNames
import play.api.libs.ws.{WSClient, WSResponse}
import CustomHttpReads.rawHttpResponseHttpReads
import com.lucidchart.open.xtract.XmlReader
import logging.Logging
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class DeparturesMovementConnector @Inject()(config: FrontendAppConfig, http: HttpClient, ws: WSClient)(implicit ec: ExecutionContext) extends Logging {
  private val channel: String = "web"

  def getDepartures()(implicit hc: HeaderCarrier): Future[Option[Departures]] = {
    val serviceUrl: String = s"${config.departureUrl}/movements/departures"
    val header             = hc.withExtraHeaders(ChannelHeader(channel))

    http
      .GET[Departures](serviceUrl)(HttpReads[Departures], header, ec)
      .map(departures => Some(departures))
      .recover {
        case _ =>
          logger.error(s"get Departures failed to return data")
          None
      }
  }

  def getPDF(departureId: DepartureId)(implicit hc: HeaderCarrier): Future[WSResponse] = {
    val serviceUrl: String = s"${config.departureUrl}/movements/departures/${departureId.index}/transit-accompanying-document"
    val headers = Seq(
      "User-Agent" -> s"${config.manageUrl}",
      "Channel"    -> channel
    ) ++ hc.headers
    ws.url(serviceUrl).withHttpHeaders(headers: _*).get
  }

  def getSummary(departureId: DepartureId)(implicit hc: HeaderCarrier): Future[Option[MessagesSummary]] = {

    val serviceUrl: String = s"${config.departureUrl}/movements/departures/${departureId.index}/messages/summary"
    val header             = hc.withExtraHeaders(ChannelHeader(channel))

    http.GET[HttpResponse](serviceUrl)(rawHttpResponseHttpReads, header, ec) map {
      case responseMessage if is2xx(responseMessage.status) =>
        Some(responseMessage.json.as[MessagesSummary])
      case _ =>
        logger.error(s"Get Summary failed to return data")
        None
    }
  }

  def getNoReleaseForTransitMessage(location: String)(implicit hc: HeaderCarrier): Future[Option[NoReleaseForTransitMessage]] = {
    val serviceUrl = s"${config.departureBaseUrl}$location"
    val header     = hc.withExtraHeaders(ChannelHeader(channel))

    http.GET[HttpResponse](serviceUrl)(rawHttpResponseHttpReads, header, ec) map {
      case responseMessage if is2xx(responseMessage.status) =>
        val message: NodeSeq = responseMessage.json.as[ResponseMessage].message
        XmlReader.of[NoReleaseForTransitMessage].read(message).toOption
      case _ =>
        logger.error(s"NoReleaseForTransitMessage failed to return data")
        None
    }
  }

  def getControlDecisionMessage(location: String)(implicit hc: HeaderCarrier): Future[Option[ControlDecision]] = {
    val serviceUrl = s"${config.departureBaseUrl}$location"
    val header     = hc.withExtraHeaders(ChannelHeader(channel))

    http.GET[HttpResponse](serviceUrl)(rawHttpResponseHttpReads, header, ec) map {
      case responseMessage if is2xx(responseMessage.status) =>
        val message: NodeSeq = responseMessage.json.as[ResponseMessage].message
        XmlReader.of[ControlDecision].read(message).toOption
      case _ =>
        logger.error(s"ControlDecision failed to return data")
        None
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
