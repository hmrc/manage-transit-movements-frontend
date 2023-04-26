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
import connectors.DepartureMovementP5Connector
import models.departureP5.DepartureMessageType._
import models.departureP5._
import uk.gov.hmrc.http.HeaderCarrier
import cats.data.OptionT

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureP5MessageService @Inject() (departureMovementP5Connector: DepartureMovementP5Connector) {

  def getMessagesForAllMovements(
    departureMovements: DepartureMovements
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[DepartureMovementAndMessage]] =
    departureMovements.departureMovements.traverse {
      movement =>
        departureMovementP5Connector
          .getMessagesForMovement(movement.messagesLocation)
          .flatMap {
            messagesForMovement =>
              val lrn: Future[String] = messagesForMovement.messages.find(_.messageType == DepartureNotification) match {
                case Some(departureMessage) =>
                  departureMovementP5Connector.getLRN(departureMessage.bodyPath).map(_.referenceNumber)
                case None =>
                  Future.successful("")
              }
              lrn.map(DepartureMovementAndMessage(movement, messagesForMovement, _))
          }
    }

  private def getGoodsUnderControlMessage(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[MessageMetaData]] =
    departureMovementP5Connector
      .getMessageMetaData(departureId: String)
      .map(
        _.messages
          .filter(_.messageType == GoodsUnderControl)
          .sortBy(_.received)
          .reverse
          .headOption
      )

  private def getRejectionMetaDataMessage(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[MessageMetaData]] =
    departureMovementP5Connector
      .getMessageMetaData(departureId: String)
      .map(
        _.messages
          .filter(_.messageType == RejectedByOfficeOfDeparture)
          .sortBy(_.received)
          .reverse
          .headOption
      )

  def getGoodsUnderControl(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[IE060Data]] =
    (
      for {
        goodsUnderControlMessage <- OptionT(getGoodsUnderControlMessage(departureId))
        goodsUnderControl        <- OptionT.liftF(departureMovementP5Connector.getGoodsUnderControl(goodsUnderControlMessage.path))
      } yield goodsUnderControl
    ).value

  def getRejectionMessage(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[IE056Data]] =
    (
      for {
        rejectionMessage <- OptionT(getRejectionMetaDataMessage(departureId))
        rejection        <- OptionT.liftF(departureMovementP5Connector.getRejectionMessage(rejectionMessage.path))
      } yield rejection
    ).value
}
