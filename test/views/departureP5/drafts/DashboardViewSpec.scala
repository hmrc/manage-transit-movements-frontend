/*
 * Copyright 2024 HM Revenue & Customs
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
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala
import viewModels.drafts.AllDraftDeparturesViewModel
import viewModels.drafts.AllDraftDeparturesViewModel.DraftDepartureRow
import views.behaviours.{PaginationViewBehaviours, SearchViewBehaviours}
import views.html.departureP5.drafts.DashboardView

class DashboardViewSpec extends SearchViewBehaviours with PaginationViewBehaviours[DepartureUserAnswerSummary, AllDraftDeparturesViewModel] {

  override def form: Form[String] = new DeparturesSearchFormProvider()()

  override val viewModel: AllDraftDeparturesViewModel =
    arbitraryAllDraftDeparturesViewModel.arbitrary.sample.value

  override def buildViewModel(
    totalNumberOfItems: Int,
    currentPage: Int,
    numberOfItemsPerPage: Int
  ): AllDraftDeparturesViewModel =
    viewModel.copy(
      departures = {
        def departure: DepartureUserAnswerSummary = arbitrary[DepartureUserAnswerSummary].sample.value
        DeparturesSummary(totalNumberOfItems, totalNumberOfItems, List.fill(totalNumberOfItems)(departure))
      },
      currentPage = currentPage,
      numberOfItemsPerPage = numberOfItemsPerPage
    )

  override val prefix: String = "departure.drafts.dashboard"

  override val movementsPerPage: Int = paginationAppConfig.draftDeparturesNumberOfDrafts

  override def viewWithSpecificPagination(viewModel: AllDraftDeparturesViewModel): HtmlFormat.Appendable =
    viewWithSpecificPagination(form, viewModel)

  override def viewWithSpecificSearchResults(
    numberOfSearchResults: Int,
    currentPage: Int,
    numberOfItemsPerPage: Int,
    searchParam: String
  ): HtmlFormat.Appendable =
    viewWithSpecificPagination(
      form.fill(searchParam),
      viewModel.copy(
        departures = {
          def departure: DepartureUserAnswerSummary = arbitrary[DepartureUserAnswerSummary].sample.value
          DeparturesSummary(numberOfSearchResults, numberOfSearchResults, List.fill(numberOfSearchResults)(departure))
        },
        currentPage = currentPage,
        numberOfItemsPerPage = numberOfItemsPerPage,
        lrn = Some(searchParam)
      )
    )

  private def viewWithSpecificPagination(
    form: Form[String],
    viewModel: AllDraftDeparturesViewModel
  ): HtmlFormat.Appendable =
    applyView(form, viewModel)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    applyView(form, viewModel)

  private def applyView(
    form: Form[String],
    viewModel: AllDraftDeparturesViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[DashboardView]
      .apply(form, viewModel)(fakeRequest, messages)

  override def view: HtmlFormat.Appendable = applyView(form, viewModel)

  behave like pageWithFullWidth()

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithSearch(
    "Search by local reference number (LRN)",
    "No results found",
    viewModel.numberOfItemsPerPage
  )

  behave like pageWithPagination()

  val drafts: Seq[DepartureUserAnswerSummary] = viewModel.items

  val rows: Elements = doc.select("tr[data-testrole^=draft-list_row]")

  "must generate a row for each draft" in {
    rows.size() mustEqual drafts.size
  }

  "must have table headers" in {
    val tableHeaders = doc.getElementsByTag("th").toList

    tableHeaders.size mustEqual 3
  }

  "must generate correct data in each row" - {
    rows.toList.zipWithIndex.foreach {
      case (row, rowIndex) =>
        val draft: DepartureUserAnswerSummary = drafts(rowIndex)

        s"when row ${rowIndex + 1}" - {

          def elementWithVisibleText(element: Element, text: String): Unit =
            element.ownText() mustEqual text

          "Local reference number" - {

            val lrn     = row.selectFirst("td[data-testrole*=-lrn]")
            val lrnLink = lrn.getElementsByClass("govuk-link").head

            "must display correct text" in {
              behave like elementWithVisibleText(lrnLink, draft.lrn.toString)
            }

            "must have correct href" in {

              val redirectLink = s"${frontendAppConfig.p5Departure}/drafts/${draft.lrn}"
              lrnLink.attr("href") mustEqual redirectLink
            }
          }

          "must display correct days remaining" in {
            val daysToComplete = row.selectFirst("td[data-testrole*=-daysToComplete]")

            behave like elementWithVisibleText(daysToComplete, draft.expiresInDays.toString)
          }

          "Delete" - {

            val delete     = row.selectFirst("td[data-testrole*=-delete]")
            val deleteLink = delete.getElementsByClass("govuk-link").head

            "must display correct text" in {
              behave like elementWithVisibleText(deleteLink, s"${messages(s"$prefix.table.action.delete")}")
              val hiddenText = deleteLink.getElementsByClass("govuk-visually-hidden").head
              hiddenText.text() mustEqual s"Local Reference Number (LRN) ${draft.lrn}"
            }

            "must have correct href" in {
              deleteLink.attr("href") mustEqual viewModel.deleteDraftUrl(DraftDepartureRow(draft)).url
            }
          }
        }
    }
  }

  "panel" - {

    val panel = doc.getElementsByClass("moj-ticket-panel")

    "must render panel" in {
      panel.headOption must be(defined)
    }

    "must render correct header" in {
      panel.head.getElementsByClass("govuk-heading-m").text() mustEqual "Make a new departure declaration"
    }

    "must render correct text" in {
      panel.head
        .getElementsByClass("govuk-body")
        .text() mustEqual "You have 30 days from starting a declaration to complete it."
    }

    "must render href button" in {
      panel.head.getElementsByClass("govuk-button").text() mustEqual "Start now"
      panel.head.getElementsByClass("govuk-button").attr("href") mustEqual frontendAppConfig.p5Departure
    }
  }

}
