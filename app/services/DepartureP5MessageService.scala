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

import cats.implicits._
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
import models.departureP5._
import scalaxb.XMLFormat
import uk.gov.hmrc.http.HeaderCarrier

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
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[(String, Boolean, Seq[String], Boolean)] =
    for {
      message <- getMessage[CC056CType](departureId, messageId)
      rejectionType = message.TransitOperation.businessRejectionType
      xPaths        = message.FunctionalError.map(_.errorPointer)
      isDeclarationAmendable <- cacheConnector.isDeclarationAmendable(lrn, xPaths.filter(_.nonEmpty))
      doesCacheExistForLrn   <- cacheConnector.doesDeclarationExist(lrn)
    } yield (rejectionType, isDeclarationAmendable, xPaths, doesCacheExistForLrn)

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
                  case (rejectionType, isDeclarationAmendable, xPaths, doesCacheExistForLrn) =>
                    RejectedMovementAndMessage(
                      movement.departureId,
                      movement.localReferenceNumber,
                      movement.updated,
                      message,
                      rejectionType,
                      isDeclarationAmendable,
                      xPaths,
                      doesCacheExistForLrn
                    )
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
