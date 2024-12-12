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

package services

import connectors.DeparturesDraftsP5Connector
import models.departure.drafts.{Limit, Skip}
import models.{DeparturesSummary, LockCheck, Sort}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.Future

class DraftDepartureService @Inject() (connector: DeparturesDraftsP5Connector) {

  def getLRNs(lrn: String, limit: Limit)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] =
    connector.lrnFuzzySearch(lrn, limit)

  def deleteDraftDeparture(lrn: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    connector.deleteDraftDeparture(lrn)

  def checkLock(lrn: String)(implicit hc: HeaderCarrier): Future[LockCheck] =
    connector.checkLock(lrn)

  def sortOrGetDrafts(
    lrn: Option[String],
    sortParams: Option[Sort],
    limit: Limit,
    skip: Skip
  )(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] = (lrn, sortParams) match {
    case (Some(lrn), Some(sortParams)) =>
      connector.sortDraftDepartures(sortParams, limit, skip, lrn)
    case (Some(lrn), None) =>
      connector.getLRNs(lrn, skip, limit)
    case (None, Some(sortParams)) =>
      connector.sortDraftDepartures(sortParams, limit, skip)
    case _ =>
      connector.getAllDeparturesSummary(limit, skip)
  }
}
