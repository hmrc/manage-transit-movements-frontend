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

package views.arrival.testOnly

import generators.Generators
import play.twirl.api.HtmlFormat
import viewModels.P5.arrival.ArrivalNotificationErrorP5ViewModel
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.arrival.TestOnly.ArrivalNotificationErrorP5View

class ArrivalNotificationErrorP5ViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  override val prefix: String = "arrival.notification.errors.message"
  val mrnString               = "MRNAB123"

  private val arrivalNotificationErrorP5ViewModel: ArrivalNotificationErrorP5ViewModel = new ArrivalNotificationErrorP5ViewModel(mrnString, true)

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector
      .instanceOf[ArrivalNotificationErrorP5View]
      .apply(arrivalNotificationErrorP5ViewModel)(fakeRequest, messages, frontendAppConfig)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithCaption(s"MRN: $mrnString")

  private def assertSpecificElementContainsText(id: String, expectedText: String): Unit = {
    val element = doc.getElementById(id)
    assertElementContainsText(element, expectedText)
  }

  "must render correct paragraph2 content" in {
    assertSpecificElementContainsText(
      "helpdesk-link",
      "New Computerised Transit System helpdesk"
    )
  }

  "must render correct link text" in {
    assertSpecificElementContainsText("create-another-arrival-notification", "Create another arrival notification")
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
