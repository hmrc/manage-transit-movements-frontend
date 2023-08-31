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

package viewModels

import models.departureP5.FunctionalError
import models.referenceData.FunctionalErrorWithDesc
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import viewModels.ErrorViewModel.ErrorRow

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class ErrorViewModel(
  errors: Seq[ErrorRow]
) {

  val messageKeyPrefix                                        = "error.table"
  def errorCodeHeading(implicit messages: Messages): String   = messages(s"$messageKeyPrefix.errorCode")
  def errorReasonHeading(implicit messages: Messages): String = messages(s"$messageKeyPrefix.errorReason")
}

object ErrorViewModel {
  case class ErrorRow(errorCode: String, errorReason: String)

  class ErrorViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(
      functionalErrors: Seq[FunctionalError]
    )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[ErrorViewModel] = {
      val dataRows: Future[Seq[ErrorRow]] = Future.sequence(functionalErrors.map {
        error: FunctionalError =>
          val errorDescription: Future[FunctionalErrorWithDesc] = referenceDataService.getFunctionalError(error.errorCode)
          errorDescription.map(
            errorDescription => ErrorRow(errorDescription.toString, error.errorReason)
          )
      })
      dataRows.map {
        dataRows =>
          ErrorViewModel(dataRows)
      }
    }
  }
}
