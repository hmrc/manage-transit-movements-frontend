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

package views.arrivalP5

import generators.Generators
import play.twirl.api.HtmlFormat
import viewModels.P5.arrival.ArrivalNotificationWithoutFunctionalErrorP5ViewModel
import viewModels.sections.Section
import views.behaviours.CheckYourAnswersViewBehaviours
import views.html.arrivalP5.ArrivalNotificationWithoutFunctionalErrorsP5View

class ArrivalNotificationWithoutFunctionalErrorsP5ViewSpec extends CheckYourAnswersViewBehaviours with Generators {

  override val prefix: String = "arrival.notification.errors.message"
  val mrnString               = "MRNAB123"

  private val arrivalNotificationErrorP5ViewModel: ArrivalNotificationWithoutFunctionalErrorP5ViewModel =
    new ArrivalNotificationWithoutFunctionalErrorP5ViewModel(mrnString)

  override def viewWithSections(sections: Seq[Section]): HtmlFormat.Appendable =
    injector
      .instanceOf[ArrivalNotificationWithoutFunctionalErrorsP5View]
      .apply(arrivalNotificationErrorP5ViewModel)(fakeRequest, messages, frontendAppConfig)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithCaption(s"MRN: $mrnString")

  behave like pageWithSpecificContent(
    "paragraph-1",
    "There are one or more errors in this notification that cannot be amended. Make a new notification with the right information."
  )

  behave like pageWithSpecificContent(
    "paragraph-2",
    "We will keep your previous answers for 30 days - so if you use the same MRN within this time, your answers will be pre-populated."
  )

  behave like pageWithSpecificContent(
    "create-another-arrival-notification",
    "Make another arrival notification"
  )

  behave like pageWithLink(
    "helpdesk-link",
    "Contact the New Computerised Transit System helpdesk for help understanding the errors (opens in a new tab)",
    frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithLink(
    "arrival-link",
    "Make another arrival notification",
    frontendAppConfig.p5Arrival
  )

}
