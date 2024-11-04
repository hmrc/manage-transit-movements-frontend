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
import generated.{CC015CType, CC056CType, CC182CType}
import models.{RichCC015Type, RichCC182Type}
import models.departureP5.DepartureMessageType.{
  DeclarationAmendmentAccepted,
  DeclarationSent,
  GoodsUnderControl,
  IncidentDuringTransit,
  RejectedByOfficeOfDeparture
}
import models.departureP5.*
import scalaxb.XMLFormat
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import generated.Generated_CC056CTypeFormat
import generated.Generated_CC015CTypeFormat
import generated.Generated_CC182CTypeFormat
import models.departureP5.BusinessRejectionType.PresentationNotificationRejection

class DepartureP5MessageService @Inject() (
  departureMovementP5Connector: DepartureMovementP5Connector,
  cacheConnector: DepartureCacheConnector
) {

  private def getRejectionMessage(
    departureId: String,
    messageId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[(String, Seq[String])] =
    for {
      message <- getMessage[CC056CType](departureId, messageId)
      rejectionType = message.TransitOperation.businessRejectionType
      xPaths        = message.FunctionalError.map(_.errorPointer)
    } yield (rejectionType, xPaths)

  private def isErrorAmendable(
    rejectionType: String,
    xPaths: Seq[String],
    lrn: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[(String, Boolean, Seq[String])] =
    for {
      isDeclarationAmendable <- cacheConnector.isDeclarationAmendable(lrn, xPaths.filter(_.nonEmpty))
    } yield (rejectionType, isDeclarationAmendable, xPaths)

  def getLatestMessagesForMovements(
    departureMovements: DepartureMovements
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[MovementAndMessage]] =
    departureMovements.departureMovements.traverse {
      movement =>
        departureMovementP5Connector.getLatestMessageForMovement(movement.departureId).flatMap {
          message =>
            message.latestMessage.messageType match {
              case RejectedByOfficeOfDeparture =>
                getRejectionMessage(movement.departureId, message.latestMessage.messageId).flatMap {
                  case (PresentationNotificationRejection.value, xPaths) =>
                    Future.successful(
                      PrelodgeRejectedMovementAndMessage(
                        movement.departureId,
                        movement.localReferenceNumber,
                        movement.updated,
                        message,
                        xPaths
                      )
                    )
                  case (rejectionType, xPaths) =>
                    isErrorAmendable(rejectionType, xPaths, movement.localReferenceNumber).map {
                      case (rejectionType, isDeclarationAmendable, xPaths) =>
                        RejectedMovementAndMessage(
                          movement.departureId,
                          movement.localReferenceNumber,
                          movement.updated,
                          message,
                          BusinessRejectionType(rejectionType),
                          isDeclarationAmendable,
                          xPaths
                        )
                    }
                }
              case DeclarationAmendmentAccepted | GoodsUnderControl | DeclarationSent =>
                getMessage[CC015CType](movement.departureId, message.ie015MessageId).map {
                  ie015 =>
                    DepartureMovementAndMessage(
                      movement.departureId,
                      movement.localReferenceNumber,
                      movement.updated,
                      message,
                      ie015.isPreLodged
                    )
                }

              case IncidentDuringTransit =>
                getMessage[CC182CType](movement.departureId, message.latestMessage.messageId).map {
                  ie182 =>
                    IncidentMovementAndMessage(
                      movement.departureId,
                      movement.localReferenceNumber,
                      movement.updated,
                      message,
                      ie182.hasMultipleIncidents
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

  def getMessage[T](
    departureId: String,
    messageId: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext, format: XMLFormat[T]): Future[T] =
    departureMovementP5Connector.getMessage(departureId, messageId)

  def getDepartureReferenceNumbers(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[DepartureReferenceNumbers] =
    departureMovementP5Connector.getDepartureReferenceNumbers(departureId)

}
