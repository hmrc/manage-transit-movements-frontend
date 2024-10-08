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

package views.departureP5.drafts

import forms.DeparturesSearchFormProvider
import models.{DepartureUserAnswerSummary, DeparturesSummary}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.Assertion
import play.twirl.api.HtmlFormat
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala
import viewModels.drafts.AllDraftDeparturesViewModel
import viewModels.drafts.AllDraftDeparturesViewModel.DraftDepartureRow
import viewModels.pagination.ListPaginationViewModel
import views.behaviours.PaginationViewBehaviours
import views.html.departureP5.drafts.DashboardView

class DashboardViewSpec extends PaginationViewBehaviours[ListPaginationViewModel] {

  override val buildViewModel: (Int, Int, Int, String) => ListPaginationViewModel =
    ListPaginationViewModel(_, _, _, _)

  val departuresSummary: DeparturesSummary = arbitrary[DeparturesSummary].sample.value

  val paginationViewModel: ListPaginationViewModel = ListPaginationViewModel(2, 1, 2, "test")

  val viewAllDepartureMovementsViewModel: AllDraftDeparturesViewModel =
    AllDraftDeparturesViewModel(departuresSummary, 20, None, frontendAppConfig.p5Departure, paginationViewModel)

  val dataRows: Seq[DraftDepartureRow] = viewAllDepartureMovementsViewModel.dataRows

  private val formProvider = new DeparturesSearchFormProvider()
  private val form         = formProvider()

  override val prefix = "departure.drafts.dashboard"

  override val movementsPerPage: Int = paginationAppConfig.draftDeparturesNumberOfDrafts

  override def viewWithSpecificPagination(paginationViewModelP5: ListPaginationViewModel): HtmlFormat.Appendable =
    applyView(
      AllDraftDeparturesViewModel(
        arbitrary[DeparturesSummary].sample.value,
        movementsPerPage,
        None,
        frontendAppConfig.p5Departure,
        paginationViewModelP5
      )
    )

