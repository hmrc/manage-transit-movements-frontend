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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewModels.ErrorViewModel.ErrorRow

trait ErrorRowViewBehaviours extends ViewBehaviours {

  val errorRows: Seq[ErrorRow]

  def pageWithErrorRows(): Unit =
    "page with Error Row" - {
      val renderedRows  = doc.getElementsByClass("govuk-table__row").toList
      val renderedLists = renderedRows.tail

      "must render Heading Rows " - {
        val renderedHead = renderedRows.head.getElementsByClass("govuk-table__header").toList

        "must contain a error Code header " in {
          val value = renderedHead.head.text()
          Text(value) mustBe Text("Error code")
        }

        "must contain a error reason header " in {
          val value = renderedHead.last.text()
          Text(value) mustBe Text("Reason")
        }

      }
      errorRows.zipWithIndex.foreach {
        case (errorRow, listIndex) =>
          val renderedList = renderedLists(listIndex)

          s"list ${listIndex + 1}" - {
            val renderedRows: List[Element] = renderedList.getElementsByClass("govuk-table__cell").toList

            val renderedErrorCodeRow: Element   = renderedRows.head
            val renderedErrorReasonRow: Element = renderedRows.last

            "must contain a error Code " in {
              val value = renderedErrorCodeRow.text()
              Text(value) mustBe Text(errorRow.errorCode)
            }

            "must contain a error Reason " in {
              val value = renderedErrorReasonRow.text()
              Text(value) mustBe Text(errorRow.errorReason)
            }

          }
      }
    }
}
