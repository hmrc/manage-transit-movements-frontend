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

package viewModels.drafts

import models.DeparturesSummary

import java.time.temporal.ChronoUnit.DAYS
import play.api.i18n.Messages
import viewModels.drafts.AllDraftDeparturesViewModel.{getRemainingDays, DraftDepartureRow}

import java.time.LocalDate

case class AllDraftDeparturesViewModel(daysTilDeletion: Int, items: DeparturesSummary) {

  val messageKeyPrefix      = "departure.drafts.dashboard"
  val tableMessageKeyPrefix = "departure.drafts.dashboard.table"

  val draftDepartures: Int = items.userAnswers.length

  def title(implicit messages: Messages): String                = messages(s"$messageKeyPrefix.title")
  def heading(implicit messages: Messages): String              = messages(s"$messageKeyPrefix.heading")
  def visuallyHiddenHeader(implicit messages: Messages): String = messages(s"$messageKeyPrefix.heading.hidden")

  def referenceNumber(implicit messages: Messages): String = messages(s"$tableMessageKeyPrefix.lrn")
  def daysToComplete(implicit messages: Messages): String  = messages(s"$tableMessageKeyPrefix.daysToComplete")

  def dataRows: Seq[DraftDepartureRow] = items.userAnswers.map {
    dd => DraftDepartureRow(dd.lrn.toString, getRemainingDays(dd.createdAt.toLocalDate, LocalDate.now(), daysTilDeletion).toInt)
  }

}

object AllDraftDeparturesViewModel {

  def getRemainingDays(createdAt: LocalDate, today: LocalDate, daysTilDeletion: Int): Long =
    DAYS.between(today, createdAt.plusDays(daysTilDeletion))

  case class DraftDepartureRow(lrn: String, daysRemaining: Int)
}
