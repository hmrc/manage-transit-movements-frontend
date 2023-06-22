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
import models.arrivalP5.{ArrivalMessages, ArrivalMovements, MessagesForArrivalMovement}
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpReadsTry, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ArrivalMovementP5Connector @Inject() (config: FrontendAppConfig, http: HttpClient)(implicit ec: ExecutionContext) extends HttpReadsTry with Logging {

  private def headers(implicit hc: HeaderCarrier): HeaderCarrier = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

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
    val url = s"${config.commonTransitConventionTradersUrl}movements/arrivals"
    http
      .GET[HttpResponse](url, queryParams)(rawHttpResponseHttpReads, headers, ec)
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

  def getMessagesForMovement(location: String)(implicit hc: HeaderCarrier): Future[MessagesForArrivalMovement] = {
    val url = s"${config.commonTransitConventionTradersUrl}$location"
    http.GET[MessagesForArrivalMovement](url)(HttpReads[MessagesForArrivalMovement], headers, ec)
  }

  def getMessageMetaData(arrivalId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[ArrivalMessages] = {
    val url = s"${config.commonTransitConventionTradersUrl}movements/arrivals/$arrivalId/messages"
    http.GET[ArrivalMessages](url)(implicitly, headers, ec)
  }

  def getSpecificMessage[MessageModel](
    path: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, HttpReads: HttpReads[MessageModel]): Future[MessageModel] = {
    val url = s"${config.commonTransitConventionTradersUrl}$path"
    http.GET[MessageModel](url)(implicitly, headers, ec)
  }
}
