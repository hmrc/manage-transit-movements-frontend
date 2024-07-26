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

import generated._
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.twirl.api.HtmlFormat
import scalaxb.XMLCalendar
import viewModels.P5.departure.GuaranteeRejectedNotAmendableP5ViewModel
import views.behaviours.ViewBehaviours
import views.html.departureP5.GuaranteeRejectedNotAmendableP5View

import scala.jdk.CollectionConverters._

class GuaranteeRejectedNotAmendableP5ViewSpec extends ViewBehaviours with Generators {

  override val prefix: String = "guarantee.rejected.message.notAmendable"

  private val guaranteeReferences: Seq[GuaranteeReferenceType08] =
    Gen.nonEmptyListOf(arbitrary[GuaranteeReferenceType08]).sample.value

  val defaultViewModel: GuaranteeRejectedNotAmendableP5ViewModel = GuaranteeRejectedNotAmendableP5ViewModel(
    guaranteeReferences = guaranteeReferences,
    lrn = lrn.value,
    mrn = mrn,
    acceptanceDate = XMLCalendar("2022-07-15")
  )

  override def view: HtmlFormat.Appendable = injector
    .instanceOf[GuaranteeRejectedNotAmendableP5View]
    .apply(defaultViewModel, departureIdP5, messageId)(fakeRequest, messages)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithoutSubmitButton()

  behave like pageWithLink(
    "makeNewDeparture",
    "Make another departure declaration",
    frontendAppConfig.p5Departure
  )

  "must change paragraph 1 text" - {

    "when there is only one guarantee reference with one error" - {

      val viewModel = defaultViewModel
        .copy(guaranteeReferences =
          Seq(
            GuaranteeReferenceType08("1", "GRN", Seq(InvalidGuaranteeReasonType01("1", "test", None)))
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
        .copy(guaranteeReferences =
          Seq(
            GuaranteeReferenceType08(
              "1",
              "GRN",
              Seq(
                InvalidGuaranteeReasonType01("1", "test", None),
                InvalidGuaranteeReasonType01("2", "test2", None)
              )
            )
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

    "when there is multiple guarantee references with only one error each" - {

      val viewModel = defaultViewModel
        .copy(guaranteeReferences =
          Seq(
            GuaranteeReferenceType08("1", "GRN1", Seq(InvalidGuaranteeReasonType01("1", "test1", None))),
            GuaranteeReferenceType08("2", "GRN2", Seq(InvalidGuaranteeReasonType01("1", "test2", None)))
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
        "There is a problem with the guarantees in this declaration. Review the error and make a new declaration with the right information."
      )
    }

    "when there is multiple guarantee references with multiple errors each" - {

      val viewModel = defaultViewModel
        .copy(guaranteeReferences =
          Seq(
            GuaranteeReferenceType08(
              "1",
              "GRN1",
              Seq(
                InvalidGuaranteeReasonType01("1", "test1", None),
                InvalidGuaranteeReasonType01("2", "test2", None)
              )
            ),
            GuaranteeReferenceType08(
              "2",
              "GRN2",
              Seq(
                InvalidGuaranteeReasonType01("1", "test3", None),
                InvalidGuaranteeReasonType01("2", "test4", None)
              )
            )
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

  "must change paragraph 2 text" - {

    "when there is only one reference with one error" - {

      val viewModel = defaultViewModel
        .copy(guaranteeReferences =
          Seq(
            GuaranteeReferenceType08("1", "GRN", Seq(InvalidGuaranteeReasonType01("1", "test", None)))
          )
        )

      val document = parseView(
        injector
          .instanceOf[GuaranteeRejectedNotAmendableP5View]
          .apply(viewModel, departureIdP5, messageId)(fakeRequest, messages)
      )

      behave like pageWithContent(document, "p", "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab).")
    }

    "when there is multiple references or errors" - {

      val viewModel = defaultViewModel
        .copy(guaranteeReferences =
          Seq(
            GuaranteeReferenceType08("1", "GRN", Seq(InvalidGuaranteeReasonType01("1", "test", None))),
            GuaranteeReferenceType08("2", "GRN", Seq(InvalidGuaranteeReasonType01("1", "test", None)))
          )
        )

      val document = parseView(
        injector
          .instanceOf[GuaranteeRejectedNotAmendableP5View]
          .apply(viewModel, departureIdP5, messageId)(fakeRequest, messages)
      )

      behave like pageWithContent(document, "p", "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab).")
    }

    "must have correct link" in {

      val getElement = doc.getElementById("contact")

      assertElementContainsHref(getElement, frontendAppConfig.nctsEnquiriesUrl)
    }

  }

  "must render summary titles" in {

    val renderSummaryLists = doc.getElementsByClass("summary-text").asScala

    guaranteeReferences.zipWithIndex.map {
      case (reference, index) =>
        renderSummaryLists(index).getElementsByClass("summary-title").text() mustBe s"Guarantee reference ${index + 1}"
        renderSummaryLists(index).getElementsByClass("summary-title-secondary").text() mustBe s"GRN: ${reference.GRN}"

    }

  }

  "must render summary tables" in {

    val renderTables = doc.getElementsByClass("govuk-table").asScala

    guaranteeReferences.zipWithIndex.map {
      case (reference, tableIndex) =>
        renderTables(tableIndex).getElementsByClass("govuk-table__header").asScala.head.text mustBe "Error"
        renderTables(tableIndex).getElementsByClass("govuk-table__header").asScala(1).text mustBe "Further information"

        reference.InvalidGuaranteeReason.zipWithIndex.map {
          case (invalidReason, rowIndex) =>
            val row = renderTables(tableIndex).getElementsByClass("govuk-table__row").asScala.tail(rowIndex)

            row.getElementsByClass("govuk-table__cell").asScala.head.text mustBe invalidReason.code
            row.getElementsByClass("govuk-table__cell").asScala(1).text mustBe invalidReason.text.getOrElse("")

        }
    }

  }
}
