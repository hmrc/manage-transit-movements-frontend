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

package models

sealed trait DraftAvailability

object DraftAvailability {

  def apply(departureSummaries: Option[DeparturesSummary]): DraftAvailability = departureSummaries match {
    case Some(value) if value.userAnswers.nonEmpty => NonEmpty
    case Some(value) if value.userAnswers.isEmpty  => Empty
    case None                                      => Unavailable
  }

  sealed trait Available extends DraftAvailability

  case object Unavailable extends DraftAvailability
  case object Empty extends DraftAvailability
  case object NonEmpty extends DraftAvailability
}
