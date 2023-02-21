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

import generators.Generators
import org.jsoup.nodes.Document
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.twirl.api.HtmlFormat
import play.twirl.api.TwirlHelperImports._
import viewModels.pagination.PaginationViewModel

// scalastyle:off method.length
// scalastyle:off magic.number
trait PaginationViewBehaviours[T <: PaginationViewModel] extends ViewBehaviours with ScalaCheckPropertyChecks with Generators {

  val movementsPerPage: Int

  val buildViewModel: (Int, Int, Int, String) => T

  def viewWithSpecificPagination(paginationViewModel: T): HtmlFormat.Appendable

  def pageWithPagination(href: String): Unit =
    "page with pagination" - {

      "must display previous button when not on the first page" in {
        val paginationViewModel = buildViewModel(4, 2, 2, href)
        val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))

        val element = doc.select("""[rel="prev"]""").headOption
        element.value.attr("href") mustBe s"$href?page=1"
      }

      "must not display previous button when on the first page" in {
        val paginationViewModel = buildViewModel(1, 1, 1, href)
        val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))

        val element = doc.select("""[rel="prev"]""").headOption
        element must not be defined
      }

      "must display next button when not on the last page" in {
        val paginationViewModel = buildViewModel(2, 1, 1, href)
        val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))

        val element = doc.select("""[rel="next"]""").headOption
        element.value.attr("href") mustBe s"$href?page=2"
      }

      "must not display next button when on the last page" in {
        val paginationViewModel = buildViewModel(2, 2, 1, href)
        val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))

        val element = doc.select("""[rel="next"]""").headOption
        element must not be defined
      }

      "must display correct amount of items" in {
        val paginationViewModel = buildViewModel(60, 4, 5, href)
        val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))

        // should look like 1 … 3 [4] 5 … 12

        val current = doc.getElementsByClass("govuk-pagination__item--current").head
        current.getElementsByClass("govuk-pagination__link").text() mustBe "4"

        val items = doc.getElementsByClass("govuk-pagination__item").toList
        items.length mustBe 7
        items.head.getElementsByClass("govuk-pagination__link").text() mustBe "1"
        items(1).text() mustBe "⋯"
        items(2).getElementsByClass("govuk-pagination__link").text() mustBe "3"
        items(3).getElementsByClass("govuk-pagination__link").text() mustBe "4"
        items(4).getElementsByClass("govuk-pagination__link").text() mustBe "5"
        items(5).text() mustBe "⋯"
        items(6).getElementsByClass("govuk-pagination__link").text() mustBe "12"
      }

      "must display correct count" - {

        "when not paginated" - {
          "when only one movement" in {
            val paginationViewModel = buildViewModel(1, 1, movementsPerPage, "")
            val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))
            val p                   = doc.getElementById("results-count")
            p.text() mustBe "Showing 1 result"
            boldWords(p) mustBe Seq("1")
          }

          "when multiple movements" in {
            forAll(Gen.choose(2, movementsPerPage)) {
              numberOfMovements =>
                val paginationViewModel = buildViewModel(numberOfMovements, 1, movementsPerPage, "")
                val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))
                val p                   = doc.getElementById("results-count")
                p.text() mustBe s"Showing $numberOfMovements results"
                boldWords(p) mustBe Seq(numberOfMovements.toString)
            }
          }
        }

        "when paginated" in {
          forAll(Gen.choose(2, 10)) {
            numberOfPages =>
              val numberOfMovements = movementsPerPage * numberOfPages
              forAll(Gen.choose(1, numberOfPages)) {
                currentPage =>
                  val to                  = currentPage * movementsPerPage
                  val from                = to - movementsPerPage + 1
                  val paginationViewModel = buildViewModel(numberOfMovements, currentPage, movementsPerPage, "")
                  val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))
                  val p                   = doc.getElementById("paginated-results-count")
                  p.text() mustBe s"Showing $from to $to of $numberOfMovements results"
                  boldWords(p) mustBe Seq(from.toString, to.toString, numberOfMovements.toString)
              }
          }
        }
      }
    }

}
// scalastyle:on method.length
// scalastyle:on magic.number
