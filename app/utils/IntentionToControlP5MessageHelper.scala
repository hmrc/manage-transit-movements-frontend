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

import generated.{CC060CType, RequestedDocumentType}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.sections.Section

import javax.xml.datatype.XMLGregorianCalendar

class IntentionToControlP5MessageHelper(ie060: CC060CType)(implicit
  messages: Messages
) extends DeparturesP5MessageHelper {

  def buildLRNRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = ie060.TransitOperation.LRN,
    formatAnswer = formatAsText,
    prefix = messages("row.label.localReferenceNumber"),
    id = None,
    call = None
  )

  def buildMRNRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = ie060.TransitOperation.MRN,
    formatAnswer = formatAsText,
    prefix = messages("row.label.movementReferenceNumber"),
    id = None,
    call = None
  )

  def buildDateTimeControlRow: Option[SummaryListRow] = buildRowFromAnswer[XMLGregorianCalendar](
    answer = Some(ie060.TransitOperation.controlNotificationDateAndTime),
    formatAnswer = formatAsDate,
    prefix = messages("row.label.dateAndTimeOfControlNotification"),
    id = None,
    call = None
  )

  private def buildDocumentTypeRow(documentSequence: String): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(documentSequence),
    formatAnswer = formatAsText,
    prefix = messages("row.label.type"),
    id = None,
    call = None
  )

  private def buildReferenceNumberRow(referenceNumber: String): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(referenceNumber),
    formatAnswer = formatAsText,
    prefix = messages("row.label.controlInformation.referenceNumber"),
    id = None,
    call = None
  )

  private def buildDocumentSection(document: RequestedDocumentType): Section = {

    val controlType: Seq[SummaryListRow]     = extractOptionalRow(buildDocumentTypeRow(document.documentType))
    val referenceNumber: Seq[SummaryListRow] = extractOptionalRow(buildReferenceNumberRow(ie060.CustomsOfficeOfDeparture.referenceNumber))
    val rows                                 = controlType ++ referenceNumber
    Section(messages("heading.label.controlInformation", document.sequenceNumber), rows, None)
  }

  def documentSection(): Seq[Section] = ie060.RequestedDocument.map(buildDocumentSection)

  def buildIntentionToControlSection(): Section = {
    val lrnRow             = extractOptionalRow(buildLRNRow)
    val mrnRow             = extractOptionalRow(buildMRNRow)
    val dateTimeControlRow = extractOptionalRow(buildDateTimeControlRow)
    val rows               = lrnRow ++ mrnRow ++ dateTimeControlRow
    Section(None, rows, None)
  }
}
