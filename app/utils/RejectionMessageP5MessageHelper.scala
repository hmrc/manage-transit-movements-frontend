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
import models.departureP5.{FunctionalError, IE056MessageData, RequestedDocument, TypeOfControls}
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.sections.Section

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

class RejectionMessageP5MessageHelper(ie056MessageData: IE056MessageData, referenceDataService: ReferenceDataService)(implicit
  messages: Messages,
  hc: HeaderCarrier,
  ec: ExecutionContext
) extends DeparturesP5MessageHelper {

//  private def getControlTypeDescription(typeOfControl: String): Future[Option[String]] =
//    (for {
//      y <- OptionT.liftF(referenceDataService.getControlType(typeOfControl)(ec, hc))
//      x = y.toString
//    } yield x).value

  private def buildErrorCodeRow(errorCode: String): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(errorCode),
    formatAnswer = formatAsText,
    prefix = messages("row.label.type"),
    id = None,
    call = None
  )

  private def buildErrorReasonRow(reason: String): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(reason),
    formatAnswer = formatAsText,
    prefix = messages("row.label.description"),
    id = None,
    call = None
  )

  private def buildErrorSection(errors: FunctionalError): Section = {

    val errorCode: Seq[SummaryListRow]   = extractOptionalRow(buildErrorCodeRow(errors.errorCode))
    val errorReason: Seq[SummaryListRow] = extractOptionalRow(buildErrorReasonRow(errors.errorReason))
    val rows                             = errorCode ++ errorReason
    Section(messages("heading.label.documentInformation"), rows, None)
  }

  def errorSection(): Seq[Section] = ie056MessageData.functionalErrorToSeq.map(
    error => buildErrorSection(error)
  )
}
