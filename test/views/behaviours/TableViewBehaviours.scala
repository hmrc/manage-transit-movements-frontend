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
import play.twirl.api.TwirlHelperImports._
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}

trait TableViewBehaviours extends ViewBehaviours {
  val headCells: Seq[HeadCell]
  val tableRows: Seq[TableRow]

  // scalastyle:off method.length
  def pageWithTable(): Unit =
    "page with Table" - {
      val renderedHeaderRows: List[Element] = doc.getElementsByAttributeValue("scope", "col").toList
      val renderedTableRows: List[Element]  = (doc.getElementsByAttributeValue("scope", "row") ++ doc.getElementsByClass("govuk-table__cell")).toList

      "must render Heading Rows " - {
        renderedHeaderRows.zipWithIndex.foreach {
          case (headerRow, headerIndex) =>
            s"HeadCell ${headerIndex + 1}" - {
              "must contain correct header " in {
                val value: String        = headerRow.text()
                val expectedText: String = headCells(headerIndex).content.asHtml.toString()
                value `mustBe` expectedText
              }
            }
        }

      }
      "must render Table Rows " - {
        renderedTableRows.zipWithIndex.foreach {
          case (tableRow, tableRowIndex) =>
            s"Table Row ${tableRowIndex + 1}" - {
              "must contain correct row " in {
                val value: String        = tableRow.text()
                val expectedText: String = tableRows(tableRowIndex).content.asHtml.toString()
                value `mustBe` expectedText
              }
            }
        }

      }
    }

}
