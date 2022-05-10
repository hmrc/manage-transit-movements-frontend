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

package views.behaviours

import generators.Generators
import models.{ErrorPointer, ErrorType, FunctionalError}
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat

trait NegativeAcknowledgementViewBehaviours extends ViewBehaviours with Generators {

  def applyView(functionalError: FunctionalError): HtmlFormat.Appendable

  override def view: HtmlFormat.Appendable = applyView(arbitrary[FunctionalError].sample.value)

  def viewWithFullyPopulatedFunctionalError: HtmlFormat.Appendable = applyView(fullyPopulatedFunctionalError)

  def viewWithPartiallyPopulatedFunctionalError: HtmlFormat.Appendable = applyView(partiallyPopulatedFunctionalError)

  private val errorType: ErrorType                       = arbitrary[ErrorType].sample.value
  val fullyPopulatedFunctionalError: FunctionalError     = FunctionalError(errorType, ErrorPointer("Message type"), Some("Error Reason"), Some("GB007A"))
  val partiallyPopulatedFunctionalError: FunctionalError = FunctionalError(errorType, ErrorPointer("Message type"), None, None)

  def pageWithNegativeAcknowledgement(): Unit = {
    "populate the error details when we have a fully populated function error" - {
      val doc = parseView(viewWithFullyPopulatedFunctionalError)

      behave like pageWithContent(doc, "dt", "Error type")
      behave like pageWithContent(doc, "dd", fullyPopulatedFunctionalError.errorType.toString)

      behave like pageWithContent(doc, "dt", "Error pointer")
      behave like pageWithContent(doc, "dd", fullyPopulatedFunctionalError.pointer.value)

      behave like pageWithContent(doc, "dt", "Error reason")
      behave like pageWithContent(doc, "dd", fullyPopulatedFunctionalError.reason.get)

      behave like pageWithContent(doc, "dt", "Original attribute value")
      behave like pageWithContent(doc, "dd", fullyPopulatedFunctionalError.originalAttributeValue.get)
    }

    "populate the error details when we have a partially populated function error" - {
      val doc = parseView(viewWithPartiallyPopulatedFunctionalError)

      behave like pageWithContent(doc, "dt", "Error type")
      behave like pageWithContent(doc, "dd", partiallyPopulatedFunctionalError.errorType.toString)

      behave like pageWithContent(doc, "dt", "Error pointer")
      behave like pageWithContent(doc, "dd", partiallyPopulatedFunctionalError.pointer.value)

      behave like pageWithoutContent(doc, "dt", "Error reason")

      behave like pageWithoutContent(doc, "dt", "Original attribute value")
    }
  }

}
