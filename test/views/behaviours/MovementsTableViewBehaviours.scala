/*
 * Copyright 2022 HM Revenue & Customs
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
import org.jsoup.select.Elements
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.ViewMovement

import scala.collection.convert.ImplicitConversions._

// scalastyle:off method.length
// scalastyle:off magic.number
trait MovementsTableViewBehaviours[T <: ViewMovement] extends ViewBehaviours with ScalaCheckPropertyChecks {

  val viewMovements: Seq[T]

  val referenceNumberType: String

  val movementsPerPage: Int

  def pageWithMovementsData(): Unit =
    "page with a movements data table" - {

      "must generate a heading for each unique day" in {
        val elements: Elements = doc.getElementsByAttributeValue("data-testrole", "movements-list_group-heading")

        // regex for date formatted as dd MMMM yyyy
        val dateRegex = "^(([0-9])|([0-2][0-9])|([3][0-1])) (January|February|March|April|May|June|July|August|September|October|November|December) \\d{4}$"

        elements.forEach {
          element =>
            element.text().matches(dateRegex) mustBe true
        }
      }

      val rows: Elements = doc.select("tr[data-testrole^=movements-list_row]")

      "must generate a row for each movement" in {
        rows.size() mustEqual viewMovements.size
      }

      "must generate correct data in each row" - {
        rows.toList.zipWithIndex.forEach {
          case (row, rowIndex) =>
            s"when row ${rowIndex + 1}" - {

              def elementWithVisibleText(element: Element, text: String): Unit =
                element.ownText() mustBe text

              def elementWithHiddenText(element: Element, text: String): Unit = {
                val heading = element.getElementsByClass("responsive-table__heading").head
                heading.attr("aria-hidden").toBoolean mustBe true
                heading.text() mustBe text
              }

              "must display time" in {
                val updated   = row.selectFirst("td[data-testrole*=-updated]")
                val timeRegex = "^(([1-9])|([1][0-2])):(([0][0-9])|([1-5][0-9]))(am|pm)$"
                updated.ownText().matches(timeRegex) mustBe true
                behave like elementWithHiddenText(updated, messages(s"$prefix.table.updated"))
              }

              "must display correct reference number" in {
                val ref = row.selectFirst("td[data-testrole*=-ref]")

                behave like elementWithVisibleText(ref, viewMovements(rowIndex).referenceNumber)
                behave like elementWithHiddenText(ref, messages(s"$prefix.table.$referenceNumberType"))
              }

              "must display correct status" in {
                val status = row.selectFirst("td[data-testrole*=-status]")

                behave like elementWithVisibleText(status, viewMovements(rowIndex).status)
                behave like elementWithHiddenText(status, messages(s"$prefix.table.status"))
              }

              "must display actions" - {
                val actions = row.selectFirst("td[data-testrole*=-actions]")

                "must include hidden content" in {
                  behave like elementWithHiddenText(actions, messages(s"$prefix.table.action"))
                }

                val actionLinks = actions.getElementsByClass("govuk-link")
                actionLinks.zipWithIndex.forEach {
                  case (link, linkIndex) =>
                    s"when action ${linkIndex + 1}" - {

                      "must display correct text" in {
                        link.text() mustBe s"${viewMovements(rowIndex).actions(linkIndex).key} for ${viewMovements(rowIndex).referenceNumber}"

                        behave like elementWithVisibleText(link, s"${viewMovements(rowIndex).actions(linkIndex).key}")

                        val hiddenText = link.getElementsByClass("govuk-visually-hidden").head
                        hiddenText.text() mustBe s"for ${viewMovements(rowIndex).referenceNumber}"
                      }

                      "must have correct id" in {
                        link.attr("id") mustBe s"${viewMovements(rowIndex).actions(linkIndex).key}-${viewMovements(rowIndex).referenceNumber}"
                      }

                      "must have correct href" in {
                        link.attr("href") mustBe viewMovements(rowIndex).actions(linkIndex).href
                      }
                    }
                }
              }
            }
        }
      }
    }

  protected def boldWords(p: Element): Seq[String] = p.getElementsByTag("b").map(_.text())

}
// scalastyle:on method.length
// scalastyle:on magic.number
