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
import models.departureP5.BusinessRejectionType
import models.departureP5.BusinessRejectionType.DepartureBusinessRejectionType
import org.jsoup.nodes.Document
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.twirl.api.HtmlFormat
import viewModels.P5.departure.DepartureDeclarationErrorsP5ViewModel
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.departureP5.DepartureDeclarationErrorsP5View

class DepartureDeclarationErrorsP5ViewSpec extends CheckYourAnswersViewBehaviours with ScalaCheckPropertyChecks with Generators {

  override val prefix: String = "departure.declaration.errors.message"

  private val lrnString = nonEmptyString.sample.value

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    buildView(None, BusinessRejectionType.DeclarationRejection)

  private def buildView(mrn: Option[String], businessRejectionType: DepartureBusinessRejectionType): HtmlFormat.Appendable = {
    val departureDeclarationErrorsP5ViewModel: DepartureDeclarationErrorsP5ViewModel =
      new DepartureDeclarationErrorsP5ViewModel(lrnString, mrn, businessRejectionType)

    injector
      .instanceOf[DepartureDeclarationErrorsP5View]
      .apply(departureDeclarationErrorsP5ViewModel)(fakeRequest, messages, frontendAppConfig)
  }

  behave like pageWithTitle()

  behave like pageWithCaption(s"LRN: $lrnString")

  behave like pageWithBackLink()

  behave like pageWithHeading()

  "must render correct paragraph1 content" in {
    assertElementWithIdContainsText(
      doc,
      "paragraph-1",
      s"There are one or more errors in this declaration that cannot be amended. Make a new declaration with the right information."
    )
  }

  "must render correct paragraph2 content" in {
    assertElementWithIdContainsText(
      doc,
      "helpdesk",
      "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab)."
    )
    assertElementWithIdContainsText(
      doc,
      "helpdesk-link",
      "New Computerised Transit System helpdesk"
    )
  }

  "must render correct link text" in {
    assertElementWithIdContainsText(doc, "create-another-declaration", "Make another departure declaration")
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
    forAll(Gen.option(nonEmptyString)) {
      mrn =>
        val doc: Document = parseView(buildView(mrn, BusinessRejectionType.AmendmentRejection))
        assertNotRenderedById(doc, "create-another-declaration")
    }
  }

  "must not render mrn when None" in {
    forAll(arbitrary[DepartureBusinessRejectionType]) {
      businessRejectionType =>
        val doc: Document = parseView(buildView(None, businessRejectionType))
        assertNotRenderedById(doc, "mrn")
    }
  }

  "must render mrn when provided" in {
    forAll(nonEmptyString, arbitrary[DepartureBusinessRejectionType]) {
      (mrn, businessRejectionType) =>
        val doc: Document = parseView(buildView(Some(mrn), businessRejectionType))
        assertElementWithIdContainsText(doc, "mrn", s"MRN: $mrn")
    }
  }

}
