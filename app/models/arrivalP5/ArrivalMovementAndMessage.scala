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

package models.arrivalP5

import models.ArrivalRejectionType

sealed trait ArrivalMovementAndMessage {
  val arrivalMovement: ArrivalMovement
  val latestArrivalMessage: LatestArrivalMessage
}

case class RejectedMovementAndMessage(
  arrivalMovement: ArrivalMovement,
  latestArrivalMessage: LatestArrivalMessage,
  functionalErrorCount: Int,
  rejectedType: ArrivalRejectionType
) extends ArrivalMovementAndMessage

case class OtherMovementAndMessage(
  arrivalMovement: ArrivalMovement,
  latestArrivalMessage: LatestArrivalMessage
) extends ArrivalMovementAndMessage

case class GoodsReleasedMovementAndMessage(
  arrivalMovement: ArrivalMovement,
  latestArrivalMessage: LatestArrivalMessage,
  goodsReleasedStatus: String
) extends ArrivalMovementAndMessage
