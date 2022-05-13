/*
 * Copyright 2022 HM Revenue & Customs
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
import logging.Logging
import models.departure.{ControlDecision, NoReleaseForTransitMessage}
import models.DepartureId
import models.arrival.XMLSubmissionNegativeAcknowledgementMessage
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureMessageService @Inject() (connector: DeparturesMovementConnector)(implicit ec: ExecutionContext) extends Logging {

  def noReleaseForTransitMessage(departureId: DepartureId)(implicit hc: HeaderCarrier): Future[Option[NoReleaseForTransitMessage]] =
    connector.getSummary(departureId) flatMap {
      case Some(summary) =>
        summary.messagesLocation.noReleaseForTransit match {
          case Some(location) =>
            connector.getNoReleaseForTransitMessage(location)
          case _ =>
            logger.error(s"Get Summary failed to get noReleaseForTransit location")
            Future.successful(None)
        }
      case _ =>
        logger.error(s"Get Summary failed to return data")
        Future.successful(None)
    }

  def controlDecisionMessage(departureId: DepartureId)(implicit hc: HeaderCarrier): Future[Option[ControlDecision]] =
    connector.getSummary(departureId) flatMap {
      case Some(summary) =>
        summary.messagesLocation.controlDecision match {
          case Some(location) =>
            connector.getControlDecisionMessage(location)
          case _ =>
            logger.error(s"Get Summary failed to get controlDecision location")
            Future.successful(None)
        }
      case _ =>
        logger.error(s"Get Summary failed to return data")
        Future.successful(None)
    }

  def getXMLSubmissionNegativeAcknowledgementMessage(
    departureId: DepartureId
  )(implicit hc: HeaderCarrier): Future[Option[XMLSubmissionNegativeAcknowledgementMessage]] =
    connector.getSummary(departureId) flatMap {
      case Some(summary) =>
        summary.messagesLocation.xmlSubmissionNegativeAcknowledgement match {
          case Some(negativeAcknowledgementLocation) =>
            connector.getXMLSubmissionNegativeAcknowledgementMessage(negativeAcknowledgementLocation)
          case _ =>
            logger.error(s"Get Summary failed to get XMLSubmissionNegativeAcknowledgement location")
            Future.successful(None)
        }
      case _ =>
        logger.error(s"Get Summary failed to get Data")
        Future.successful(None)
    }
}
