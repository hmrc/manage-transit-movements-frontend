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

import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.twirl.api.HtmlFormat

trait SearchViewBehaviours extends InputTextViewBehaviours[String] with ScalaCheckPropertyChecks {

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaNumStr)

  def viewWithSpecificSearchResults(numberOfSearchResults: Int, currentPage: Int, numberOfItemsPerPage: Int, searchParam: String): HtmlFormat.Appendable

  "must contain search div" in {
    assertRenderedById(doc, "search")
  }

  def pageWithSearch(expectedLabelText: String, expectednoResultsFound: String, numberOfItemsPerPage: Int): Unit = {
    "page with a search box" - {
      behave like pageWithContent("label", expectedLabelText)

      behave like pageWithInputText()

      behave like pageWithSubmitButton("Search")
    }

    val searchParam = nonEmptyString.sample.value

    "page with search results" - {
      "must display correct text" - {
        "when there are no results" in {
          val doc = parseView(viewWithSpecificSearchResults(0, 1, 20, searchParam))
          val p   = doc.getElementById("results-count")
          p.text() `mustBe` expectednoResultsFound
        }

        "when there is a single result" in {
          val doc = parseView(viewWithSpecificSearchResults(1, 1, 20, searchParam))
          val p   = doc.getElementById("results-count")
          p.text() `mustBe` s"Showing 1 result matching $searchParam"
          boldWords(p) `mustBe` Seq("1")
        }

        "when there are multiple results" in {
          forAll(Gen.choose(2, numberOfItemsPerPage)) {
            retrieved =>
              val doc = parseView(viewWithSpecificSearchResults(retrieved, 1, numberOfItemsPerPage, searchParam))
              val p   = doc.getElementById("results-count")
              p.text() `mustBe` s"Showing $retrieved results matching $searchParam"
              boldWords(p) `mustBe` Seq(retrieved.toString)
          }
        }
      }
    }
  }
}
