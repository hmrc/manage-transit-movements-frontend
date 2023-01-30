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

package views.departure.drafts

import generators.Generators
import models.DraftDeparture
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.twirl.api.HtmlFormat
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala
import viewModels.drafts.AllDraftDeparturesViewModel
import viewModels.drafts.AllDraftDeparturesViewModel.{getRemainingDays, DraftDepartureRow}
import views.behaviours.ViewBehaviours
import views.html.departure.drafts.DashboardView

import java.time.LocalDate

class DashboardViewSpec extends ViewBehaviours with Generators with ScalaCheckPropertyChecks {

  val genDraftDeparture: List[DraftDeparture]                         = arbitrary[List[DraftDeparture]].sample.value
  val viewAllDepartureMovementsViewModel: AllDraftDeparturesViewModel = AllDraftDeparturesViewModel(genDraftDeparture)
  val dataRows: Seq[DraftDepartureRow]                                = viewAllDepartureMovementsViewModel.dataRows

  override val prefix = "departure.drafts.dashboard"

  private def applyView(
    viewAllDepartureMovementsViewModel: AllDraftDeparturesViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[DashboardView]
      .apply(viewAllDepartureMovementsViewModel)(fakeRequest, messages)

  override def view: HtmlFormat.Appendable = applyView(viewAllDepartureMovementsViewModel)

  behave like pageWithFullWidth()

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  val rows: Elements = doc.select("tr[data-testrole^=movements-list_row]")

  "must generate a row for each movement" in {
    rows.size() mustEqual genDraftDeparture.size
  }

  "must generate correct data in each row" - {
    rows.toList.zipWithIndex.foreach {
      case (row, rowIndex) =>
        val viewDraftDeparture = genDraftDeparture(rowIndex)

        s"when row ${rowIndex + 1}" - {

          def elementWithVisibleText(element: Element, text: String): Unit =
            element.ownText() mustBe text

          def elementWithHiddenText(element: Element, text: String): Unit = {
            val heading = element.getElementsByClass("responsive-table__heading").head
            heading.attr("aria-hidden").toBoolean mustBe true
            heading.text() mustBe text
          }

          "must display correct local reference number" in {
            val lrn = row.selectFirst("td[data-testrole*=-lrn]")

            behave like elementWithVisibleText(lrn, viewDraftDeparture.lrn.value)
            behave like elementWithHiddenText(lrn, messages(s"$prefix.table.$lrn"))
          }

          "must display correct days remaining" in {
            val daysToComplete = row.selectFirst("td[data-testrole*=-daysToComplete]")

            val daysRemaining = getRemainingDays(viewDraftDeparture.createdAt, LocalDate.now())

            behave like elementWithVisibleText(daysToComplete, daysRemaining.toString)
            behave like elementWithHiddenText(daysToComplete, messages(s"$prefix.table.daysToComplete"))
          }

          "must display delete actions" - {
            val actions = row.selectFirst("td[data-testrole*=-actions]")

            val actionLinks = actions.getElementsByClass("govuk-link")
            actionLinks.zipWithIndex.foreach {
              case (link, linkIndex) =>
                s"when action ${linkIndex + 1}" - {

                  "must display correct text" in {
                    link.text() mustBe messages(s"${prefix.table.action.delete} for ${viewDraftDeparture.lrn}") // TODO - When href links are in

                    behave like elementWithVisibleText(link, s"$prefix.table.action.delete")

                    val hiddenText = link.getElementsByClass("govuk-visually-hidden").head
                    hiddenText.text() mustBe s"for ${viewDraftDeparture.lrn}"
                  }

                  "must have correct href" ignore {
                    link.attr("href") mustBe ???
                  }
                }
            }
          }
        }
    }
  }

}
