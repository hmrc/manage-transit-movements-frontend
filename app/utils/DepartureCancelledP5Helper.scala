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

import models.departureP5.IE009MessageData
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.sections.Section

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class DepartureCancelledP5Helper(ie009MessageData: IE009MessageData, referenceDataService: ReferenceDataService)(implicit
  messages: Messages,
  ec: ExecutionContext,
  hc: HeaderCarrier
) extends DeparturesP5MessageHelper {

  def buildMRNRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = ie009MessageData.transitOperation.MRN,
    formatAnswer = formatAsText,
    prefix = messages("row.label.movementReferenceNumber"),
    id = None,
    call = None
  )

  def buildDateTimeDecisionRow: Option[SummaryListRow] = buildRowFromAnswer[LocalDateTime](
    answer = ie009MessageData.invalidation.decisionDateAndTime,
    formatAnswer = formatAsDecisionDateTime,
    prefix = messages("row.label.dateAndTimeOfDecision"),
    id = None,
    call = None
  )

  def buildInitiatedByCustomsRow: Option[SummaryListRow] = buildRowFromAnswer[Boolean](
    answer = Some(ie009MessageData.invalidation.initiatedByCustoms match {
      case "1" => true
      case _   => false
    }),
    formatAnswer = formatAsYesOrNo,
    prefix = messages("row.label.initiatedByCustoms"),
    id = None,
    call = None
  )

  def buildOfficeOfDepartureRow: Future[Option[SummaryListRow]] = {
    val referenceNumber = ie009MessageData.customsOfficeOfDeparture.referenceNumber
    referenceDataService.getCustomsOffice(referenceNumber).map {
      customsOffice =>
        val answerToDisplay = customsOffice match {
          case Right(customsOffice) => customsOffice.nameAndCode
          case Left(id)             => id
        }
        buildRowFromAnswer[String](
          answer = Some(answerToDisplay),
          formatAnswer = formatAsText,
          prefix = messages("row.label.officeOfDeparture"),
          id = None,
          call = None
        )
    }
  }

  def buildCommentsRow: Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = ie009MessageData.invalidation.justification,
    formatAnswer = formatAsText,
    prefix = messages("row.label.justification"),
    id = None,
    call = None
  )

  def buildInvalidationSection: Future[Section] =
    buildOfficeOfDepartureRow.map {
      officeOfDeparture =>
        val mrnRow                = extractOptionalRow(buildMRNRow)
        val dateTimeRow           = extractOptionalRow(buildDateTimeDecisionRow)
        val initiatedByCustomsRow = extractOptionalRow(buildInitiatedByCustomsRow)
        val officeOfDepartureRow  = extractOptionalRow(officeOfDeparture)
        val commentsRow           = extractOptionalRow(buildCommentsRow)

        val rows = mrnRow ++ dateTimeRow ++ initiatedByCustomsRow ++ officeOfDepartureRow ++ commentsRow

        Section(None, rows, None)
    }

}
