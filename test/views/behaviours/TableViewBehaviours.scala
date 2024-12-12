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

import org.jsoup.nodes.Element
import play.twirl.api.TwirlHelperImports.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table

trait TableViewBehaviours extends ViewBehaviours {

  val tables: Seq[Table]

  def pageWithTables(): Unit =
    "page with tables" - {
      doc.getElementsByClass("govuk-table").toList.zipWithIndex.foreach {
        case (table, index) =>
          s"when table $index" - {
            pageWithTable(table, tables(index))
          }
      }
    }

  // scalastyle:off method.length
  private def pageWithTable(table: Element, expectedTable: Table): Unit = {
    val rows = table.getElementsByClass("govuk-table__row")

    "header cells" - {
      rows.head.getElementsByClass("govuk-table__header").toList.zipWithIndex.foreach {
        case (cell, index) =>
          s"when header cell ${index + 1}" - {
            "must contain correct value" in {
              val value: String        = cell.text()
              val expectedText: String = expectedTable.head.get(index).content.asHtml.toString()
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
                  val expectedText: String = expectedTable.rows(rowIndex)(cellIndex).content.asHtml.toString()
                  value mustEqual expectedText
                }
              }
          }
      }
    }
  }
}
