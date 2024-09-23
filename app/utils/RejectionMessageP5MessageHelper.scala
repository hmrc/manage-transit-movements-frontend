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

import generated.FunctionalErrorType04
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class RejectionMessageP5MessageHelper(functionalErrors: Seq[FunctionalErrorType04], referenceDataService: ReferenceDataService)(implicit
  messages: Messages,
  hc: HeaderCarrier,
  ec: ExecutionContext
) extends DeparturesP5MessageHelper {

  def tableRows(): Future[Seq[Seq[TableRow]]] =
    Future.sequence(functionalErrors.map(buildTableRows))

  private def buildTableRows(error: FunctionalErrorType04): Future[Seq[TableRow]] =
    referenceDataService.getFunctionalError(error.errorCode.toString).map {
      functionalError =>
        Seq(
          TableRow(Text(functionalError.toString)),
          TableRow(Text(error.errorReason)),
          TableRow(Text(error.errorPointer), classes = "text-wrap"),
          TableRow(Text(error.originalAttributeValue.getOrElse("N/A")), classes = "text-wrap")
        )
    }

}
