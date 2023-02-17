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
import models.arrivalP5.{ArrivalMovements, MessagesForMovement}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpReadsTry}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ArrivalMovementP5Connector @Inject() (config: FrontendAppConfig, http: HttpClient)(implicit ec: ExecutionContext) extends HttpReadsTry with Logging {

  def getAllMovements()(implicit hc: HeaderCarrier): Future[Option[ArrivalMovements]] = {

    val headers = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

    val url = s"${config.commonTransitConventionTradersUrl}movements/arrivals"

    http
      .GET[ArrivalMovements](url)(HttpReads[ArrivalMovements], headers, ec)
      .map(Some(_))
      .recover {
        case e =>
          logger.error(s"Failed to get arrival movements with error: $e")
          None
      }
  }

  def getMessagesForMovement(location: String)(implicit hc: HeaderCarrier): Future[MessagesForMovement] = {

    val headers = hc.withExtraHeaders(("Accept", "application/vnd.hmrc.2.0+json"))

    val url = s"${config.commonTransitConventionTradersUrl}$location"

    http.GET[MessagesForMovement](url)(HttpReads[MessagesForMovement], headers, ec)
  }
}
