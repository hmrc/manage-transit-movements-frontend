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
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.sections.Section
import viewModels.sections.Section.StaticSection

import javax.xml.datatype.XMLGregorianCalendar
import scala.concurrent.{ExecutionContext, Future}

class IntentionToControlP5MessageHelper(ie060: CC060CType, referenceDataService: ReferenceDataService)(implicit
  messages: Messages,
  hc: HeaderCarrier,
  ec: ExecutionContext
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
    formatAnswer = formatAsDateAndTime,
    prefix = messages("row.label.dateAndTimeOfControlNotification"),
    id = None,
    call = None
  )

  def getCustomsOfficeForDisplay(referenceNumber: String): Future[String] = referenceDataService
    .getCustomsOffice(referenceNumber)
    .map(_.toString)

  def buildOfficeOfDepartureRow: Future[Option[SummaryListRow]] = getCustomsOfficeForDisplay(ie060.CustomsOfficeOfDeparture.referenceNumber).map {
    nameAndCode =>
      buildRowFromAnswer[String](
        answer = Some(nameAndCode),
        formatAnswer = formatAsText,
        prefix = messages("row.label.controlInformation.officeOfDeparture"),
        id = None,
        call = None
      )
  }

  def buildIntentionToControlSection(): Future[Section] =
    buildOfficeOfDepartureRow.map {
      officeOfDeparture =>
        val lrnRow               = extractOptionalRow(buildLRNRow)
        val mrnRow               = extractOptionalRow(buildMRNRow)
        val dateTimeControlRow   = extractOptionalRow(buildDateTimeControlRow)
        val officeOfDepartureRow = extractOptionalRow(officeOfDeparture)
        val rows                 = lrnRow ++ mrnRow ++ dateTimeControlRow ++ officeOfDepartureRow
        StaticSection(None, rows)
    }
}
