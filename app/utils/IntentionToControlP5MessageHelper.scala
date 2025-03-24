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

import generated.CC060CType
import models.referenceData.CustomsOffice
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.sections.Section
import viewModels.sections.Section.StaticSection

import javax.xml.datatype.XMLGregorianCalendar

class IntentionToControlP5MessageHelper(ie060: CC060CType, customsOffice: CustomsOffice)(implicit
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

  def buildOfficeOfDepartureRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(customsOffice.toString),
    formatAnswer = formatAsText,
    prefix = messages("row.label.controlInformation.officeOfDeparture"),
    id = None,
    call = None
  )

  def buildDateTimeControlRow: Option[SummaryListRow] = buildRowFromAnswer[XMLGregorianCalendar](
    answer = Some(ie060.TransitOperation.controlNotificationDateAndTime),
    formatAnswer = formatAsDateAndTime,
    prefix = messages("row.label.dateAndTimeOfControlNotification"),
    id = None,
    call = None
  )

  def buildIntentionToControlSection(): Section =
    val lrnRow               = extractOptionalRow(buildLRNRow)
    val mrnRow               = extractOptionalRow(buildMRNRow)
    val dateTimeControlRow   = extractOptionalRow(buildDateTimeControlRow)
    val officeOfDepartureRow = extractOptionalRow(buildOfficeOfDepartureRow)
    val rows                 = lrnRow ++ mrnRow ++ dateTimeControlRow ++ officeOfDepartureRow
    StaticSection(None, rows)
}
