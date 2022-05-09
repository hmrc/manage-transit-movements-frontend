/*
 * Copyright 2022 HM Revenue & Customs
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

package views

import generators.Generators
import models.{ErrorPointer, ErrorType, FunctionalError}
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.ArrivalXmlNegativeAcknowledgementView

class ArrivalXmlNegativeAcknowledgementViewSpec extends ViewBehaviours with Generators {

  private def applyView(functionalError: FunctionalError): HtmlFormat.Appendable =
    injector.instanceOf[ArrivalXmlNegativeAcknowledgementView].apply(functionalError)(fakeRequest, messages)
  val genRejectionError = arbitrary[ErrorType].sample.value

  private val fullFunctionalError: FunctionalError    = FunctionalError(genRejectionError, ErrorPointer("Message type"), Some("Error Reason"), Some("GB007A"))
  private val partialFunctionalError: FunctionalError = FunctionalError(genRejectionError, ErrorPointer("Message type"), None, None)

  override def view: HtmlFormat.Appendable = applyView(fullFunctionalError)

  override val prefix: String = "arrivalXmlNegativeAcknowledgement"

  behave like pageWithTitle()

  behave like pageWithBackLink

  behave like pageWithHeading()

  "populate the error details when we have a fully populated function error" - {
    val docWithFullError = parseView(applyView(fullFunctionalError))

    behave like pageWithContent(docWithFullError, "dt", "Error type")
    behave like pageWithContent(docWithFullError, "dd", fullFunctionalError.errorType.toString)

    behave like pageWithContent(docWithFullError, "dt", "Error pointer")
    behave like pageWithContent(docWithFullError, "dd", fullFunctionalError.pointer.value)

    behave like pageWithContent(docWithFullError, "dt", "Error reason")
    behave like pageWithContent(docWithFullError, "dd", fullFunctionalError.reason.getOrElse(""))

    behave like pageWithContent(docWithFullError, "dt", "Original attribute value")
    behave like pageWithContent(docWithFullError, "dd", fullFunctionalError.originalAttributeValue.getOrElse(""))
  }

  "populate the error details when we have a partially populated function error" - {
    val docWithPartialError = parseView(applyView(partialFunctionalError))

    behave like pageWithContent(docWithPartialError, "dt", "Error type")
    behave like pageWithContent(docWithPartialError, "dd", partialFunctionalError.errorType.toString)

    behave like pageWithContent(docWithPartialError, "dt", "Error pointer")
    behave like pageWithContent(docWithPartialError, "dd", partialFunctionalError.pointer.value)

    behave like pageWithoutContent(docWithPartialError, "dt", "Error reason")

    behave like pageWithoutContent(docWithPartialError, "dt", "Original attribute value")
  }

  behave like pageWithPartialContent("p", "You must")
  behave like pageWithLink(
    "contact",
    "contact the New Computerised Transit System helpdesk (opens in a new tab)",
    "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/new-computerised-transit-system-enquiries"
  )
}
