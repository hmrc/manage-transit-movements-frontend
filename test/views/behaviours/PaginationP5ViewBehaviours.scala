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
import models.DeparturesSummary
import org.jsoup.nodes.{Document, Element}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.twirl.api.HtmlFormat
import play.twirl.api.TwirlHelperImports._
import viewModels.pagination.DraftsPaginationViewModel

// scalastyle:off method.length
// scalastyle:off magic.number
trait PaginationP5ViewBehaviours[T <: DeparturesSummary] extends ViewBehaviours with ScalaCheckPropertyChecks with Generators {

  val movementsPerPage: Int

  def viewWithSpecificPagination(paginationP5ViewModel: DraftsPaginationViewModel): HtmlFormat.Appendable
  def viewWithSpecificPaginationAndSearch(paginationP5ViewModel: DraftsPaginationViewModel): HtmlFormat.Appendable

  def pageWithPaginationP5(href: String): Unit =
    "page with pagination" - {

      "must display previous button when not on the first page" in {
        val paginationP5ViewModel = DraftsPaginationViewModel(4, 2, 2, href)
        val doc: Document         = parseView(viewWithSpecificPagination(paginationP5ViewModel))

        val element = doc.select("""[rel="prev"]""").headOption
        element.value.attr("href") mustBe s"$href?page=1"
      }

      "must not display previous button when on the first page" in {
        val paginationP5ViewModel = DraftsPaginationViewModel(1, 1, 1, href)
        val doc: Document         = parseView(viewWithSpecificPagination(paginationP5ViewModel))

        val element = doc.select("""[rel="prev"]""").headOption
        element must not be defined
      }

      "must display next button when not on the last page" in {
        val paginationP5ViewModel = DraftsPaginationViewModel(2, 1, 1, href)
        val doc: Document         = parseView(viewWithSpecificPagination(paginationP5ViewModel))

        val element = doc.select("""[rel="next"]""").headOption
        element.value.attr("href") mustBe s"$href?page=2"
      }

      "must not display next button when on the last page" in {
        val paginationP5ViewModel = DraftsPaginationViewModel(2, 2, 1, href)
        val doc: Document         = parseView(viewWithSpecificPagination(paginationP5ViewModel))

        val element = doc.select("""[rel="next"]""").headOption
        element must not be defined
      }

      "must display correct amount of items" in {
        val paginationP5ViewModel = DraftsPaginationViewModel(60, 4, 5, href)
        val doc: Document         = parseView(viewWithSpecificPagination(paginationP5ViewModel))

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
            val paginationP5ViewModel = DraftsPaginationViewModel(1, 1, movementsPerPage, "")
            val doc: Document         = parseView(viewWithSpecificPagination(paginationP5ViewModel))
            val p                     = doc.getElementById("results-count")
            p.text() mustBe "Showing 1 result"
            boldWords(p) mustBe Seq("1")
          }

          "when multiple movements" in {
            forAll(Gen.choose(2, movementsPerPage)) {
              numberOfMovements =>
                val paginationP5ViewModel = DraftsPaginationViewModel(numberOfMovements, 1, movementsPerPage, "")
                val doc: Document         = parseView(viewWithSpecificPagination(paginationP5ViewModel))
                val p                     = doc.getElementById("results-count")
                p.text() mustBe s"Showing $numberOfMovements results"
                boldWords(p) mustBe Seq(numberOfMovements.toString)
            }
          }

          "when only one movement returned on a search" in {

            val paginationP5ViewModel = DraftsPaginationViewModel(1, 1, movementsPerPage, "", lrn = Some(lrn.toString))
            val doc: Document         = parseView(viewWithSpecificPaginationAndSearch(paginationP5ViewModel))
            val p                     = doc.getElementById("results-count")
            p.text() mustBe s"Showing 1 result matching $lrn"
            boldWords(p) mustBe Seq("1")
          }

          "when multiple movements returned on a search" in {
            forAll(Gen.choose(2, movementsPerPage)) {
              numberOfMovements =>
                val paginationP5ViewModel = DraftsPaginationViewModel(numberOfMovements, 1, movementsPerPage, "", lrn = Some(lrn.toString))
                val doc: Document         = parseView(viewWithSpecificPaginationAndSearch(paginationP5ViewModel))
                val p                     = doc.getElementById("results-count")

                p.text() mustBe s"Showing $numberOfMovements results matching $lrn"
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
                  val to                    = currentPage * movementsPerPage
                  val from                  = to - movementsPerPage + 1
                  val paginationP5ViewModel = DraftsPaginationViewModel(numberOfMovements, currentPage, movementsPerPage, "")
                  val doc: Document         = parseView(viewWithSpecificPagination(paginationP5ViewModel))
                  val p                     = doc.getElementById("paginated-results-count")
                  p.text() mustBe s"Showing $from to $to of $numberOfMovements results"
                  boldWords(p) mustBe Seq(from.toString, to.toString, numberOfMovements.toString)
              }
          }
        }

        "when paginated and on a search" in {
          forAll(Gen.choose(2, 10)) {
            numberOfPages =>
              val numberOfMovements = movementsPerPage * numberOfPages
              forAll(Gen.choose(1, numberOfPages)) {
                currentPage =>
                  val to                    = currentPage * movementsPerPage
                  val from                  = to - movementsPerPage + 1
                  val paginationP5ViewModel = DraftsPaginationViewModel(numberOfMovements, currentPage, movementsPerPage, "", lrn = Some(lrn.toString))
                  val doc: Document         = parseView(viewWithSpecificPagination(paginationP5ViewModel))
                  val p                     = doc.getElementById("paginated-results-count")
                  p.text() mustBe s"Showing $from to $to of $numberOfMovements results matching $lrn"
                  boldWords(p) mustBe Seq(from.toString, to.toString, numberOfMovements.toString)
              }
          }
        }
      }
    }

  protected def boldWords(p: Element): Seq[String] = p.getElementsByTag("b").toList.map(_.text())

}
// scalastyle:on method.length
// scalastyle:on magic.number
