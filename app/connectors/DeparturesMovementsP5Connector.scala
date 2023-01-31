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
import models.{DeparturesSummary, DraftAvailability}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeparturesMovementsP5Connector @Inject() (config: FrontendAppConfig, http: HttpClient)(implicit ec: ExecutionContext) extends Logging {

  def getDeparturesSummary(queryParams: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[Option[DeparturesSummary]] = {
    val url = s"${config.draftDeparturesUrl}/user-answers"

    http
      .GET[DeparturesSummary](url, queryParams)(HttpReads[DeparturesSummary], hc, ec)
      .map(
        departureSummaries => Some(departureSummaries)
      )
      .recover {
        case _ =>
          logger.error(s"get Departure Summary failed to return data")
          None
      }
  }

  def getDraftDeparturesAvailability()(implicit hc: HeaderCarrier): Future[DraftAvailability] =
    getDeparturesSummary(Seq("limit" -> "1")).map(DraftAvailability(_))

}
