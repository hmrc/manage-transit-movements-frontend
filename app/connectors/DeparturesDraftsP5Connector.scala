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
import models.LockCheck.{LockCheckFailure, Locked, Unlocked}
import models.departure.drafts.{Limit, Skip}
import models.{Availability, DeparturesSummary, LockCheck, Sort}
import play.api.Logging
import play.api.http.Status.{LOCKED, OK}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.UpstreamErrorResponse.{Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeparturesDraftsP5Connector @Inject() (config: FrontendAppConfig, http: HttpClientV2)(implicit ec: ExecutionContext) extends Logging {

  private def getDeparturesSummary(queryParams: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] = {
    val url = url"${config.departureCacheUrl}/user-answers"

    http
      .get(url)
      .transform(_.withQueryStringParameters(queryParams :+ ("state" -> "notSubmitted")*))
      .execute[DeparturesSummary]
      .map(Some(_))
      .recover {
        case Upstream4xxResponse(e) =>
          logger.info(s"getDeparturesSummary failed to return data: ${e.getMessage}")
          None
        case Upstream5xxResponse(e) =>
          logger.warn(s"getDeparturesSummary failed to return data: ${e.getMessage}")
          None
      }
  }

  def getAllDeparturesSummary(limit: Limit, skip: Skip)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] =
    getDeparturesSummary(Seq("limit" -> limit.value.toString, "skip" -> skip.value.toString))

  def getLRNs(partialLRN: String, skip: Skip, limit: Limit)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] =
    getDeparturesSummary(Seq("lrn" -> partialLRN, "limit" -> limit.value.toString, "skip" -> skip.value.toString))

  def lrnFuzzySearch(lrn: String, limit: Limit)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] =
    getDeparturesSummary(Seq("lrn" -> lrn, "limit" -> limit.value.toString))

  def sortDraftDepartures(sortParams: Sort, limit: Limit, skip: Skip)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] =
    getDeparturesSummary(Seq("limit" -> limit.value.toString, "skip" -> skip.value.toString, "sortBy" -> sortParams.convertParams))

  def sortDraftDepartures(sortParams: Sort, limit: Limit, skip: Skip, lrn: String)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] =
    getDeparturesSummary(Seq("limit" -> limit.value.toString, "skip" -> skip.value.toString, "sortBy" -> sortParams.convertParams, "lrn" -> lrn))

  def deleteDraftDeparture(lrn: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url = url"${config.departureCacheUrl}/user-answers/$lrn"

    http
      .delete(url)
      .execute[HttpResponse]
  }

  def getDraftDeparturesAvailability()(implicit hc: HeaderCarrier): Future[Availability] =
    getDeparturesSummary(Seq("limit" -> "1")).map(_.map(_.userAnswers)).map(Availability(_))

  def checkLock(lrn: String)(implicit hc: HeaderCarrier): Future[LockCheck] = {
    val url = url"${config.departureCacheUrl}/user-answers/$lrn/lock"

    http
      .get(url)
      .execute[HttpResponse]
      .map {
        _.status match {
          case OK     => Unlocked
          case LOCKED => Locked
          case _      => LockCheckFailure
        }
      }
  }

}
