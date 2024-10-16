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
import models.departureP5.GuaranteeReferenceTable
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import scalaxb.XMLCalendar
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import viewModels.P5.departure.GuaranteeRejectedNotAmendableP5ViewModel
import views.behaviours.TableViewBehaviours
import views.html.departureP5.GuaranteeRejectedNotAmendableP5View

class GuaranteeRejectedNotAmendableP5ViewSpec extends TableViewBehaviours with Generators {

  override val headCells: Seq[HeadCell] =
    Seq(HeadCell(Text("Error")), HeadCell(Text("Further information")))

  override val tableRows: Seq[TableRow] = arbitrary[Seq[TableRow]].sample.value

  override val prefix: String = "guarantee.rejected.message.notAmendable"

  private val table = arbitrary[GuaranteeReferenceTable].sample.value.table.copy(rows = Seq(tableRows), head = Some(headCells))

  private val tables = Seq(GuaranteeReferenceTable("title", "grn", table))

  private val defaultViewModel: GuaranteeRejectedNotAmendableP5ViewModel =
    new GuaranteeRejectedNotAmendableP5ViewModel(tables, lrn.toString, mrn, XMLCalendar("2022-07-15"))

  override def view: HtmlFormat.Appendable = injector
    .instanceOf[GuaranteeRejectedNotAmendableP5View]
    .apply(defaultViewModel, departureIdP5, messageId)(fakeRequest, messages)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithTable()

  behave like pageWithoutSubmitButton()

  behave like pageWithLink(
    "makeNewDeparture",
    "Make another departure declaration",
    frontendAppConfig.p5Departure
  )

  "must change paragraph 1 text" - {

    "when there is only one guarantee reference with one error" - {

      val viewModel = defaultViewModel
        .copy(tables =
          Seq(
            GuaranteeReferenceTable("title", "GRN", table.copy(rows = Seq(tableRows)))
          )
        )

      val document = parseView(
        injector
          .instanceOf[GuaranteeRejectedNotAmendableP5View]
          .apply(viewModel, departureIdP5, messageId)(fakeRequest, messages)
      )

      behave like pageWithContent(
        document,
        "p",
        "There is a problem with the guarantee in this declaration. Review the error and make a new declaration with the right information."
      )
    }

    "when there is only one guarantee reference with multiple errors" - {

      val viewModel = defaultViewModel
        .copy(tables =
          Seq(
            GuaranteeReferenceTable("title", "GRN", table.copy(rows = Seq(tableRows, tableRows)))
          )
        )

      val document = parseView(
        injector
          .instanceOf[GuaranteeRejectedNotAmendableP5View]
          .apply(viewModel, departureIdP5, messageId)(fakeRequest, messages)
      )

      behave like pageWithContent(
        document,
        "p",
        "There is a problem with the guarantee in this declaration. Review the errors and make a new declaration with the right information."
      )
    }

    "when there is multiple guarantee references with multiple errors each" - {

      val viewModel = defaultViewModel
        .copy(tables =
          Seq(
            GuaranteeReferenceTable("title", "GRN", table.copy(rows = Seq(tableRows, tableRows))),
            GuaranteeReferenceTable("title", "GRN", table.copy(rows = Seq(tableRows, tableRows)))
          )
        )

      val document = parseView(
        injector
          .instanceOf[GuaranteeRejectedNotAmendableP5View]
          .apply(viewModel, departureIdP5, messageId)(fakeRequest, messages)
      )

      behave like pageWithContent(
        document,
        "p",
        "There is a problem with the guarantees in this declaration. Review the errors and make a new declaration with the right information."
      )
    }

  }

  "must change helpdesk link text" - {

    "when there is only one reference with one error" - {

      val viewModel = defaultViewModel
        .copy(tables =
          Seq(
            GuaranteeReferenceTable("title", "GRN", table.copy(rows = Seq(tableRows)))
          )
        )

      val document = parseView(
        injector
          .instanceOf[GuaranteeRejectedNotAmendableP5View]
          .apply(viewModel, departureIdP5, messageId)(fakeRequest, messages)
      )

      behave like pageWithLink(
        document,
        "helpdesk-link",
        "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab).",
        frontendAppConfig.nctsEnquiriesUrl
      )
    }

    "when there is multiple references or errors" - {

      val viewModel = defaultViewModel
        .copy(tables =
          Seq(
            GuaranteeReferenceTable("title", "GRN", table.copy(rows = Seq(tableRows))),
            GuaranteeReferenceTable("title", "GRN", table.copy(rows = Seq(tableRows)))
          )
        )

      val document = parseView(
        injector
          .instanceOf[GuaranteeRejectedNotAmendableP5View]
          .apply(viewModel, departureIdP5, messageId)(fakeRequest, messages)
      )

      behave like pageWithLink(
        document,
        "helpdesk-link",
        "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab).",
        frontendAppConfig.nctsEnquiriesUrl
      )
    }
  }

}
