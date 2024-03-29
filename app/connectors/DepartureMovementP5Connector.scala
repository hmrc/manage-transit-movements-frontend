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
import models.Availability
import models.departureP5._
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Reads
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureMovementP5Connector @Inject() (config: FrontendAppConfig, http: HttpClient)(implicit ec: ExecutionContext) extends MovementP5Connector {

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

  def getLatestMessageForMovement(location: String)(implicit hc: HeaderCarrier): Future[LatestDepartureMessage] = {
    val url = s"${config.commonTransitConventionTradersUrl}$location"
    http.GET[LatestDepartureMessage](url)(HttpReads[LatestDepartureMessage], headers, ec)
  }

  def getMessageForMessageId[MessageModel](departureId: String, messageId: String)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier,
    reads: Reads[MessageModel]
  ): Future[MessageModel] = {
    val url = s"${config.commonTransitConventionTradersUrl}movements/departures/$departureId/messages/$messageId"
    http
      .GET[MessageModel](url)(messageModelHttpReads, headers, ec)

  }

  def getDepartureReferenceNumbers(departureId: String)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[DepartureReferenceNumbers] = {
    val url = s"${config.commonTransitConventionTradersUrl}movements/departures/$departureId"

    http.GET[DepartureReferenceNumbers](url)(implicitly, headers, ec)
  }

}
