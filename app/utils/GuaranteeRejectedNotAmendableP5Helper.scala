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

import generated.GuaranteeReferenceType08
import models.departureP5.GuaranteeReferenceTable
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

// TODO - refactor
class GuaranteeRejectedNotAmendableP5Helper(guaranteeReferences: Seq[GuaranteeReferenceType08], referenceDataService: ReferenceDataService)(implicit
  messages: Messages,
  hc: HeaderCarrier,
  ec: ExecutionContext
) extends DeparturesP5MessageHelper {

  val tables: Future[Seq[GuaranteeReferenceTable]] =
    Future.sequence {
      guaranteeReferences.zipWithIndex.map {
        case (guaranteeReference, index) =>
          val title = messages("guarantee.rejected.message.notAmendable.guaranteeReference", index + 1)

          Future
            .sequence {
              guaranteeReference.InvalidGuaranteeReason.map {
                x =>
                  for {
                    y <- referenceDataService.getInvalidGuaranteeReason(x.code)
                  } yield Seq(
                    TableRow(y.toString.toText),
                    TableRow(x.text.getOrElse("").toText)
                  )
              }
            }
            .map {
              rows =>
                val table = Table(
                  rows,
                  Some(
                    Seq(
                      HeadCell(messages("guarantee.rejected.message.notAmendable.error").toText),
                      HeadCell(messages("guarantee.rejected.message.notAmendable.furtherInformation").toText)
                    )
                  )
                )

                GuaranteeReferenceTable(title, guaranteeReference.GRN, table)
            }
      }
    }

}
