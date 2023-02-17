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

import forms.SearchFormProvider
import models.{DepartureUserAnswerSummary, DeparturesSummary, LocalReferenceNumber}
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala
import viewModels.drafts.AllDraftDeparturesViewModel
import viewModels.drafts.AllDraftDeparturesViewModel.DraftDepartureRow
import viewModels.paginationP5.DraftsPaginationViewModel
import views.behaviours.PaginationP5ViewBehaviours
import views.html.departure.drafts.DashboardView

import java.time.LocalDateTime

class DashboardViewSpec extends PaginationP5ViewBehaviours[DeparturesSummary] {

  val genDraftDeparture: DeparturesSummary = arbitrary[DeparturesSummary].sample.value

  val paginationViewModel: DraftsPaginationViewModel = DraftsPaginationViewModel(2, 1, 2, "test")

  val viewAllDepartureMovementsViewModel: AllDraftDeparturesViewModel =
    AllDraftDeparturesViewModel(genDraftDeparture, 20, None, frontendAppConfig.draftDepartureFrontendUrl, paginationViewModel)
  val dataRows: Seq[DraftDepartureRow] = viewAllDepartureMovementsViewModel.dataRows

  private val formProvider = new SearchFormProvider()
  private val form         = formProvider()

  override val prefix = "departure.drafts.dashboard"

  override val movementsPerPage: Int = paginationAppConfig.draftDeparturesNumberOfDrafts

  override def viewWithSpecificPagination(paginationViewModelP5: DraftsPaginationViewModel): HtmlFormat.Appendable =
    applyView(
      AllDraftDeparturesViewModel(
        arbitrary[DeparturesSummary].sample.value,
        movementsPerPage,
        None,
        frontendAppConfig.draftDepartureFrontendUrl,
        paginationViewModelP5
      )
    )

