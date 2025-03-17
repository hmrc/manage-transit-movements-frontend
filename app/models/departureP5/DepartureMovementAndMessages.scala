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

sealed trait MovementAndMessages {
  val departureId: String
  val localReferenceNumber: String
  val messages: DepartureMovementMessages
  val updated: LocalDateTime
}

case class RejectedMovementAndMessages(
  departureId: String,
  localReferenceNumber: String,
  updated: LocalDateTime,
  messages: DepartureMovementMessages,
  rejectionType: BusinessRejectionType,
  isRejectionAmendable: Boolean,
  xPaths: Seq[String]
) extends MovementAndMessages

case class PrelodgeRejectedMovementAndMessages(
  departureId: String,
  localReferenceNumber: String,
  updated: LocalDateTime,
  messages: DepartureMovementMessages,
  xPaths: Seq[String]
) extends MovementAndMessages

case class OtherMovementAndMessages(
  departureId: String,
  localReferenceNumber: String,
  updated: LocalDateTime,
  messages: DepartureMovementMessages
) extends MovementAndMessages

case class DepartureMovementAndMessages(
  departureId: String,
  localReferenceNumber: String,
  updated: LocalDateTime,
  messages: DepartureMovementMessages,
  isPrelodged: Boolean
) extends MovementAndMessages

case class IncidentMovementAndMessages(
  departureId: String,
  localReferenceNumber: String,
  updated: LocalDateTime,
  messages: DepartureMovementMessages,
  hasMultipleIncidents: Boolean
) extends MovementAndMessages
