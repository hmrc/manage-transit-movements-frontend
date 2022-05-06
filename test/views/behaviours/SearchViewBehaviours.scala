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

import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import viewModels.ViewMovement

trait SearchViewBehaviours[T <: ViewMovement] {
  self: MovementsTableViewBehaviours[T] =>

  val dataRows: Seq[(String, Seq[T])]

  def viewWithSpecificSearchResults(dataRows: Seq[(String, Seq[T])], retrieved: Int, tooManyResults: Boolean): HtmlFormat.Appendable =
    view

  def pageWithMovementSearch(expectedLabelText: String): Unit =
    "page with a movements search box" - {
      s"must display a search box for $referenceNumberType" in {
        assertRenderedById(doc, referenceNumberType)
      }

      "must contain a label for the search" in {
        assertContainsLabel(doc, referenceNumberType, expectedLabelText)
      }

      behave like pageWithSubmitButton("Search")
    }

  def pageWithSearchResults(): Unit =
    "page with search results" - {
      "must display correct text" - {
        "when there are no results" in {
          val doc = parseView(viewWithSpecificSearchResults(Nil, 0, tooManyResults = false))
          val p   = doc.getElementById("no-results-found")
          p.text() mustBe "No results found"
        }

        "when there are results" in {
          forAll(arbitrary[Int]) {
            retrieved =>
              val doc = parseView(viewWithSpecificSearchResults(dataRows, retrieved, tooManyResults = false))
              val p   = doc.getElementById("results-found")
              p.text() mustBe s"Showing $retrieved results matching $mrn."
              boldWords(p) mustBe Seq(retrieved.toString)
          }
        }

        "when there are too many results" in {
          forAll(arbitrary[Int]) {
            retrieved =>
              val doc = parseView(viewWithSpecificSearchResults(dataRows, retrieved, tooManyResults = true))
              val p   = doc.getElementById("results-found")
              p.text() mustBe s"Showing $retrieved results matching $mrn. There are too many results. Please refine your search."
              boldWords(p) mustBe Seq(retrieved.toString)
          }
        }
      }
    }

}
