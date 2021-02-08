/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.DeparturesMovementConnector
import javax.inject.Inject
import models.DepartureId
import models.departure.NoReleaseForTransitMessage
import logging.Logging
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class DepartureMessageService @Inject()(connectors: DeparturesMovementConnector) extends Logging {

  def noReleaseForTransitMessage(departureId: DepartureId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[NoReleaseForTransitMessage]] =
    connectors.getSummary(departureId) flatMap {
      case Some(summary) =>
        summary.messagesLocation.noReleaseForTransit match {
          case Some(location) =>
            connectors.getNoReleaseForTransitMessage(location)
          case _ =>
            logger.error(s"Get Summary failed to get noReleaseForTransit location")
            Future.successful(None)
        }
      case _ =>
        logger.error(s"Get Summary failed to return data")
        Future.successful(None)
    }
}
