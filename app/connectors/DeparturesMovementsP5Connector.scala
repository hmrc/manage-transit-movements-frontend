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
import logging.Logging
import models.departure.drafts.{Limit, Skip}
import models.{DeparturesSummary, DraftAvailability}
import play.api.http.Status.OK
import models.{DeparturesSummary, DraftAvailability, Sort}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeparturesMovementsP5Connector @Inject() (config: FrontendAppConfig, http: HttpClient)(implicit ec: ExecutionContext) extends Logging {

  def getDeparturesSummary(queryParams: Seq[(String, String)] = Seq.empty)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] = {
    val url = s"${config.draftDeparturesUrl}/user-answers"

    http
      .GET[DeparturesSummary](url, queryParams)
      .map(Some(_))
      .recover {
        case _ =>
          logger.error(s"get Departures Summary failed to return data")
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
    val url = s"${config.draftDeparturesUrl}/user-answers/$lrn"
    http.DELETE[HttpResponse](url)
  }

  def getDraftDeparturesAvailability()(implicit hc: HeaderCarrier): Future[DraftAvailability] =
    getDeparturesSummary(Seq("limit" -> "1")).map(DraftAvailability(_))

  def checkLock(lrn: String)(implicit hc: HeaderCarrier): Future[Boolean] = {

    val url = s"${config.draftDeparturesUrl}/user-answers/$lrn/lock"
    http
      .GET[HttpResponse](url)
      .map {
        _.status == OK
      }
  }

}
