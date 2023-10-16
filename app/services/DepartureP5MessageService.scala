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
import models.departureP5.DepartureMessageType.{DeclarationAmendmentAccepted, DeclarationSent, GoodsUnderControl, RejectedByOfficeOfDeparture}
import models.departureP5._
import models.{LocalReferenceNumber, RejectionType}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureP5MessageService @Inject() (
  departureMovementP5Connector: DepartureMovementP5Connector,
  cacheConnector: DepartureCacheConnector
) {

  private def isErrorAmendable(
    departureId: String,
    messageId: String,
    lrn: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[(RejectionType, Boolean, Seq[String])] =
    for {
      message <- getMessageWithMessageId[IE056Data](departureId, messageId)
      rejectionType = message.data.transitOperation.businessRejectionType
      xPaths        = message.data.functionalErrors.map(_.errorPointer)
      isDeclarationAmendable <- cacheConnector.isDeclarationAmendable(lrn, xPaths.filter(_.nonEmpty))
    } yield (rejectionType, isDeclarationAmendable, xPaths)

  def getLatestMessagesForMovement(
    departureMovements: DepartureMovements
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[MovementAndMessage]] =
    departureMovements.departureMovements.traverse {
      movement =>
        departureMovementP5Connector.getLatestMessageForMovement(movement.messagesLocation).flatMap {
          message =>
            message.latestMessage.messageType match {
              case RejectedByOfficeOfDeparture =>
                isErrorAmendable(movement.departureId, message.latestMessage.messageId, movement.localReferenceNumber.value).map {
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

  def getMessageWithMessageId[MessageModel](
    departureId: String,
    messageId: String
  )(implicit ec: ExecutionContext, hc: HeaderCarrier, httpReads: HttpReads[MessageModel]): Future[MessageModel] =
    departureMovementP5Connector
      .getMessageForMessageId(departureId, messageId)

  def getDepartureReferenceNumbers(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DepartureReferenceNumbers] =
    departureMovementP5Connector.getDepartureReferenceNumbers(departureId)
}
