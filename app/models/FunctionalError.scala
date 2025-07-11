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

import play.api.libs.json.*
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

sealed trait FunctionalError {
  val error: String
  val businessRuleId: String
  val invalidDataItem: InvalidDataItem
  val invalidAnswer: Option[String]

  def toTableRow: Seq[TableRow]
}

object FunctionalError {

  case class FunctionalErrorWithSection(
    error: String,
    businessRuleId: String,
    section: Option[String],
    invalidDataItem: InvalidDataItem,
    invalidAnswer: Option[String]
  ) extends FunctionalError {

    override def toTableRow: Seq[TableRow] = Seq(
      TableRow(Text(error)),
      TableRow(Text(businessRuleId)),
      TableRow(Text(section.getOrElse("N/A"))),
      TableRow(Text(invalidDataItem.value)),
      TableRow(Text(invalidAnswer.getOrElse("N/A")))
    )
  }

  object FunctionalErrorWithSection {

    implicit val reads: Reads[FunctionalErrorWithSection] = Json.reads[FunctionalErrorWithSection]
  }

  case class FunctionalErrorWithoutSection(
    error: String,
    businessRuleId: String,
    invalidDataItem: InvalidDataItem,
    invalidAnswer: Option[String]
  ) extends FunctionalError {

    override def toTableRow: Seq[TableRow] = Seq(
      TableRow(Text(error)),
      TableRow(Text(businessRuleId)),
      TableRow(Text(invalidDataItem.value)),
      TableRow(Text(invalidAnswer.getOrElse("N/A")))
    )
  }

  object FunctionalErrorWithoutSection {

    def apply(error: FunctionalErrorType): FunctionalErrorWithoutSection =
      new FunctionalErrorWithoutSection(
        error = error.errorCode,
        businessRuleId = error.errorReason,
        invalidDataItem = InvalidDataItem(error.errorPointer),
        invalidAnswer = error.originalAttributeValue
      )
  }
}
