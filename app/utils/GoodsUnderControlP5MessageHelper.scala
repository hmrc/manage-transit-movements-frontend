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

import models.departureP5.IE060MessageData
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.sections.Section

import java.time.LocalDateTime

class GoodsUnderControlP5MessageHelper(ie060MessageData: IE060MessageData)(implicit messages: Messages) extends DeparturesP5MessageHelper {

  def buildLRNRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = ie060MessageData.TransitOperation.LRN,
    formatAnswer = formatAsText,
    prefix = messages("row.label.localReferenceNumber"),
    id = None,
    call = None
  )

  def buildMRNRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = ie060MessageData.TransitOperation.MRN,
    formatAnswer = formatAsText,
    prefix = messages("row.label.movementReferenceNumber"),
    id = None,
    call = None
  )

  def buildDateTimeControlRow: Option[SummaryListRow] = buildRowFromAnswer[LocalDateTime](
    answer = Some(ie060MessageData.TransitOperation.controlNotificationDateAndTime),
    formatAnswer = formatAsDate,
    prefix = messages("row.label.dateAndTimeOfControl"),
    id = None,
    call = None
  )

  def buildOfficeOfDepartureRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(ie060MessageData.CustomsOfficeOfDeparture.referenceNumber),
    formatAnswer = formatAsText,
    prefix = messages("heading.label.controlInformation"),
    id = None,
    call = None
  )

  def buildGoodsUnderControlSection(): Section = {

    val lrnRow               = buildLRNRow.map(Seq(_)).getOrElse(Seq.empty)
    val mrnRow               = buildMRNRow.map(Seq(_)).getOrElse(Seq.empty)
    val dateTimeControlRow   = buildDateTimeControlRow.map(Seq(_)).getOrElse(Seq.empty)
    val officeOfDepartureRow = buildOfficeOfDepartureRow.map(Seq(_)).getOrElse(Seq.empty)

    val rows = lrnRow ++ mrnRow ++ dateTimeControlRow ++ officeOfDepartureRow

    Section(None, rows, None)

  }

}
