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
import org.jsoup.select.Elements
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.twirl.api.TwirlHelperImports._
import viewModels.ViewMovement

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
            element.text().matches(dateRegex) `mustBe` true
        }
      }

      val rows: Elements = doc.getElementsByClass("govuk-table__body")

      "must generate a row for each movement" in {
        rows.size() mustEqual viewMovements.size
      }

      "must generate correct data in each row" - {
        rows.toList.zipWithIndex.foreach {
          case (row, rowIndex) =>
            val viewMovement = viewMovements(rowIndex)

            s"when row ${rowIndex + 1}" - {

              def elementWithVisibleText(element: Element, text: String): Unit =
                element.ownText() `mustBe` text

              "must display time" in {
                val updated   = row.getElementsByClass("govuk-table__cell").first()
                val timeRegex = "^(([1-9])|([1][0-2])):(([0][0-9])|([1-5][0-9]))(am|pm)$"
                updated.ownText().matches(timeRegex) `mustBe` true
              }

              "must display correct reference number" in {
                val ref = row.getElementsByClass("govuk-table__cell").get(1)

                behave like elementWithVisibleText(ref, viewMovement.referenceNumber)
              }

              "must display correct status" in {
                val status = row.getElementsByClass("govuk-table__cell").get(2)

                behave like elementWithVisibleText(status, viewMovement.status)
              }

              "must display actions" - {
                val actions = row.getElementsByClass("govuk-table__cell").get(3)

                val actionLinks = actions.getElementsByClass("govuk-link")
                actionLinks.zipWithIndex.foreach {
                  case (link, linkIndex) =>
                    val action = viewMovement.actions(linkIndex)

                    s"when action ${linkIndex + 1}" - {

                      "must display correct text" in {
                        link.text() `mustBe` s"${action.key} for ${viewMovement.referenceNumber}"

                        behave like elementWithVisibleText(link, s"${action.key}")

                        val hiddenText = link.getElementsByClass("govuk-visually-hidden").head
                        hiddenText.text() `mustBe` s"for ${viewMovement.referenceNumber}"
                      }

                      "must have correct id" in {
                        link.attr("id") `mustBe` s"${action.key}-${viewMovement.referenceNumber}"
                      }

                      "must have correct href" in {
                        link.attr("href") `mustBe` action.href
                      }
                    }
                }
              }
            }
        }
      }
    }

}

// scalastyle:on method.length
// scalastyle:on magic.number
