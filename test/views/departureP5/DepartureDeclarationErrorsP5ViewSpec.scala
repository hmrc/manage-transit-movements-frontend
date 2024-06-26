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
import play.twirl.api.HtmlFormat
import viewModels.P5.departure.DepartureDeclarationErrorsP5ViewModel
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.departureP5.DepartureDeclarationErrorsP5View

class DepartureDeclarationErrorsP5ViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  override val prefix: String = "departure.declaration.errors.message"
  val lrnString               = "LRNAB123"

  private val departureDeclarationErrorsP5ViewModel: DepartureDeclarationErrorsP5ViewModel =
    new DepartureDeclarationErrorsP5ViewModel(lrnString, isAmendmentJourney = false)

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector
      .instanceOf[DepartureDeclarationErrorsP5View]
      .apply(departureDeclarationErrorsP5ViewModel, isAmendmentJourney = false, None)(fakeRequest, messages, frontendAppConfig)

  def viewWithSpecificAmendment(isAmendmentJourney: Boolean, mrn: Option[String] = None): HtmlFormat.Appendable =
    injector
      .instanceOf[DepartureDeclarationErrorsP5View]
      .apply(departureDeclarationErrorsP5ViewModel, isAmendmentJourney, mrn)(fakeRequest, messages, frontendAppConfig)

  behave like pageWithTitle()

  behave like pageWithCaption(s"LRN: $lrnString")

  behave like pageWithBackLink()

  behave like pageWithHeading()

  private def assertSpecificElementContainsText(id: String, expectedText: String): Unit = {
    val element = doc.getElementById(id)
    assertElementContainsText(element, expectedText)
  }

  "must render correct paragraph1 content" in {
    assertSpecificElementContainsText(
      "paragraph-1",
      s"There are one or more errors in this declaration that cannot be amended. Make a new declaration with the right information."
    )
  }

  "must render correct paragraph2 content" in {
    assertSpecificElementContainsText(
      "helpdesk",
      "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab)."
    )
    assertSpecificElementContainsText(
      "helpdesk-link",
      "New Computerised Transit System helpdesk"
    )
  }

  "must render correct link text" in {
    assertSpecificElementContainsText("create-another-declaration", "Make another departure declaration")
  }

  behave like pageWithLink(
    "helpdesk-link",
    "New Computerised Transit System helpdesk",
    frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithLink(
    "departure-link",
    "Make another departure declaration",
    frontendAppConfig.p5Departure
  )

  "must not render add another declaration link when isAmendmentJourney is true" in {
    val doc: Document = parseView(viewWithSpecificAmendment(isAmendmentJourney = true))
    assertNotRenderedById(doc, "create-another-declaration")
  }

  "must not render mrn when None" in {
    val doc: Document = parseView(viewWithSpecificAmendment(isAmendmentJourney = true, None))
    assertNotRenderedById(doc, "mrn")
  }

  "must render mrn when provided" in {
    val doc: Document = parseView(viewWithSpecificAmendment(isAmendmentJourney = true, Some("mrn")))
    assertRenderedById(doc, "mrn")
  }

}
