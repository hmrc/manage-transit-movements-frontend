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
import models.departureP5.*
import play.api.http.Status.*
import play.api.libs.json.{JsError, JsSuccess}
import scalaxb.XMLFormat
import scalaxb.`package`.fromXML
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.XML

class DepartureMovementP5Connector @Inject() (
  config: FrontendAppConfig,
  http: HttpClientV2
)(implicit ec: ExecutionContext)
    extends MovementP5Connector {

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
    val url = url"${config.commonTransitConventionTradersUrl}movements/departures"

    http
      .get(url)
      .setHeader(jsonAcceptHeader)
      .transform(_.withQueryStringParameters(queryParams*))
      .execute[HttpResponse]
      .map {
        response =>
          response.status match {
            case OK =>
              response.json.validateOpt[DepartureMovements] match {
                case JsSuccess(value, _) =>
                  value
                case JsError(errors) =>
                  logger.warn(s"[DepartureMovementP5Connector][getMovements]: $errors")
                  Some(DepartureMovements(Seq.empty, 0))
              }
            case NOT_FOUND =>
              Some(DepartureMovements(Seq.empty, 0))
            case e =>
              logger.warn(s"[DepartureMovementP5Connector][getMovements]: $e")
              None
          }
      }
      .recover {
        case e =>
          logger.error(s"[DepartureMovementP5Connector][getMovements]: $e")
          None
      }
  }

  def getLatestMessageForMovement(departureId: String)(implicit hc: HeaderCarrier): Future[LatestDepartureMessage] = {
    val url = url"${config.commonTransitConventionTradersUrl}movements/departures/$departureId/messages"

    http
      .get(url)
      .setHeader(jsonAcceptHeader)
      .transform(_.withQueryStringParameters("count" -> config.apiResults.toString))
      .execute[LatestDepartureMessage]
  }

  def getMessage[T](
    departureId: String,
    messageId: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, format: XMLFormat[T]): Future[T] = {
    val url = url"${config.commonTransitConventionTradersUrl}movements/departures/$departureId/messages/$messageId/body"

    http
      .get(url)
      .setHeader(xmlAcceptHeader)
      .execute[HttpResponse]
      .map(_.body)
      .map(XML.loadString)
      .map(fromXML(_))
  }

  def getDepartureReferenceNumbers(departureId: String)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[DepartureReferenceNumbers] = {
    val url = url"${config.commonTransitConventionTradersUrl}movements/departures/$departureId"

    http
      .get(url)
      .setHeader(jsonAcceptHeader)
      .execute[DepartureReferenceNumbers]
  }

}
