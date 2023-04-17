/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.CustomHttpReads.rawHttpResponseHttpReads
import logging.Logging
import models.departureP5.{DepartureMovements, IE060Data, Messages, MessagesForDepartureMovement}
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpReadsTry, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureMovementP5Connector @Inject() (config: FrontendAppConfig, http: HttpClient)(implicit ec: ExecutionContext) extends HttpReadsTry with Logging {

  def getAllMovements()(implicit hc: HeaderCarrier): Future[Option[DepartureMovements]] = {

    val headers = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

    val url = s"${config.commonTransitConventionTradersUrl}movements/departures"

    http
      .GET[HttpResponse](url)(rawHttpResponseHttpReads, headers, ec)
      .map {
        response =>
          response.status match {
            case OK        => response.json.asOpt[DepartureMovements]
            case NOT_FOUND => Some(DepartureMovements(Seq.empty))
            case _         => None
          }
      }
      .recover {
        case e =>
          logger.error(s"Failed to get departure movements with error: $e")
          None
      }
  }

  def getMessagesForMovement(location: String)(implicit hc: HeaderCarrier): Future[MessagesForDepartureMovement] = {

    val headers = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

    val url = s"${config.commonTransitConventionTradersUrl}$location"

    http.GET[MessagesForDepartureMovement](url)(HttpReads[MessagesForDepartureMovement], headers, ec)
  }

  def getMessageMetaData(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Messages] = {
    val headers = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

    val serviceUrl = s"${config.commonTransitConventionTradersUrl}movements/departures/$departureId/messages"

    http.GET[Messages](serviceUrl)(implicitly, headers, ec)
  }

  def getGoodsUnderControl(path: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[IE060Data] = {
    val headers = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

    val serviceUrl = s"${config.commonTransitConventionTradersUrl}$path"

    http.GET[IE060Data](serviceUrl)(implicitly, headers, ec)
  }
}
