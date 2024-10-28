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

import config.FrontendAppConfig
import generators.Generators
import models.departureP5.BusinessRejectionType
import org.jsoup.nodes.Document
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import viewModels.P5.departure.RejectionMessageP5ViewModel
import viewModels.pagination.PaginationViewModel
import viewModels.sections.Section
import views.behaviours.{PaginationViewBehaviours, TableViewBehaviours}
import views.html.departureP5.RejectionMessageP5View

class RejectionMessageP5ViewSpec extends PaginationViewBehaviours[PaginationViewModel] with TableViewBehaviours with Generators {

  override val headCells: Seq[HeadCell] =
    Seq(HeadCell(Text("Error")), HeadCell(Text("Business rule ID")), HeadCell(Text("Invalid data item")), HeadCell(Text("Invalid answer")))

  override val tableRows: Seq[TableRow] = arbitrary[Seq[TableRow]].sample.value

  override val prefix: String = "departure.ie056.message"

  override val buildViewModel: (Int, Int, Int, String) => PaginationViewModel =
    PaginationViewModel(_, _, _, _)

  override val movementsPerPage: Int = paginationAppConfig.departuresNumberOfMovements

  private val sections: Seq[Section] = arbitrary[List[Section]].sample.value

  private val rejectionMessageP5ViewModel: RejectionMessageP5ViewModel =
    new RejectionMessageP5ViewModel(Seq(tableRows), lrn.toString, false, BusinessRejectionType.DeclarationRejection)

  val paginationViewModel: PaginationViewModel = PaginationViewModel(
    totalNumberOfItems = sections.length,
    currentPage = 1,
    numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
    href = controllers.departureP5.routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, messageId).url
  )

  private def applyView(
    viewModel: RejectionMessageP5ViewModel,
    paginationViewModel: PaginationViewModel,
    mrn: Option[String]
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[RejectionMessageP5View]
      .apply(viewModel, departureIdP5, messageId, paginationViewModel, mrn)(fakeRequest, messages, frontendAppConfig)

  override def view: HtmlFormat.Appendable = applyView(rejectionMessageP5ViewModel, paginationViewModel, None)

  override def viewWithSpecificPagination(paginationViewModel: PaginationViewModel): HtmlFormat.Appendable =
    applyView(rejectionMessageP5ViewModel, paginationViewModel, None)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithoutFormAction()

  behave like pageWithSubmitButton("Amend errors")

  behave like pageWithCaption(s"LRN: $lrn")

  behave like pageWithPagination(
    controllers.departureP5.routes.RejectionMessageP5Controller.onPageLoad(None, departureIdP5, messageId).url
  )

  behave like pageWithTable()

  behave like pageWithSpecificContent(
    "paragraph-1",
    "There is a problem with this declaration. Amend the error and resend the declaration."
  )

  behave like pageWithSpecificContent(
    "create-another-declaration",
    "Make another departure declaration"
  )

  behave like pageWithLink(
    "helpdesk-link",
    "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab)",
    frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithLink(
    "departure-link",
    "Make another departure declaration",
    frontendAppConfig.p5Departure
  )

  "must not render table headings when no table rows" in {

    val rejectionMessageP5ViewModel: RejectionMessageP5ViewModel =
      new RejectionMessageP5ViewModel(Nil, lrn.toString, false, BusinessRejectionType.DeclarationRejection)

    val doc: Document = parseView(applyView(rejectionMessageP5ViewModel, paginationViewModel, None))
    assertElementDoesNotExist(doc, "govuk-table__head")

  }

  "must not render add another declaration link when isAmendmentJourney is true" in {
    val rejectionMessageP5ViewModel: RejectionMessageP5ViewModel =
      new RejectionMessageP5ViewModel(Nil, lrn.toString, false, BusinessRejectionType.AmendmentRejection)

    val doc: Document = parseView(applyView(rejectionMessageP5ViewModel, paginationViewModel, None))
    assertNotRenderedById(doc, "departure-link")
  }

  "must not render mrn when None" in {
    val doc: Document = parseView(applyView(rejectionMessageP5ViewModel, paginationViewModel, None))
    assertNotRenderedById(doc, "mrn")
  }

  "must render mrn when provided" in {
    val doc: Document = parseView(applyView(rejectionMessageP5ViewModel, paginationViewModel, Some("mrn")))
    assertRenderedById(doc, "mrn")
  }

  "when trader test enabled" - {
    "must not display helpdesk link" - {
      val app = new GuiceApplicationBuilder()
        .configure("trader-test.enabled" -> true)
        .build()

      running(app) {
        val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

        val view = app.injector
          .instanceOf[RejectionMessageP5View]
          .apply(
            rejectionMessageP5ViewModel,
            departureIdP5,
            messageId,
            paginationViewModel,
            None
          )(fakeRequest, messages, frontendAppConfig)

        val doc = parseView(view)

        behave like pageWithoutLink(
          doc,
          "helpdesk-link"
        )
      }
    }
  }

}
