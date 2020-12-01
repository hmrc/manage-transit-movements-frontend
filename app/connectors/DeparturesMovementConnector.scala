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
import models.{DepartureId, Departures, MessagesSummary}
import play.api.Logger
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class DeparturesMovementConnector @Inject()(config: FrontendAppConfig, http: HttpClient, ws: WSClient)(implicit ec: ExecutionContext) {

  def getDepartures()(implicit hc: HeaderCarrier): Future[Option[Departures]] = {
    val serviceUrl: String = s"${config.departureUrl}/movements/departures"
    http
      .GET[Departures](serviceUrl)
      .map(departures => Some(departures))
      .recover {
        case _ => None
      }
  }

  def getSummary(departureId: DepartureId)(implicit hc: HeaderCarrier): Future[Option[MessagesSummary]] = {

    val serviceUrl: String = s"${config.departureUrl}/movements/departures/${departureId.index}/messages/summary"
    http.GET[HttpResponse](serviceUrl) map {
      case responseMessage if is2xx(responseMessage.status) =>
        Some(responseMessage.json.as[MessagesSummary])
      case _ =>
        Logger.error(s"Get Summary failed to return data")
        None
    }
  }
}
