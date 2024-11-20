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

import cats.implicits.*
import connectors.{DepartureCacheConnector, DepartureMovementP5Connector}
import generated.{CC015CType, CC056CType, CC182CType, Generated_CC015CTypeFormat, Generated_CC056CTypeFormat, Generated_CC182CTypeFormat}
import models.departureP5.*
import models.departureP5.BusinessRejectionType.*
import models.departureP5.DepartureMessageType.{
  DeclarationAmendmentAccepted,
  DeclarationSent,
  GoodsUnderControl,
  IncidentDuringTransit,
  RejectedByOfficeOfDeparture
}
import models.departureP5.Rejection.IE056Rejection
import models.{RichCC015Type, RichCC182Type}
import scalaxb.XMLFormat
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureP5MessageService @Inject() (
  departureMovementP5Connector: DepartureMovementP5Connector,
  cacheConnector: DepartureCacheConnector
) {

  private def handleDepartureMovementAndMessage(
    movement: DepartureMovement,
    message: LatestDepartureMessage
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MovementAndMessage] = {
    val departureId = movement.departureId
    getMessage[CC015CType](departureId, message.ie015MessageId).map {
      ie015 =>
        DepartureMovementAndMessage(
          departureId,
          movement.localReferenceNumber,
          movement.updated,
          message,
          ie015.isPreLodged
        )
    }
  }

  private def handleRejectedMovementAndMessage(
    movement: DepartureMovement,
    message: LatestDepartureMessage
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MovementAndMessage] = {
    val departureId = movement.departureId
    getMessage[CC056CType](departureId, message.latestMessage.messageId).flatMap {
      ie056 =>
        val xPaths = ie056.FunctionalError.map(_.errorPointer)
        BusinessRejectionType(ie056) match {
          case PresentationNotificationRejection =>
            Future.successful(
              PrelodgeRejectedMovementAndMessage(
                departureId,
                movement.localReferenceNumber,
                movement.updated,
                message,
                xPaths
              )
            )
          case InvalidationRejection =>
            Future.successful(
              RejectedMovementAndMessage(
                departureId,
                movement.localReferenceNumber,
                movement.updated,
                message,
                InvalidationRejection,
                false,
                xPaths
              )
            )
          case rejectionType =>
            val rejection = IE056Rejection(departureId, ie056)
            cacheConnector.isRejectionAmendable(movement.localReferenceNumber, rejection).map {
              isRejectionAmendable =>
                RejectedMovementAndMessage(
                  departureId,
                  movement.localReferenceNumber,
                  movement.updated,
                  message,
                  rejectionType,
                  isRejectionAmendable,
                  xPaths
                )
            }
        }
    }
  }

  private def handleIncidentMovementAndMessage(
    movement: DepartureMovement,
    message: LatestDepartureMessage
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MovementAndMessage] = {
    val departureId = movement.departureId
    getMessage[CC182CType](departureId, message.latestMessage.messageId).map {
      ie182 =>
        IncidentMovementAndMessage(
          departureId,
          movement.localReferenceNumber,
          movement.updated,
          message,
          ie182.hasMultipleIncidents
        )
    }
  }

  private def handleOtherMovementAndMessage(
    movement: DepartureMovement,
    message: LatestDepartureMessage
  ): Future[MovementAndMessage] =
    Future.successful(
      OtherMovementAndMessage(
        movement.departureId,
        movement.localReferenceNumber,
        movement.updated,
        message
      )
    )

  def getLatestMessagesForMovements(
    departureMovements: DepartureMovements
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[MovementAndMessage]] =
    departureMovements.departureMovements.traverse {
      movement =>
        departureMovementP5Connector.getLatestMessageForMovement(movement.departureId).flatMap {
          message =>
            message.latestMessage.messageType match {
              case RejectedByOfficeOfDeparture =>
                handleRejectedMovementAndMessage(movement, message)
              case DeclarationAmendmentAccepted | GoodsUnderControl | DeclarationSent =>
                handleDepartureMovementAndMessage(movement, message)
              case IncidentDuringTransit =>
                handleIncidentMovementAndMessage(movement, message)
              case _ =>
                handleOtherMovementAndMessage(movement, message)
            }
        }
    }

  def getMessage[T](
    departureId: String,
    messageId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext, format: XMLFormat[T]): Future[T] =
    departureMovementP5Connector.getMessage(departureId, messageId)

  def getDepartureReferenceNumbers(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DepartureReferenceNumbers] =
    departureMovementP5Connector.getDepartureReferenceNumbers(departureId)

}
