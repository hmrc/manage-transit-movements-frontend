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

import models.departureP5.{IE060MessageData, RequestedDocument, TypeOfControls}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewModels.sections.Section

import java.time.LocalDateTime
import scala.annotation.unused

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
    prefix = messages("row.label.officeOfDeparture"),
    id = None,
    call = None
  )

  def buildControlTypeRow(typeOfControl: String): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(typeOfControl),
    formatAnswer = formatAsText,
    prefix = messages("row.label.type"),
    id = None,
    call = None
  )

  def buildControlDescriptionRow(description: Option[String]): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = description,
    formatAnswer = formatAsText,
    prefix = messages("row.label.description"),
    id = None,
    call = None
  )

  def buildControlSequenceRow(controlSequence: String): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(controlSequence),
    formatAnswer = formatAsText,
    prefix = messages("row.label.control"),
    id = None,
    call = None
  )

  def buildDocumentSequenceRow(documentSequence: String): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(documentSequence),
    formatAnswer = formatAsText,
    prefix = messages("row.label.document"),
    id = None,
    call = None
  )

  def buildDocumentTypeRow(documentSequence: String): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(documentSequence),
    formatAnswer = formatAsText,
    prefix = messages("row.label.type"),
    id = None,
    call = None
  )

  def buildDocumentDescriptionRow(description: Option[String]): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = description,
    formatAnswer = formatAsText,
    prefix = messages("row.label.description"),
    id = None,
    call = None
  )

  def buildTypeOfControlSection(typeOfControl: TypeOfControls): Section = {

    val sequenceNumber: Seq[SummaryListRow]     = extractOptionalRow(buildControlSequenceRow(typeOfControl.sequenceNumber))
    val controlType: Seq[SummaryListRow]        = extractOptionalRow(buildControlTypeRow(typeOfControl.`type`))
    val controlDescription: Seq[SummaryListRow] = extractOptionalRow(buildControlDescriptionRow(typeOfControl.text))
    val rows                                    = sequenceNumber ++ controlType ++ controlDescription
    Section(None, rows, None)

  }

  def buildDocumentSection(document: RequestedDocument): Section = {

    val sequenceNumber: Seq[SummaryListRow]      = extractOptionalRow(buildDocumentSequenceRow(document.sequenceNumber))
    val documentType: Seq[SummaryListRow]        = extractOptionalRow(buildDocumentTypeRow(document.documentType))
    val documentDescription: Seq[SummaryListRow] = extractOptionalRow(buildDocumentDescriptionRow(document.description))
    val rows                                     = sequenceNumber ++ documentType ++ documentDescription
    Section(None, rows, None)

  }

  def documentSection(): Seq[Section] = {

    val documentInformation: Seq[RequestedDocument] = ie060MessageData.requestedDocumentsToSeq
    documentInformation.map {
      document =>
        buildDocumentSection(document)
    }
  }

  def buildGoodsUnderControlSection(): Section = {

    val lrnRow               = extractOptionalRow(buildLRNRow)
    val mrnRow               = extractOptionalRow(buildMRNRow)
    val dateTimeControlRow   = extractOptionalRow(buildDateTimeControlRow)
    val officeOfDepartureRow = extractOptionalRow(buildOfficeOfDepartureRow)

    val rows = lrnRow ++ mrnRow ++ dateTimeControlRow ++ officeOfDepartureRow

    Section(None, rows, None)

  }

  def controlInformationSection(): Seq[Section] = {

    val controlInformation: Seq[TypeOfControls] = ie060MessageData.typeOfControlsToSeq
    controlInformation.map {
      typeOfControl =>
        buildTypeOfControlSection(typeOfControl)
    }
  }
}
