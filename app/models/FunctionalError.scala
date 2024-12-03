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

import generated.FunctionalErrorType04
import play.api.i18n.Messages
import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.html.components.Table
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}

import scala.annotation.tailrec

case class FunctionalError(
  error: String,
  businessRuleId: String,
  section: Option[String],
  invalidDataItem: InvalidDataItem,
  invalidAnswer: Option[String]
) {

  def toTableRow: Seq[TableRow] = Seq(
    TableRow(Text(error)),
    TableRow(Text(businessRuleId)),
    TableRow(Text(invalidDataItem.value)),
    TableRow(Text(invalidAnswer.getOrElse("N/A")))
  )

  def toDepartureTableRow: Seq[TableRow] = Seq(
    TableRow(Text(error)),
    TableRow(Text(businessRuleId)),
    TableRow(Text(section.getOrElse("N/A"))),
    TableRow(Text(invalidDataItem.value)),
    TableRow(Text(invalidAnswer.getOrElse("N/A")))
  )
}

object FunctionalError {

  implicit val reads: Reads[FunctionalError] = Json.reads[FunctionalError]

  implicit val writes: Writes[FunctionalErrorType04] = (
    (__ \ "errorPointer").write[String] and
      (__ \ "errorCode").write[String] and
      (__ \ "errorReason").write[String] and
      (__ \ "originalAttributeValue").writeNullable[String]
  )(
    functionalError => (functionalError.errorPointer, functionalError.errorCode.toString, functionalError.errorReason, functionalError.originalAttributeValue)
  )

  implicit class RichFunctionalErrors(value: Seq[FunctionalError]) {

    def paginate(page: Int, numberOfErrorsPerPage: Int): Seq[FunctionalError] = {
      val start = (page - 1) * numberOfErrorsPerPage
      value.slice(start, start + numberOfErrorsPerPage)
    }

    def toTableOfErrors(implicit messages: Messages): Table = Table(
      rows = value.map(_.toTableRow),
      head = Some(
        Seq(
          HeadCell(Text(messages("error.table.errorCode"))),
          HeadCell(Text(messages("error.table.errorReason"))),
          HeadCell(Text(messages("error.table.pointer"))),
          HeadCell(Text(messages("error.table.attributeValue")))
        )
      )
    )

    def toTableOfDepartureErrors(implicit messages: Messages): Table = Table(
      rows = value.map(_.toDepartureTableRow),
      head = Some(
        Seq(
          HeadCell(Text(messages("error.table.errorCode"))),
          HeadCell(Text(messages("error.table.errorReason"))),
          HeadCell(Text(messages("error.table.section"))),
          HeadCell(Text(messages("error.table.pointer"))),
          HeadCell(Text(messages("error.table.attributeValue")))
        )
      )
    )
  }
}
