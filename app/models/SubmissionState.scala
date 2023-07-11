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

import models.arrival.ArrivalStatus
import models.arrival.ArrivalStatus.{values, InvalidStatus}
import play.api.libs.json.{__, JsError, JsString, JsSuccess, Reads, Writes}

sealed trait SubmissionState {
  def toString: String
  def isSubmitted: Boolean
  def isAmendable: Boolean
}

sealed trait AmendableState {}

object SubmissionState {

  case object NotSubmitted extends SubmissionState {
    override def toString: String = "notSubmitted"
    def isSubmitted: Boolean      = false
    def isAmendable: Boolean      = false
  }

  case object Submitted extends SubmissionState with AmendableState {
    override def toString: String = "submitted"
    def isSubmitted: Boolean      = true

    def isAmendable: Boolean = true
  }

  case object RejectedPendingChanges extends SubmissionState with AmendableState {
    override def toString: String = "rejectedPendingChanges"
    def isSubmitted: Boolean      = false
    def isAmendable: Boolean      = true
  }

  case object RejectedAndResubmitted extends SubmissionState {
    override def toString: String = "rejectedAndResubmitted"
    def isSubmitted: Boolean      = false
    def isAmendable: Boolean      = false
  }

  val values: Seq[SubmissionState] =
    Seq(
      NotSubmitted,
      Submitted,
      RejectedPendingChanges,
      RejectedAndResubmitted
    )

  implicit val enumerable: Enumerable[SubmissionState] =
    Enumerable(
      values.map(
        v => v.toString -> v
      ): _*
    )

  implicit def reads(implicit ev: Enumerable[SubmissionState]): Reads[SubmissionState] =
    Reads {
      case JsString(str) =>
        ev.withName(str)
          .map(JsSuccess(_))
          .getOrElse(
            JsSuccess(NotSubmitted)
          )
      case _ =>
        JsError("error.invalid")
    }

  implicit def writes: Writes[SubmissionState] = Writes {
    state => JsString(state.toString)
  }

}
