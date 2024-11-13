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

package models.departureP5

import java.time.LocalDateTime

sealed trait MovementAndMessage {
  val departureId: String
  val localReferenceNumber: String
  val message: LatestDepartureMessage
  val updated: LocalDateTime
}

case class RejectedMovementAndMessage(
  departureId: String,
  localReferenceNumber: String,
  updated: LocalDateTime,
  message: LatestDepartureMessage,
  rejectionType: BusinessRejectionType,
  isRejectionAmendable: Boolean,
  xPaths: Seq[String]
) extends MovementAndMessage

case class PrelodgeRejectedMovementAndMessage(
  departureId: String,
  localReferenceNumber: String,
  updated: LocalDateTime,
  message: LatestDepartureMessage,
  xPaths: Seq[String]
) extends MovementAndMessage

case class OtherMovementAndMessage(
  departureId: String,
  localReferenceNumber: String,
  updated: LocalDateTime,
  message: LatestDepartureMessage
) extends MovementAndMessage

case class DepartureMovementAndMessage(
  departureId: String,
  localReferenceNumber: String,
  updated: LocalDateTime,
  message: LatestDepartureMessage,
  isPrelodged: Boolean
) extends MovementAndMessage

case class IncidentMovementAndMessage(
  departureId: String,
  localReferenceNumber: String,
  updated: LocalDateTime,
  message: LatestDepartureMessage,
  hasMultipleIncidents: Boolean
) extends MovementAndMessage
