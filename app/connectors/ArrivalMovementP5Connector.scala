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
import play.api.libs.json.{JsError, JsSuccess}
import scalaxb.XMLFormat
import scalaxb.`package`.fromXML
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.XML

class ArrivalMovementP5Connector @Inject() (
  config: FrontendAppConfig,
  http: HttpClientV2
)(implicit ec: ExecutionContext)
    extends MovementP5Connector {

  def getAllMovements()(implicit hc: HeaderCarrier): Future[Option[ArrivalMovements]] =
    getMovements()

  def getAvailability()(implicit hc: HeaderCarrier): Future[Availability] = {
    val queryParams = Seq("count" -> "1")
    getMovements(queryParams*).map(_.map(_.movements)).map(Availability(_))
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
    getMovements(queryParams*)
  }

  private def getMovements(queryParams: (String, String)*)(implicit hc: HeaderCarrier): Future[Option[ArrivalMovements]] = {
    val url = url"${config.commonTransitConventionTradersUrl}movements/arrivals?$queryParams"

    http
      .get(url)
      .setHeader(jsonAcceptHeader)
      .execute[HttpResponse]
      .map {
        response =>
          response.status match {
            case OK =>
              response.json.validateOpt[ArrivalMovements] match {
                case JsSuccess(value, _) =>
                  value
                case JsError(errors) =>
                  logger.warn(s"[ArrivalMovementP5Connector][getMovements]: $errors")
                  Some(ArrivalMovements(Seq.empty, 0))
              }
            case NOT_FOUND =>
              Some(ArrivalMovements(Seq.empty, 0))
            case e =>
              logger.warn(s"[ArrivalMovementP5Connector][getMovements]: $e")
              None
          }
      }
      .recover {
        case e =>
          logger.error(s"[ArrivalMovementP5Connector][getMovements]: $e")
          None
      }
  }

  def getLatestMessageForMovement(arrivalId: String)(implicit hc: HeaderCarrier): Future[LatestArrivalMessage] = {
    val queryParams = Seq("count" -> config.apiResults.toString)
    val url         = url"${config.commonTransitConventionTradersUrl}movements/arrivals/$arrivalId/messages?$queryParams"

    http
      .get(url)
      .setHeader(jsonAcceptHeader)
      .execute[LatestArrivalMessage]
  }

  def getMessage[T](
    arrivalId: String,
    messageId: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, format: XMLFormat[T]): Future[T] = {
    val url = url"${config.commonTransitConventionTradersUrl}movements/arrivals/$arrivalId/messages/$messageId/body"

    http
      .get(url)
      .setHeader(xmlAcceptHeader)
      .execute[HttpResponse]
      .map(_.body)
      .map(XML.loadString)
      .map(fromXML(_))
  }

}
