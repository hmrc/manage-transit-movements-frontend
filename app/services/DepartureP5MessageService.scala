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
import connectors.DepartureMovementP5Connector
import models.departureP5.DepartureMessageType.GoodsUnderControl
import models.departureP5.{DepartureMovementAndMessage, DepartureMovements, IE060Data, MessageMetaData}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DepartureP5MessageService @Inject() (
  departureMovementP5Connector: DepartureMovementP5Connector
) {

  def getMessagesForAllMovements(
    departureMovements: DepartureMovements
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[DepartureMovementAndMessage]] =
    departureMovements.departureMovements.traverse {
      movement =>
        departureMovementP5Connector
          .getMessagesForMovement(movement.messagesLocation)
          .map(
            messagesForMovement => DepartureMovementAndMessage(movement, messagesForMovement)
          )
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

  def getGoodsUnderControl(departureId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[IE060Data]] =
    (
      for {
        goodsUnderControlMessage <- OptionT(getGoodsUnderControlMessage(departureId))
        goodsUnderControl        <- OptionT.liftF(departureMovementP5Connector.getGoodsUnderControl(goodsUnderControlMessage.path))
      } yield goodsUnderControl
    ).value

}