  override def viewWithSpecificPaginationAndSearch(paginationViewModelP5: DraftsPaginationViewModel): HtmlFormat.Appendable =
    applyView(
      AllDraftDeparturesViewModel(
        arbitrary[DeparturesSummary].sample.value,
        movementsPerPage,
        Some(lrn.toString),
        frontendAppConfig.draftDepartureFrontendUrl,
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

  behave like pageWithPaginationP5(controllers.departure.drafts.routes.DashboardController.onPageLoad(None, None).url)

  val rows: Elements = doc.select("tr[data-testrole^=draft-list_row]")

  "must generate a row for each draft" in {
    rows.size() mustEqual genDraftDeparture.userAnswers.size
  }

  "must generate correct hidden text for delete heading" in {
    val deleteHiddenSpan = doc.getElementById("actionHidden").text()
    deleteHiddenSpan mustBe "Actions"
  }

  "search result text" - {

    "must render when 'isSearch' is true and is singular" in {

      val draftDeparture = DeparturesSummary(
        0,
        0,
        List(
          DepartureUserAnswerSummary(LocalReferenceNumber("AB123"), LocalDateTime.now(), 30)
        )
      )
      val view = applyView(viewAllDepartureMovementsViewModel =
        AllDraftDeparturesViewModel(draftDeparture, 20, Some("123"), frontendAppConfig.draftDepartureFrontendUrl, paginationViewModel)
      )

      val doc = Jsoup.parse(view.toString())

      doc.getElementById("results-found").text() mustBe s"Showing 1 result matching 123."
    }

    "must render when 'isSearch' is true and is plural" in {

      val draftDeparture = DeparturesSummary(
        0,
        0,
        List(
          DepartureUserAnswerSummary(LocalReferenceNumber("AB123"), LocalDateTime.now(), 30),
          DepartureUserAnswerSummary(LocalReferenceNumber("CD123"), LocalDateTime.now(), 29)
        )
      )
      val view = applyView(viewAllDepartureMovementsViewModel =
        AllDraftDeparturesViewModel(draftDeparture, 20, Some("123"), frontendAppConfig.draftDepartureFrontendUrl, paginationViewModel)
      )

      val doc = Jsoup.parse(view.toString())

      doc.getElementById("results-found").text() mustBe s"Showing 2 results matching 123."
    }

    "must not render when there are no drafts" in {
      val draftDeparture = DeparturesSummary(0, 0, List.empty)
      val view =
        applyView(viewAllDepartureMovementsViewModel =
          AllDraftDeparturesViewModel(draftDeparture, 20, None, frontendAppConfig.draftDepartureFrontendUrl, paginationViewModel)
        )

      val doc = Jsoup.parse(view.toString())

      doc.getElementById("results-found") mustBe null
    }

    "must not render when 'isSearch' is false" in {

      doc.getElementById("results-found") mustBe null
    }

  }

  "must generate correct data in each row" - {
    rows.toList.zipWithIndex.foreach {
      case (row, rowIndex) =>
        val viewDraftDeparture: DepartureUserAnswerSummary = genDraftDeparture.userAnswers(rowIndex)

        s"when row ${rowIndex + 1}" - {

          def elementWithVisibleText(element: Element, text: String): Unit =
            element.ownText() mustBe text

          def elementWithHiddenText(element: Element, text: String): Unit = {
            val heading = element.getElementsByClass("responsive-table__heading").head
            heading.attr("aria-hidden").toBoolean mustBe true
            heading.text() mustBe text
          }

          "Local reference number" - {

            val lrn     = row.selectFirst("td[data-testrole*=-lrn]")
            val lrnLink = lrn.getElementsByClass("govuk-link").head

            "must display correct text" in {
              behave like elementWithVisibleText(lrnLink, viewDraftDeparture.lrn.toString)
            }

            "must have correct href" in {

              val redirectLink = s"${frontendAppConfig.draftDepartureFrontendUrl}/drafts/${viewDraftDeparture.lrn}"
              lrnLink.attr("href") mustBe redirectLink
            }
          }

          "must display correct days remaining" in {
            val daysToComplete = row.selectFirst("td[data-testrole*=-daysToComplete]")

            val daysRemaining = viewDraftDeparture.expiresInDays

            behave like elementWithVisibleText(daysToComplete, daysRemaining.toString)
            behave like elementWithHiddenText(daysToComplete, messages(s"$prefix.table.daysToComplete"))
          }

          "Delete" - {

            val delete     = row.selectFirst("td[data-testrole*=-delete]")
            val deleteLink = delete.getElementsByClass("govuk-link").head

            "must display correct text" in {
              behave like elementWithVisibleText(deleteLink, s"${messages(s"$prefix.table.action.delete")}")
              val hiddenText = deleteLink.getElementsByClass("govuk-visually-hidden").head
              hiddenText.text() mustBe s"Local Reference Number (LRN) ${viewDraftDeparture.lrn}"
            }

            "must have correct href" in {

              val redirectLink =
                controllers.departure.drafts.routes.DeleteDraftDepartureYesNoController
                  .onPageLoad(viewDraftDeparture.lrn.toString(), 1, rows.toList.length, None)
                  .url

              deleteLink.attr("href") mustBe redirectLink
            }
          }
        }
    }
  }

  "no search results found" - {

    "must render when no data rows for a search" in {

      val draftDeparture = DeparturesSummary(1, 0, List.empty)
      val view = applyView(viewAllDepartureMovementsViewModel =
        AllDraftDeparturesViewModel(draftDeparture, 20, Some("AB123"), frontendAppConfig.draftDepartureFrontendUrl, paginationViewModel)
      )

      val doc = Jsoup.parse(view.toString())

      doc.getElementById("no-search-results-found").text() mustBe "This LRN does not exist."
    }

    "must not render when there are data rows" in {

      doc.getElementById("no-search-results-found") mustBe null
    }
  }

  "no results found" - {

    "must render when no data rows" in {

      val draftDeparture = DeparturesSummary(0, 0, List.empty)
      val view = applyView(viewAllDepartureMovementsViewModel =
        AllDraftDeparturesViewModel(draftDeparture, 20, None, frontendAppConfig.draftDepartureFrontendUrl, paginationViewModel)
      )

      val doc = Jsoup.parse(view.toString())

      doc.getElementById("no-results-found").text() mustBe "You have no draft departure declarations."
    }

    "must not render when there are data rows" in {

      val draftDeparture = DeparturesSummary(1, 0, List.empty)
      val view = applyView(viewAllDepartureMovementsViewModel =
        AllDraftDeparturesViewModel(draftDeparture, 20, None, frontendAppConfig.draftDepartureFrontendUrl, paginationViewModel)
      )

      val doc = Jsoup.parse(view.toString())

      doc.getElementById("no-results-found") mustBe null
    }
  }

  "panel" - {

    val panel = doc.getElementsByClass("ticket-panel")

    "must render panel" in {
      panel.headOption must be(defined)
    }

    "must render correct header" in {
      panel.head.getElementsByClass("govuk-heading-m").text() mustBe "Create a new departure declaration"
    }

    "must render correct text" in {
      panel.head
        .getElementsByClass("govuk-body")
        .text() mustBe "You have 30 days from starting a declaration to complete it."
    }

    "must render href button" in {
      panel.head.getElementsByClass("govuk-button").text() mustBe "Start now"
      panel.head.getElementsByClass("govuk-button").attr("href") mustBe frontendAppConfig.declareDepartureStartWithLRNUrl
    }
  }
}
