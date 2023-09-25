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
import models.{DeparturesSummary, LockCheck, Sort}
import models.departure.drafts.{Limit, Skip}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.Future

class MongoDraftDepartureService @Inject() (connector: DeparturesDraftsP5Connector) extends DraftDepartureService {

  override def getAll(queryParams: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] =
    connector.getDeparturesSummary(queryParams)

  override def getLRNs(lrn: String, limit: Limit)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] =
    connector.lrnFuzzySearch(lrn, limit)

  override def sortDraftDepartures(sortParams: Sort, limit: Limit, skip: Skip)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] =
    connector.sortDraftDepartures(sortParams, limit, skip)

  override def sortDraftDepartures(sortParams: Sort, limit: Limit, skip: Skip, lrn: String)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] =
    connector.sortDraftDepartures(sortParams, limit, skip, lrn)

  override def deleteDraftDeparture(lrn: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = connector.deleteDraftDeparture(lrn)

  override def getLRNs(partialLRN: String, skip: Skip, limit: Limit)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] =
    connector.getLRNs(partialLRN, skip, limit)

  override def getPagedDepartureSummary(limit: Limit, skip: Skip)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] =
    connector.getAllDeparturesSummary(limit, skip)

  override def checkLock(lrn: String)(implicit hc: HeaderCarrier): Future[LockCheck] = connector.checkLock(lrn)
}
