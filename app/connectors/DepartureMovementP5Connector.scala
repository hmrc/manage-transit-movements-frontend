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
import models.Availability
import models.departureP5._
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpReadsTry, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureMovementP5Connector @Inject() (config: FrontendAppConfig, http: HttpClient)(implicit ec: ExecutionContext) extends HttpReadsTry with Logging {

  private def headers(implicit hc: HeaderCarrier): HeaderCarrier = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

  def getAllMovements()(implicit hc: HeaderCarrier): Future[Option[DepartureMovements]] =
    getMovements(Seq.empty)

  def getAvailability()(implicit hc: HeaderCarrier): Future[Availability] = {
    val queryParams = Seq("count" -> "1")
    getMovements(queryParams).map(_.map(_.movements)).map(Availability(_))
  }

  def getAllMovementsForSearchQuery(
    page: Int,
    resultsPerPage: Int,
    searchParam: Option[String]
  )(implicit hc: HeaderCarrier): Future[Option[DepartureMovements]] = {
    val queryParams = Seq(
      "page"                 -> Some(page),
      "count"                -> Some(resultsPerPage),
      "localReferenceNumber" -> searchParam
    ).flatMap {
      case (key, Some(value)) => Some((key, value.toString))
      case _                  => None
    }
    getMovements(queryParams)
  }

  private def getMovements(queryParams: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[Option[DepartureMovements]] = {
    val url = s"${config.commonTransitConventionTradersUrl}movements/departures"
    http
      .GET[HttpResponse](url, queryParams)(rawHttpResponseHttpReads, headers, ec)
      .map {
        response =>
          response.status match {
            case OK        => response.json.asOpt[DepartureMovements]
            case NOT_FOUND => Some(DepartureMovements(Seq.empty, 0))
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
    val url = s"${config.commonTransitConventionTradersUrl}$location"
    http.GET[MessagesForDepartureMovement](url)(HttpReads[MessagesForDepartureMovement], headers, ec)
  }

  def getMessageMetaData(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DepartureMessages] = {
    val url = s"${config.commonTransitConventionTradersUrl}movements/departures/$departureId/messages"
    http.GET[DepartureMessages](url)(implicitly, headers, ec)
  }

  def getSpecificMessageByPath[MessageModel](
    path: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, HttpReads: HttpReads[MessageModel]): Future[MessageModel] = {
    val url = s"${config.commonTransitConventionTradersUrl}$path"
    http.GET[MessageModel](url)(implicitly, headers, ec)
  }

  def getMessageMetaDataForMessageId(departureId: String, messageId: String)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Option[DepartureMessageMetaData]] = {
    val url = s"${config.commonTransitConventionTradersUrl}movements/departures/$departureId/messages/$messageId"
    http
      .GET[DepartureMessageMetaData](url)(implicitly, headers, ec)
      .map {
        case response => Some(response)
        case _        => None
      }
      .recover {
        case e =>
          logger.error(s"Failed to get arrival movements with error: $e")
          None
      }
  }
}
