/*
 * Copyright 2020 HM Revenue & Customs
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
import models.{ArrivalId, Arrivals}
import play.api.http.HeaderNames
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpReadsTry}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

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
