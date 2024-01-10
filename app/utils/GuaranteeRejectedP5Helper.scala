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
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, Table, TableRow}

class GuaranteeRejectedP5Helper(guaranteeReferences: Seq[GuaranteeReferenceType08])(implicit messages: Messages) extends DeparturesP5MessageHelper {

  def toTables: Seq[(String, String, Table)] =
    guaranteeReferences.zipWithIndex.map {
      case (guaranteeReference, index) =>
        val title = messages("guarantee.rejected.message.guaranteeReference", index + 1)

        val rows: Seq[Seq[TableRow]] = guaranteeReference.InvalidGuaranteeReason.map {
          x =>
            Seq(
              TableRow(x.code.toText),
              TableRow(x.text.getOrElse("").toText)
            )
        }

        val table = Table(
          rows,
          Some(
            Seq(
              HeadCell(messages("guarantee.rejected.message.error").toText),
              HeadCell(messages("guarantee.rejected.message.furtherInformation").toText)
            )
          )
        )

        (title, guaranteeReference.GRN, table)
    }
}
