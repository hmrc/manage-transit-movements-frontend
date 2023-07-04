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

package views.arrival.P5

import generators.Generators
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewModels.P5.arrival.ArrivalNotificationWithFunctionalErrorsP5ViewModel
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.arrival.P5.ArrivalNotificationWithFunctionalErrorsP5View

class ArrivalNotificationWithFunctionalErrorsP5ViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  override val prefix: String = "arrival.ie057.review.notification.message"

  private val arrivalNotificationWithFunctionalErrorsP5ViewModel: ArrivalNotificationWithFunctionalErrorsP5ViewModel =
    new ArrivalNotificationWithFunctionalErrorsP5ViewModel(sections, lrn.toString, false)

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector
      .instanceOf[ArrivalNotificationWithFunctionalErrorsP5View]
      .apply(arrivalNotificationWithFunctionalErrorsP5ViewModel, departureIdP5)(fakeRequest, messages, frontendAppConfig)

  override def summaryLists: Seq[SummaryList] = sections.map(
    section => SummaryList(section.rows)
  )

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithSummaryLists()

  behave like pageWithoutFormAction()

  behave like pageWithoutSubmitButton()

  "must render section titles when rows are non-empty" - {
    sections.foreach(_.sectionTitle.map {
      sectionTitle =>
        behave like pageWithContent("h2", sectionTitle)
    })
  }

  private def assertSpecificElementContainsText(id: String, expectedText: String): Unit = {
    val element = doc.getElementById(id)
    assertElementContainsText(element, expectedText)
  }

  "must render correct paragraph1 content" in {
    assertSpecificElementContainsText(
      "paragraph-1-prefix",
      s"There is a problem with arrival notification $mrn."
    )
    assertSpecificElementContainsText(
      "paragraph-1-suffix",
      "Review the error and make/create a new arrival notification with the right information."
    )
  }

  "must render correct paragraph2 content" in {
    assertSpecificElementContainsText(
      "paragraph-2",
      "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab)."
    )
    assertSpecificElementContainsText(
      "helpdesk-link",
      "New Computerised Transit System helpdesk"
    )

  }

  behave like pageWithLink(
    "helpdesk-link",
    "New Computerised Transit System helpdesk",
    frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithLink(
    "arrival-link",
    "Create another arrival notification",
    frontendAppConfig.declareArrivalNotificationStartUrl
  )

}
