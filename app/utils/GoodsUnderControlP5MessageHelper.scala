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

import cats.data.OptionT
import generated.{CC060CType, RequestedDocumentType, TypeOfControlsType}
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.sections.Section

import javax.xml.datatype.XMLGregorianCalendar
import scala.concurrent.{ExecutionContext, Future}

class GoodsUnderControlP5MessageHelper(ie060: CC060CType, referenceDataService: ReferenceDataService)(implicit
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
    prefix = messages("row.label.dateAndTimeOfControl"),
    id = None,
    call = None
  )

  def getCustomsOfficeForDisplay(referenceNumber: String): Future[String] = referenceDataService.getCustomsOffice(referenceNumber).map {
    case Right(customsOffice) => customsOffice.nameAndCode
    case Left(id)             => id
  }

  def buildOfficeOfDepartureRow: Future[Option[SummaryListRow]] = getCustomsOfficeForDisplay(ie060.CustomsOfficeOfDeparture.referenceNumber).map {
    customsOffice =>
      buildRowFromAnswer[String](
        answer = Some(customsOffice),
        formatAnswer = formatAsText,
        prefix = messages("row.label.officeOfDeparture"),
        id = None,
        call = None
      )
  }

  private def buildControlTypeRow(typeOfControl: String): Future[Option[SummaryListRow]] =
    getControlTypeDescription(typeOfControl).map(
      desc =>
        buildRowFromAnswer[String](
          answer = desc,
          formatAnswer = formatAsText,
          prefix = messages("row.label.type"),
          id = None,
          call = None
        )
    )

  private def getControlTypeDescription(typeOfControl: String): Future[Option[String]] =
    (for {
      y <- OptionT.liftF(referenceDataService.getControlType(typeOfControl)(ec, hc))
      x = y.toString
    } yield x).value

  private def buildControlDescriptionRow(description: Option[String]): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = description,
    formatAnswer = formatAsText,
    prefix = messages("row.label.description"),
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

  private def buildDocumentDescriptionRow(description: Option[String]): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = description,
    formatAnswer = formatAsText,
    prefix = messages("row.label.description"),
    id = None,
    call = None
  )

  private def buildTypeOfControlSection(typeOfControl: TypeOfControlsType): Future[Section] =
    buildControlTypeRow(typeOfControl.typeValue).map {
      x =>
        val controlType: Seq[SummaryListRow]        = extractOptionalRow(x)
        val controlDescription: Seq[SummaryListRow] = extractOptionalRow(buildControlDescriptionRow(typeOfControl.text))
        val rows                                    = controlType ++ controlDescription
        Section(messages("heading.label.controlInformation", typeOfControl.sequenceNumber), rows, None)
    }

  private def buildDocumentSection(document: RequestedDocumentType): Section = {

    val documentType: Seq[SummaryListRow]        = extractOptionalRow(buildDocumentTypeRow(document.documentType))
    val documentDescription: Seq[SummaryListRow] = extractOptionalRow(buildDocumentDescriptionRow(document.description))
    val rows                                     = documentType ++ documentDescription
    Section(messages("heading.label.documentInformation", document.sequenceNumber), rows, None)
  }

  def documentSection(): Seq[Section] = ie060.RequestedDocument.map(buildDocumentSection)

  def buildGoodsUnderControlSection(): Future[Section] =
    buildOfficeOfDepartureRow.map {
      officeOfDeparture =>
        val lrnRow               = extractOptionalRow(buildLRNRow)
        val mrnRow               = extractOptionalRow(buildMRNRow)
        val dateTimeControlRow   = extractOptionalRow(buildDateTimeControlRow)
        val officeOfDepartureRow = extractOptionalRow(officeOfDeparture)
        val rows                 = lrnRow ++ mrnRow ++ dateTimeControlRow ++ officeOfDepartureRow

        Section(None, rows, None)
    }

  def controlInformationSection(): Future[Seq[Section]] =
    Future.sequence(ie060.TypeOfControls.map(buildTypeOfControlSection))
}
