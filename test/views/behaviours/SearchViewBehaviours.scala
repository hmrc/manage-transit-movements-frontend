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

package views.behaviours

import forms.SearchFormProvider
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.ViewMovement

trait SearchViewBehaviours[T <: ViewMovement] extends InputTextViewBehaviours[String] {
  self: MovementsTableViewBehaviours[T] =>

  override def form: Form[String] = new SearchFormProvider()()

  val dataRows: Seq[(String, Seq[T])]

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaNumStr)

  def viewWithSpecificSearchResults(dataRows: Seq[(String, Seq[T])], retrieved: Int, tooManyResults: Boolean): HtmlFormat.Appendable =
    view

  "must contain movement-search div" in {
    assert(doc.getElementsByClass("movement-search").size() == 1)
  }

  def pageWithMovementSearch(expectedLabelText: String): Unit =
    "page with a movements search box" - {
      behave like pageWithInputText()

      behave like pageWithSubmitButton("Search")
    }

  def pageWithSearchResults(referenceNumber: String): Unit =
    "page with search results" - {
      "must display correct text" - {
        "when there are no results" in {
          val doc = parseView(viewWithSpecificSearchResults(Nil, 0, tooManyResults = false))
          val p   = doc.getElementById("no-results-found")
          p.text() mustBe "No results found"
        }

        "when there is a single result" in {
          val doc = parseView(viewWithSpecificSearchResults(dataRows, 1, tooManyResults = false))
          val p   = doc.getElementById("results-found")
          p.text() mustBe s"Showing 1 result matching $referenceNumber."
          boldWords(p) mustBe Seq("1")
        }

        "when there are multiple results" in {
          forAll(arbitrary[Int].retryUntil(_ > 1)) {
            retrieved =>
              val doc = parseView(viewWithSpecificSearchResults(dataRows, retrieved, tooManyResults = false))
              val p   = doc.getElementById("results-found")
              p.text() mustBe s"Showing $retrieved results matching $referenceNumber."
              boldWords(p) mustBe Seq(retrieved.toString)
          }
        }

        "when there are too many results" in {
          forAll(arbitrary[Int].retryUntil(_ > 1)) {
            retrieved =>
              val doc = parseView(viewWithSpecificSearchResults(dataRows, retrieved, tooManyResults = true))
              val p   = doc.getElementById("results-found")
              p.text() mustBe s"Showing $retrieved results matching $referenceNumber. There are too many results. Please refine your search."
              boldWords(p) mustBe Seq(retrieved.toString)
          }
        }
      }
    }

}
