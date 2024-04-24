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

package views.departureP5

import generators.Generators
import org.jsoup.nodes.Document
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.P5.departure.{ViewAllDepartureMovementsP5ViewModel, ViewDepartureP5}
import viewModels.pagination.ListPaginationViewModel
import views.behaviours.{MovementsTableViewBehaviours, PaginationViewBehaviours, SearchViewBehaviours}
import views.html.departureP5.ViewAllDeparturesP5View

class ViewAllDeparturesP5ViewSpec
    extends MovementsTableViewBehaviours[ViewDepartureP5]
    with SearchViewBehaviours[ViewDepartureP5]
    with PaginationViewBehaviours[ListPaginationViewModel]
    with Generators
    with ScalaCheckPropertyChecks {

  override val prefix: String = "viewDepartureDeclarationsP5"

  override val referenceNumberType: String = "lrn"

  override val movementsPerPage: Int = paginationAppConfig.departuresNumberOfMovements

  private val viewAllDepartureMovementsP5ViewModel = arbitrary[ViewAllDepartureMovementsP5ViewModel].sample.value

  override val dataRows: Seq[(String, Seq[ViewDepartureP5])] = viewAllDepartureMovementsP5ViewModel.dataRows

  override val viewMovements: Seq[ViewDepartureP5] = dataRows.flatMap(_._2)

  override def viewWithSpecificPagination(paginationViewModel: ListPaginationViewModel): HtmlFormat.Appendable =
    viewWithSpecificPagination(form, Nil, paginationViewModel, None)

  private def viewWithSpecificPagination(
    form: Form[String],
    departures: Seq[ViewDepartureP5],
    paginationViewModel: ListPaginationViewModel,
    searchParam: Option[String]
  ): HtmlFormat.Appendable =
    applyView(form, ViewAllDepartureMovementsP5ViewModel(departures, paginationViewModel, searchParam))

  override def applyView(form: Form[String]): HtmlFormat.Appendable = applyView(form, viewAllDepartureMovementsP5ViewModel)

  override val buildViewModel: (Int, Int, Int, String) => ListPaginationViewModel = ListPaginationViewModel(_, _, _, _)

  private def applyView(
    form: Form[String],
    viewAllDepartureMovementsP5ViewModel: ViewAllDepartureMovementsP5ViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ViewAllDeparturesP5View]
      .apply(form, viewAllDepartureMovementsP5ViewModel)(fakeRequest, messages)

  behave like pageWithFullWidth()

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithLink(
    id = "make-departure-declaration",
    expectedText = "Make a departure declaration",
    expectedHref = frontendAppConfig.startDepartureDeclarationStartUrl
  )

  behave like pageWithMovementSearch("Search by Movement Reference Number (MRN)")

  behave like pageWithLink(
    id = "go-to-manage-transit-movements",
    expectedText = "Manage your transit movements",
    expectedHref = controllers.routes.WhatDoYouWantToDoController.onPageLoad().url
  )

  "must render search result text" - {
    "when 1 page" - {
      "and search param provided" in {
        val departures          = listWithMaxLength[ViewDepartureP5]().sample.value
        val paginationViewModel = buildViewModel(1, 1, movementsPerPage, "")
        val searchParam         = "LRN123"
        val filledForm          = form.fill(searchParam)
        val doc: Document       = parseView(viewWithSpecificPagination(filledForm, departures, paginationViewModel, Some(searchParam)))
        val p                   = doc.getElementById("results-count")
        p.text() mustBe "Showing 1 result matching LRN123"
        boldWords(p) mustBe Seq("1")
      }

      "when search param not provided" in {
        val departures          = listWithMaxLength[ViewDepartureP5]().sample.value
        val paginationViewModel = buildViewModel(1, 1, movementsPerPage, "")
        val doc: Document       = parseView(viewWithSpecificPagination(form, departures, paginationViewModel, None))
        val p                   = doc.getElementById("results-count")
        p.text() mustBe "Showing 1 result"
        boldWords(p) mustBe Seq("1")
      }
    }

    "when there are no results" in {
      val paginationViewModel = buildViewModel(1, 1, movementsPerPage, "")
      val doc: Document       = parseView(viewWithSpecificPagination(form, Nil, paginationViewModel, None))
      val p                   = doc.getElementById("no-results-found")
      p.text() mustBe "No results found"
    }
  }
}
