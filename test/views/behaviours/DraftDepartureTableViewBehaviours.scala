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

import models.DraftDeparture
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.twirl.api.TwirlHelperImports._
import viewModels.ViewMovement

import java.time.LocalDate

// scalastyle:off method.length
// scalastyle:off magic.number
trait DraftDepartureTableViewBehaviours extends ViewBehaviours with ScalaCheckPropertyChecks {

  val draftDepartures: Seq[DraftDeparture]

  val messageKeyPrefix = "viewDraftDepartures.table"

  def pageWithMovementsData(): Unit =
    "page with a movements data table" - {

      "must generate correct headings" in {
        val elements: Elements = doc.getElementsByAttributeValue("data-testrole", "movements-list_group-heading")

        val headers = List(messages(s"$messageKeyPrefix.lrn"), messages(s"$messageKeyPrefix.daysToComplete"))

        elements.forEach {
          element =>
            headers.contains(element.text()) mustBe true
        }
      }

      val rows: Elements = doc.select("tr[data-testrole^=movements-list_row]")

      "must generate a row for each draft departure" in {
        rows.size() mustEqual draftDepartures.size
      }

      "must generate correct data in each row" - {
        rows.toList.zipWithIndex.foreach {
          case (row, rowIndex) =>
            val draftDeparture = draftDepartures(rowIndex)

            s"when row ${rowIndex + 1}" - {

              def elementWithVisibleText(element: Element, text: String): Unit =
                element.ownText() mustBe text

              def elementWithHiddenText(element: Element, text: String): Unit = {
                val heading = element.getElementsByClass("responsive-table__heading").head
                heading.attr("aria-hidden").toBoolean mustBe true
                heading.text() mustBe text
              }

              "must display correct lrn" in {
                val lrn = row.selectFirst("td[data-testrole*=-lrn]")

                behave like elementWithVisibleText(lrn, draftDeparture.lrn.toString)
                behave like elementWithHiddenText(lrn, messages(s"$prefix.table.lrn"))
              }

              "must display correct status" in {
                val daysToComplete = row.selectFirst("td[data-testrole*=-daysToComplete]")


                val daysLeft = LocalDate.now().until(draftDeparture.createdAt.plusDays(30)).getDays.toString

                behave like elementWithVisibleText(daysToComplete, daysLeft)
                behave like elementWithHiddenText(daysToComplete, messages(s"$prefix.table.daysToComplete"))
              }

              "must display actions" - {
                val actions = row.selectFirst("td[data-testrole*=-actions]")
                behave like elementWithVisibleText(actions, "Delete")

                "must include hidden content" in {
                  behave like elementWithHiddenText(actions, messages(s"$prefix.table.action"))
                }
              }
// TODO - Add test for Delete href

//                val actionLinks = actions.getElementsByClass("govuk-link")
//                actionLinks.zipWithIndex.foreach {
//                  case (link, linkIndex) =>
//                    val action = viewMovement.actions(linkIndex)
//
//                    s"when action ${linkIndex + 1}" - {
//
//                      "must display correct text" in {
//                        link.text() mustBe s"${action.key} for ${viewMovement.referenceNumber}"
//
//                        behave like elementWithVisibleText(link, s"${action.key}")
//
//                        val hiddenText = link.getElementsByClass("govuk-visually-hidden").head
//                        hiddenText.text() mustBe s"for ${viewMovement.referenceNumber}"
//                      }
//
//                      "must have correct id" in {
//                        link.attr("id") mustBe s"${action.key}-${viewMovement.referenceNumber}"
//                      }
//
//                      "must have correct href" in {
//                        link.attr("href") mustBe action.href
//                      }
//                    }
              }
            }
        }
      }

  protected def boldWords(p: Element): Seq[String] = p.getElementsByTag("b").toList.map(_.text())

}
// scalastyle:on method.length
// scalastyle:on magic.number
