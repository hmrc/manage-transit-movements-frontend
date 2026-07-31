/*
 * Copyright 2024 HM Revenue & Customs
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

import models.FunctionalError.*
import play.api.i18n.Messages
import play.api.libs.json.*
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.html.components.Table
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.HeadCell

sealed trait FunctionalErrors[T <: FunctionalError] {

  val value: Seq[T]

  def update(f: Seq[T] => Seq[T]): FunctionalErrors[T]

  def paginate(page: Int, numberOfErrorsPerPage: Int): FunctionalErrors[T] = {
    val start = (page - 1) * numberOfErrorsPerPage
    update(_.slice(start, start + numberOfErrorsPerPage))
  }

  def head(implicit messages: Messages): Seq[HeadCell]

  def toTable(implicit messages: Messages): Table =
    Table(
      rows = value.map(_.toTableRow),
      head = Some(head)
    )

  def multipleErrors: Boolean = value.length > 1
}

object FunctionalErrors {

  case class FunctionalErrorsWithSection(value: Seq[FunctionalErrorWithSection]) extends FunctionalErrors[FunctionalErrorWithSection] {

    override def update(f: Seq[FunctionalErrorWithSection] => Seq[FunctionalErrorWithSection]): FunctionalErrors[FunctionalErrorWithSection] =
      this.copy(value = f(value))

    override def head(implicit messages: Messages): Seq[HeadCell] = Seq(
      HeadCell(Text(messages("error.table.errorCode"))),
      HeadCell(Text(messages("error.table.errorReason"))),
      HeadCell(Text(messages("error.table.section"))),
      HeadCell(Text(messages("error.table.pointer"))),
      HeadCell(Text(messages("error.table.attributeValue")))
    )
  }

  object FunctionalErrorsWithSection {

    implicit val reads: Reads[FunctionalErrorsWithSection] =
      __.read[Seq[FunctionalErrorWithSection]].map(FunctionalErrorsWithSection.apply)
  }

  case class FunctionalErrorsWithoutSection(value: Seq[FunctionalErrorWithoutSection]) extends FunctionalErrors[FunctionalErrorWithoutSection] {

    override def update(f: Seq[FunctionalErrorWithoutSection] => Seq[FunctionalErrorWithoutSection]): FunctionalErrors[FunctionalErrorWithoutSection] =
      this.copy(value = f(value))

    override def head(implicit messages: Messages): Seq[HeadCell] = Seq(
      HeadCell(Text(messages("error.table.errorCode"))),
      HeadCell(Text(messages("error.table.errorReason"))),
      HeadCell(Text(messages("error.table.pointer"))),
      HeadCell(Text(messages("error.table.attributeValue")))
    )
  }
}
