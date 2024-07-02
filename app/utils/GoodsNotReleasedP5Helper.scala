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

package utils

import generated.CC051CType
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.sections.Section
import viewModels.sections.Section.StaticSection

import javax.xml.datatype.XMLGregorianCalendar

class GoodsNotReleasedP5Helper(
  ie051: CC051CType
)(implicit messages: Messages)
    extends DeparturesP5MessageHelper {

  def buildMRNRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(ie051.TransitOperation.MRN),
    formatAnswer = formatAsText,
    prefix = messages("row.label.movementReferenceNumber"),
    id = None,
    call = None
  )

  def buildDateTimeDeclarationRow: Option[SummaryListRow] = buildRowFromAnswer[XMLGregorianCalendar](
    answer = Some(ie051.TransitOperation.declarationSubmissionDateAndTime),
    formatAnswer = formatAsDateAndTime,
    prefix = messages("departure.notReleased.row.label.dateTimeDeclaration"),
    id = None,
    call = None
  )

  def buildReasonRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(ie051.TransitOperation.noReleaseMotivationCode),
    formatAnswer = formatAsText,
    prefix = messages("departure.notReleased.row.label.reason"),
    id = None,
    call = None
  )

  def buildDescriptionRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(ie051.TransitOperation.noReleaseMotivationText),
    formatAnswer = formatAsText,
    prefix = messages("departure.notReleased.row.label.description"),
    id = None,
    call = None
  )

  def buildDetailsSection: Section = {
    val mrnRow         = extractOptionalRow(buildMRNRow)
    val dateTimeRow    = extractOptionalRow(buildDateTimeDeclarationRow)
    val reasonRow      = extractOptionalRow(buildReasonRow)
    val descriptionRow = extractOptionalRow(buildDescriptionRow)

    val rows = mrnRow ++ dateTimeRow ++ reasonRow ++ descriptionRow

    StaticSection(None, rows)
  }

}
