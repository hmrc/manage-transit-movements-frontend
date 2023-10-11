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
import models.arrivalP5.IE025Data
import models.departureP5.DepartureMessageType._
import models.departureP5._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureP5MessageService @Inject() (
  departureMovementP5Connector: DepartureMovementP5Connector,
  cacheConnector: DepartureCacheConnector
) {

  def isErrorAmendable(
    departureId: String,
    lrn: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[(Option[RejectionType], Boolean, Seq[String])] =
    for {
      message <- filterForMessage[IE056Data](departureId, RejectedByOfficeOfDeparture)
      rejectionType = message.map(_.data.transitOperation.businessRejectionType)
      xPaths        = message.map(_.data.functionalErrors.map(_.errorPointer))
      isDeclarationAmendable <- xPaths.filter(_.nonEmpty).fold(Future.successful(false)) {
        cacheConnector.isDeclarationAmendable(lrn, _)
      }
    } yield (rejectionType, isDeclarationAmendable, xPaths.getOrElse(Seq.empty))

  def getMessagesForAllMovements(
    departureMovements: DepartureMovements
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[DepartureMovementAndMessage]] =
    departureMovements.departureMovements.traverse {
      movement =>
        for {
          messagesForMovement <- departureMovementP5Connector.getMessagesForMovement(movement.messagesLocation)
          isAmendable         <- isErrorAmendable(movement.departureId, movement.localReferenceNumber.value)
        } yield DepartureMovementAndMessage(
          movement,
          messagesForMovement,
          movement.localReferenceNumber,
          isAmendable._1,
          isAmendable._2,
          isAmendable._3
        )
    }

  def getLatestMessagesForMovement(
    departureMovements: DepartureMovements
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[MovementAndMessage]] =
    departureMovements.departureMovements.traverse {
      movement =>
        departureMovementP5Connector.getLatestMessageForMovement(movement.messagesLocation).flatMap {
          message =>
            message.latestMessage.messageType match {
              case RejectedByOfficeOfDeparture =>
                isErrorAmendable(movement.departureId, movement.localReferenceNumber.value).map {
                  case (rejectionType, isDeclarationAmendable, xPaths) =>
                    RejectedMovementAndMessage(
                      movement.departureId,
                      movement.localReferenceNumber,
                      movement.updated,
                      message,
                      rejectionType,
                      isDeclarationAmendable,
                      xPaths
                    )
                }
              case DeclarationAmendmentAccepted | GoodsUnderControl | DeclarationSent =>
                departureMovementP5Connector.getMessageForMessageId[IE015Data](movement.departureId, message.ie015MessageId).map {
                  ie015 =>
                    PrelodgedMovementAndMessage(
                      movement.departureId,
                      movement.localReferenceNumber,
                      movement.updated,
                      message,
                      ie015.isPrelodged
                    )
                }
              case _ =>
                Future.successful(
                  OtherMovementAndMessage(
                    movement.departureId,
                    movement.localReferenceNumber,
                    movement.updated,
                    message
                  )
                )
            }
        }
    }

  def getSpecificMessageMetaData[T <: DepartureMessageType](departureId: String, typeOfMessage: T)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Option[DepartureMessageMetaData]] =
    getMessageMetaData(departureId, typeOfMessage)

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

  def filterForMessage[MessageModel](
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
        message         <- OptionT.liftF(departureMovementP5Connector.getSpecificMessageByPath[MessageModel](messageMetaData.path))
      } yield message
    ).value

  def getMessageWithMessageId[MessageModel](
    departureId: String,
    messageId: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, httpReads: HttpReads[MessageModel]): Future[MessageModel] =
    departureMovementP5Connector
      .getMessageForMessageId[MessageModel](departureId, messageId)
}