  private def applyView(
    viewAllDepartureMovementsViewModel: AllDraftDeparturesViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[DashboardView]
      .apply(form, viewAllDepartureMovementsViewModel)(fakeRequest, messages)

  override def view: HtmlFormat.Appendable = applyView(viewAllDepartureMovementsViewModel)

  behave like pageWithFullWidth()

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithPagination(controllers.departureP5.drafts.routes.DashboardController.onPageLoad(None, None, None).url)

  val rows: Elements = doc.select("tr[data-testrole^=draft-list_row]")

  "must generate a row for each draft" in {
    rows.size() mustEqual departuresSummary.userAnswers.size
  }

  "must have visually hidden text on table headers" in {
    val tableHeaders = doc.getElementsByTag("th").toList

    tableHeaders.size `mustBe` 3

    def check(th: Element, expectedVisuallyHiddenText: String): Assertion = {
      val visuallyHiddenText = th.getElementsByClass("govuk-visually-hidden").text()
      visuallyHiddenText `mustBe` expectedVisuallyHiddenText
    }

    check(tableHeaders.head, viewAllDepartureMovementsViewModel.sortHiddenTextLRN)
    check(tableHeaders(1), viewAllDepartureMovementsViewModel.sortHiddenTextDaysToComplete)
    check(tableHeaders(2), "Actions")
  }

  "must generate correct data in each row" - {
    rows.toList.zipWithIndex.foreach {
      case (row, rowIndex) =>
        val draft: DepartureUserAnswerSummary = departuresSummary.userAnswers(rowIndex)

        s"when row ${rowIndex + 1}" - {

          def elementWithVisibleText(element: Element, text: String): Unit =
            element.ownText() `mustBe` text

          def elementWithHiddenText(element: Element, text: String): Unit = {
            val heading = element.getElementsByClass("responsive-table__heading").head
            heading.attr("aria-hidden").toBoolean `mustBe` true
            heading.text() `mustBe` text
          }

          "Local reference number" - {

            val lrn     = row.selectFirst("td[data-testrole*=-lrn]")
            val lrnLink = lrn.getElementsByClass("govuk-link").head

            "must display correct text" in {
              behave like elementWithVisibleText(lrnLink, draft.lrn.toString)
            }

            "must have correct href" in {

              val redirectLink = s"${frontendAppConfig.p5Departure}/drafts/${draft.lrn}"
              lrnLink.attr("href") `mustBe` redirectLink
            }
          }

          "must display correct days remaining" in {
            val daysToComplete = row.selectFirst("td[data-testrole*=-daysToComplete]")

            behave like elementWithVisibleText(daysToComplete, draft.expiresInDays.toString)
            behave like elementWithHiddenText(daysToComplete, messages(s"$prefix.table.daysToComplete"))
          }

          "Delete" - {

            val delete     = row.selectFirst("td[data-testrole*=-delete]")
            val deleteLink = delete.getElementsByClass("govuk-link").head

            "must display correct text" in {
              behave like elementWithVisibleText(deleteLink, s"${messages(s"$prefix.table.action.delete")}")
              val hiddenText = deleteLink.getElementsByClass("govuk-visually-hidden").head
              hiddenText.text() `mustBe` s"Local Reference Number (LRN) ${draft.lrn}"
            }

            "must have correct href" in {

              val redirectLink =
                controllers.departureP5.drafts.routes.DeleteDraftDepartureYesNoController
                  .onPageLoad(draft.lrn, 1, rows.toList.length, None)
                  .url

              deleteLink.attr("href") `mustBe` redirectLink
            }
          }
        }
    }
  }

  "no search results found" - {

    "must render when no data rows for a search" in {

      val draftDeparture = DeparturesSummary(1, 0, List.empty)
      val view = applyView(viewAllDepartureMovementsViewModel =
        AllDraftDeparturesViewModel(draftDeparture, 20, Some("AB123"), frontendAppConfig.p5Departure, paginationViewModel)
      )

      val doc = Jsoup.parse(view.toString())

      doc.getElementById("no-search-results-found").text() `mustBe` "This LRN does not exist."
    }

    "must not render when there are data rows" in {
      assertNotRenderedById(doc, "no-search-results-found")
    }
  }

  "no results found" - {

    "must render when no data rows" in {

      val draftDeparture = DeparturesSummary(0, 0, List.empty)
      val view = applyView(viewAllDepartureMovementsViewModel =
        AllDraftDeparturesViewModel(draftDeparture, 20, None, frontendAppConfig.p5Departure, paginationViewModel)
      )

      val doc = Jsoup.parse(view.toString())

      doc.getElementById("no-results-found").text() `mustBe` "You have no draft departure declarations."
    }

    "must not render when there are data rows" in {

      val draftDeparture = DeparturesSummary(1, 0, List.empty)
      val view = applyView(viewAllDepartureMovementsViewModel =
        AllDraftDeparturesViewModel(draftDeparture, 20, None, frontendAppConfig.p5Departure, paginationViewModel)
      )

      val doc = Jsoup.parse(view.toString())

      assertNotRenderedById(doc, "no-results-found")
    }
  }

  "panel" - {

    val panel = doc.getElementsByClass("ticket-panel")

    "must render panel" in {
      panel.headOption must be(defined)
    }

    "must render correct header" in {
      panel.head.getElementsByClass("govuk-heading-m").text() `mustBe` "Make a new departure declaration"
    }

    "must render correct text" in {
      panel.head
        .getElementsByClass("govuk-body")
        .text() `mustBe` "You have 30 days from starting a declaration to complete it."
    }

    "must render href button" in {
      panel.head.getElementsByClass("govuk-button").text() `mustBe` "Start now"
      panel.head.getElementsByClass("govuk-button").attr("href") `mustBe` frontendAppConfig.p5Departure
    }
  }

}
