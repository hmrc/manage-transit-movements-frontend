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
import models.Availability
import models.departureP5._
import play.api.http.Status._
import scalaxb.XMLFormat
import scalaxb.`package`.fromXML
import sttp.model.HeaderNames
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.XML

class DepartureMovementP5Connector @Inject() (config: FrontendAppConfig, http: HttpClientV2)(implicit ec: ExecutionContext) extends MovementP5Connector {

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
    val url    = url"${config.commonTransitConventionTradersUrl}movements/departures"
    val header = HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"

    http
      .get(url)
      .setHeader(header)
      .transform(_.withQueryStringParameters(queryParams: _*))
      .execute[HttpResponse]
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

  def getLatestMessageForMovement(departureId: String)(implicit hc: HeaderCarrier): Future[LatestDepartureMessage] = {
    val url    = url"${config.commonTransitConventionTradersUrl}movements/departures/$departureId/messages"
    val header = HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"

    http
      .get(url)
      .setHeader(header)
      .execute[LatestDepartureMessage]
  }

  def getMessage[T](
    departureId: String,
    messageId: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, format: XMLFormat[T]): Future[T] = {
    val url    = url"${config.commonTransitConventionTradersUrl}movements/departures/$departureId/messages/$messageId/body"
    val header = HeaderNames.Accept -> "application/vnd.hmrc.2.0+xml"

    http
      .get(url)
      .setHeader(header)
      .execute[HttpResponse]
      .map(_.body)
      .map(XML.loadString)
      .map(fromXML(_))
  }

  def getDepartureReferenceNumbers(departureId: String)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[DepartureReferenceNumbers] = {
    val url    = url"${config.commonTransitConventionTradersUrl}movements/departures/$departureId"
    val header = HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"

    http
      .get(url)
      .setHeader(header)
      .execute[DepartureReferenceNumbers]
  }

}
