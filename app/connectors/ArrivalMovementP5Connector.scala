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
import models.arrivalP5.{ArrivalMovements, LatestArrivalMessage}
import play.api.http.Status.{NOT_FOUND, OK}
import scalaxb.XMLFormat
import scalaxb.`package`.fromXML
import sttp.model.HeaderNames
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.XML

class ArrivalMovementP5Connector @Inject() (config: FrontendAppConfig, http: HttpClientV2)(implicit ec: ExecutionContext) extends MovementP5Connector {

  def getAllMovements()(implicit hc: HeaderCarrier): Future[Option[ArrivalMovements]] =
    getMovements(Seq.empty)

  def getAvailability()(implicit hc: HeaderCarrier): Future[Availability] = {
    val queryParams = Seq("count" -> "1")
    getMovements(queryParams).map(_.map(_.movements)).map(Availability(_))
  }

  def getAllMovementsForSearchQuery(
    page: Int,
    resultsPerPage: Int,
    searchParam: Option[String]
  )(implicit hc: HeaderCarrier): Future[Option[ArrivalMovements]] = {
    val queryParams = Seq(
      "page"                    -> Some(page),
      "count"                   -> Some(resultsPerPage),
      "movementReferenceNumber" -> searchParam
    ).flatMap {
      case (key, Some(value)) => Some((key, value.toString))
      case _                  => None
    }
    getMovements(queryParams)
  }

  private def getMovements(queryParams: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[Option[ArrivalMovements]] = {
    val url    = url"${config.commonTransitConventionTradersUrl}movements/arrivals"
    val header = HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"

    http
      .get(url)
      .setHeader(header)
      .transform(_.withQueryStringParameters(queryParams: _*))
      .execute[HttpResponse]
      .map {
        response =>
          response.status match {
            case OK        => response.json.asOpt[ArrivalMovements]
            case NOT_FOUND => Some(ArrivalMovements(Seq.empty, 0))
            case _         => None
          }
      }
      .recover {
        case e =>
          logger.error(s"Failed to get arrival movements with error: $e")
          None
      }
  }

  def getLatestMessageForMovement(location: String)(implicit hc: HeaderCarrier): Future[LatestArrivalMessage] = {
    val url    = url"${config.commonTransitConventionTradersUrl}$location"
    val header = HeaderNames.Accept -> "application/vnd.hmrc.2.0+json"

    http
      .get(url)
      .setHeader(header)
      .execute[LatestArrivalMessage]
  }

  def getMessage[T](
    arrivalId: String,
    messageId: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, format: XMLFormat[T]): Future[T] = {
    val url    = url"${config.commonTransitConventionTradersUrl}movements/arrivals/$arrivalId/messages/$messageId/body"
    val header = HeaderNames.Accept -> "application/vnd.hmrc.2.0+xml"

    http
      .get(url)
      .setHeader(header)
      .execute[HttpResponse]
      .map(_.body)
      .map(XML.loadString)
      .map(fromXML(_))
  }

}
