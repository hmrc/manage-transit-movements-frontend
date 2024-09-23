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

import generated.{CC035CType, RecoveryNotificationType}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.sections.Section
import viewModels.sections.Section.StaticSection

import javax.xml.datatype.XMLGregorianCalendar

class RecoveryNotificationHelper(ie035: CC035CType)(implicit
  messages: Messages
) extends DeparturesP5MessageHelper {

  def buildMRNRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(ie035.TransitOperation.MRN),
    formatAnswer = formatAsText,
    prefix = messages("row.label.movementReferenceNumber"),
    id = None,
    call = None
  )

  def buildDeclarationAcceptanceDateRow: Option[SummaryListRow] = buildRowFromAnswer[XMLGregorianCalendar](
    answer = Some(ie035.TransitOperation.declarationAcceptanceDate),
    formatAnswer = formatAsDate,
    prefix = messages("row.label.declarationAcceptanceDate"),
    id = None,
    call = None
  )

  def buildRecoveryDateRow: Option[SummaryListRow] = buildRowFromAnswer[XMLGregorianCalendar](
    answer = ie035.RecoveryNotification.recoveryNotificationDate,
    formatAnswer = formatAsDate,
    prefix = messages("row.label.recoveryNotificationDate"),
    id = None,
    call = None
  )

  def buildFurtherInformationRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = ie035.RecoveryNotification.recoveryNotificationText,
    formatAnswer = formatAsText,
    prefix = messages("row.label.recoveryNotificationText"),
    id = None,
    call = None
  )

  def buildAmountRow: Option[SummaryListRow] = buildRowFromAnswer[RecoveryNotificationType](
    answer = Some(ie035.RecoveryNotification),
    formatAnswer = formatAsCurrency,
    prefix = messages("row.label.amountClaimed"),
    id = None,
    call = None
  )

  def buildRecoveryNotificationSection: Section = {

    val mrnRow                       = extractOptionalRow(buildMRNRow)
    val declarationAcceptanceDateRow = extractOptionalRow(buildDeclarationAcceptanceDateRow)
    val recoveryDateRow              = extractOptionalRow(buildRecoveryDateRow)
    val furtherInformationRow        = extractOptionalRow(buildFurtherInformationRow)
    val amountRow                    = extractOptionalRow(buildAmountRow)

    val rows = mrnRow ++ declarationAcceptanceDateRow ++ recoveryDateRow ++ furtherInformationRow ++ amountRow

    StaticSection(None, rows)
  }

}
