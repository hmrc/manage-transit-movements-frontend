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

package views.behaviours

import play.twirl.api.TwirlHelperImports.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table

trait TableViewBehaviours extends ViewBehaviours {

  val table: Table

  // scalastyle:off method.length
  def pageWithTable(): Unit =
    "page with table" - {
      val rows = doc.getElementsByClass("govuk-table__row")

      "header cells" - {
        rows.head.getElementsByClass("govuk-table__header").toList.zipWithIndex.foreach {
          case (cell, index) =>
            s"when header cell ${index + 1}" - {
              "must contain correct value" in {
                val value: String        = cell.text()
                val expectedText: String = table.head.get(index).content.asHtml.toString()
                value mustEqual expectedText
              }
            }
        }
      }

      "table rows" - {
        rows.tail.zipWithIndex.foreach {
          case (row, rowIndex) =>
            row.getElementsByClass("govuk-table__cell").toList.zipWithIndex.foreach {
              case (cell, cellIndex) =>
                s"when row ${rowIndex + 1} cell ${cellIndex + 1}" - {
                  "must contain correct value" in {
                    val value: String        = cell.text()
                    val expectedText: String = table.rows(rowIndex)(cellIndex).content.asHtml.toString()
                    value mustEqual expectedText
                  }
                }
            }
        }
      }
    }
}
