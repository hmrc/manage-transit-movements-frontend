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
import models.departureP5.{FunctionalError, IE056MessageData}
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.sections.Section

import scala.concurrent.{ExecutionContext, Future}

class RejectionMessageP5MessageHelper(ie056MessageData: IE056MessageData, referenceDataService: ReferenceDataService)(implicit
  messages: Messages,
  hc: HeaderCarrier,
  ec: ExecutionContext
) extends DeparturesP5MessageHelper {

  private def getFunctionalErrorType(errorCode: String): Future[String] =
    referenceDataService.getFunctionalErrorType(errorCode)(ec, hc).map(_.toString)

  def buildErrorCodeRow(errorCode: String): Future[Option[SummaryListRow]] =
    getFunctionalErrorType(errorCode).map {
      code =>
        println(s"\n\n\n\n\n******************* In buildErrorCodeRow $code")
        buildRowFromAnswer[String](
          answer = Some(code),
          formatAnswer = formatAsText,
          prefix = messages("row.label.error"),
          id = None,
          call = None
        )
    }

  def buildErrorReasonRow(reason: String): Option[SummaryListRow] = buildRowFromAnswer[String](
    answer = Some(reason),
    formatAnswer = formatAsText,
    prefix = messages("row.label.reason"),
    id = None,
    call = None
  )

  def buildErrorRows(errors: FunctionalError): Future[Seq[SummaryListRow]] =
    buildErrorCodeRow(errors.errorCode).map {
      code =>
        val errorCode: Seq[SummaryListRow]   = extractOptionalRow(code)
        val errorReason: Seq[SummaryListRow] = extractOptionalRow(buildErrorReasonRow(errors.errorReason))
        errorCode ++ errorReason
    }

  def errorSection(): Future[Section] = {

    val summaryListRows: Future[Seq[SummaryListRow]] = Future
      .sequence(
        ie056MessageData.functionalErrors.map(
          error => buildErrorRows(error)
        )
      )
      .map(_.flatten)

    summaryListRows.map(
      slr => Section(None, slr, None)
    )
  }
}
