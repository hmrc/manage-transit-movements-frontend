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

import models.departure.drafts.{Limit, Skip}
import models.{DeparturesSummary, LockCheck, Sort}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

trait DraftDepartureService {

  def getLRNs(partialLRN: String, limit: Limit)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]]
  def sortDraftDepartures(sortParams: Sort, limit: Limit, skip: Skip)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]]
  def sortDraftDepartures(sortParams: Sort, limit: Limit, skip: Skip, lrn: String)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]]
  def deleteDraftDeparture(lrn: String)(implicit hc: HeaderCarrier): Future[HttpResponse]
  def getLRNs(partialLRN: String, skip: Skip, limit: Limit)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]]
  def getPagedDepartureSummary(limit: Limit, skip: Skip)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]]

  def checkLock(lrn: String)(implicit hc: HeaderCarrier): Future[LockCheck]
}
