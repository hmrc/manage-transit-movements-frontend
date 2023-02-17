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

import connectors.DeparturesMovementsP5Connector
import models.DeparturesSummary
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.Future

class MongoDraftDepartureService @Inject() (connector: DeparturesMovementsP5Connector) extends DraftDepartureService {

  override def getAll(queryParams: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] =
    connector.getDeparturesSummary(queryParams)

  override def getLRNs(lrn: String, limit: Int)(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] =
    connector.lrnFuzzySearch(lrn, limit)

  override def deleteDraftDeparture(lrn: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = connector.deleteDraftDeparture(lrn)

  override def checkLock(lrn: String)(implicit hc: HeaderCarrier): Future[Boolean] = connector.checkLock(lrn)
}
