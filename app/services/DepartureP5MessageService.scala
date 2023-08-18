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

import cats.data.OptionT
import cats.implicits._
import connectors.{DepartureCacheConnector, DepartureMovementP5Connector}
import models.RejectionType
import models.departureP5.DepartureMessageType.{DepartureNotification, _}
import models.departureP5._
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureP5MessageService @Inject() (
  departureMovementP5Connector: DepartureMovementP5Connector,
  cacheConnector: DepartureCacheConnector
) {

  def getMessagesForAllMovements(
    departureMovements: DepartureMovements
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[DepartureMovementAndMessage]] =
    departureMovements.departureMovements.traverse {
      movement =>
        departureMovementP5Connector
          .getMessagesForMovement(movement.messagesLocation)
          .flatMap {
            messagesForMovement =>
              messagesForMovement.messages.find(_.messageType == DepartureNotification) match {
                case Some(ie015) =>
                  for {
                    // TODO - the data will be manipulated in the backend to make the LRN more accessible in the frontend
                    lrn   <- departureMovementP5Connector.getLRN(ie015.bodyPath).map(_.referenceNumber)
                    ie056 <- getMessage[IE056Data](movement.departureId, RejectedByOfficeOfDeparture)
                    rejectionType = ie056.map(_.data.transitOperation.businessRejectionType)
                    xPaths = ie056.map(_.data.functionalErrors.map(_.errorPointer))
                    isDeclarationAmendable <- xPaths.filter(_.nonEmpty).fold(Future.successful(false))(cacheConnector.isDeclarationAmendable(lrn, _))
                  } yield DepartureMovementAndMessage(movement, messagesForMovement, lrn, rejectionType, isDeclarationAmendable, xPaths.getOrElse(Seq.empty))
                case None =>
                  Future.failed(new Throwable("Movement did not contain an IE015 message"))
              }
          }
    }

  private def getSpecificMessageMetaData[T <: DepartureMessageType](departureId: String, typeOfMessage: T)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Option[DepartureMessageMetaData]] =
    getMessageMetaData(departureId, typeOfMessage)

  private def getDepartureNotificationMetaData(
    departureId: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[DepartureMessageMetaData]] =
    getMessageMetaData(departureId, DepartureNotification)

  private def getMessageMetaData(departureId: String, messageType: DepartureMessageType)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Option[DepartureMessageMetaData]] =
    departureMovementP5Connector
      .getMessageMetaData(departureId)
      .map(
        _.messages
          .filter(_.messageType == messageType)
          .sortBy(_.received)
          .reverse
          .headOption
      )

  def getMessage[MessageModel](
    departureId: String,
    typeOfMessage: DepartureMessageType
  )(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier,
    httpReads: HttpReads[MessageModel]
  ): Future[Option[MessageModel]] =
    (
      for {
        messageMetaData <- OptionT(getSpecificMessageMetaData(departureId, typeOfMessage))
        message         <- OptionT.liftF(departureMovementP5Connector.getSpecificMessage[MessageModel](messageMetaData.path))
      } yield message
    ).value

  def getLRNFromDeclarationMessage(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[String]] =
    (
      for {
        declarationMessage <- OptionT(getDepartureNotificationMetaData(departureId))
        lrn                <- OptionT.liftF(departureMovementP5Connector.getLRN(declarationMessage.path).map(_.referenceNumber))
      } yield lrn
    ).value
}
