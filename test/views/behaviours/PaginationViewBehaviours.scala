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

import org.jsoup.nodes.Document
import org.scalacheck.Gen
import play.twirl.api.HtmlFormat
import play.twirl.api.TwirlHelperImports._
import viewModels.ViewMovement
import viewModels.pagination.PaginationViewModel

// scalastyle:off method.length
// scalastyle:off magic.number
trait PaginationViewBehaviours[T <: ViewMovement] {
  self: MovementsTableViewBehaviours[T] =>

  def viewWithSpecificPagination(paginationViewModel: PaginationViewModel): HtmlFormat.Appendable

  def pageWithPagination(href: String): Unit =
    "page with pagination" - {

      "must display previous button when not on the first page" in {
        val paginationViewModel = PaginationViewModel(4, 2, 2, href)
        val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))

        assertRenderedById(doc, "previous")
        doc.getElementById("previous").attr("href") mustBe s"$href?page=1"
      }

      "must not display previous button when on the first page" in {
        val paginationViewModel = PaginationViewModel(1, 1, 1, href)
        val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))

        assertNotRenderedById(doc, "previous")
      }

      "must display next button when not on the last page" in {
        val paginationViewModel = PaginationViewModel(2, 1, 1, href)
        val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))

        assertRenderedById(doc, "next")
        doc.getElementById("next").attr("href") mustBe s"$href?page=2"
      }

      "must not display next button when on the last page" in {
        val paginationViewModel = PaginationViewModel(2, 2, 1, href)
        val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))

        assertNotRenderedById(doc, "next")
      }

      "must display correct amount of items" in {
        val paginationViewModel = PaginationViewModel(60, 4, 5, href)
        val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))

        assertRenderedById(doc, "pagination-item-1")
        assertNotRenderedById(doc, "pagination-item-2")
        assertRenderedById(doc, "pagination-item-3")
        assertRenderedById(doc, "pagination-item-4")
        assertRenderedById(doc, "pagination-item-5")
        assertNotRenderedById(doc, "pagination-item-6")
        assertRenderedById(doc, "pagination-item-12")
      }

      "must display correct page as active" in {
        val activePage          = 4
        val paginationViewModel = PaginationViewModel(60, activePage, 5, href)
        val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))
        val element             = doc.getElementsByClass("pagination__item--active").first()
        element.text() mustBe s"$activePage"
      }

      "when there's a sufficient number of pages" - {
        "must display ellipses" - {
          "after the first page number" in {
            val paginationViewModel = PaginationViewModel(101, 4, 20, href)
            val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))
            // drop the Previous and Next pagination items so we're just left with the 1, 2, 3 etc.
            val paginationItems = doc.getElementsByClass("pagination__item").toList.drop(1).dropRight(1)
            paginationItems.zip(LazyList from 1).foreach {
              case (paginationItem, index) =>
                if (index == 2) {
                  assert(paginationItem.hasClass("pagination__item--dots"))
                  paginationItem.text() must include("...")
                } else {
                  assert(!paginationItem.hasClass("pagination__item--dots"))
                  paginationItem.text() must include(s"$index")
                }
            }
          }

          "before the final page number" in {
            val paginationViewModel = PaginationViewModel(101, 3, 20, href)
            val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))
            // drop the Previous and Next pagination items so we're just left with the 1, 2, 3 etc.
            val paginationItems = doc.getElementsByClass("pagination__item").toList.drop(1).dropRight(1)
            paginationItems.zip(LazyList from 1).foreach {
              case (paginationItem, index) =>
                if (index == 5) {
                  assert(paginationItem.hasClass("pagination__item--dots"))
                  paginationItem.text() must include("...")
                } else {
                  assert(!paginationItem.hasClass("pagination__item--dots"))
                  paginationItem.text() must include(s"$index")
                }
            }
          }

          "after the first page number and before the final page number" in {
            val paginationViewModel = PaginationViewModel(121, 4, 20, href)
            val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))
            // drop the Previous and Next pagination items so we're just left with the 1, 2, 3 etc.
            val paginationItems = doc.getElementsByClass("pagination__item").toList.drop(1).dropRight(1)
            paginationItems.zip(LazyList from 1).foreach {
              case (paginationItem, index) =>
                if (index == 2 || index == 6) {
                  assert(paginationItem.hasClass("pagination__item--dots"))
                  paginationItem.text() must include("...")
                } else {
                  assert(!paginationItem.hasClass("pagination__item--dots"))
                  paginationItem.text() must include(s"$index")
                }
            }
          }
        }
      }

      "must display correct count" - {

        "when not paginated" - {
          "when only one movement" in {
            val paginationViewModel = PaginationViewModel(1, 1, movementsPerPage, "")
            val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))
            val p                   = doc.getElementById("results-count")
            p.text() mustBe "Showing 1 result"
            boldWords(p) mustBe Seq("1")
          }

          "when multiple movements" in {
            forAll(Gen.choose(2, movementsPerPage)) {
              numberOfMovements =>
                val paginationViewModel = PaginationViewModel(numberOfMovements, 1, movementsPerPage, "")
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
                  val paginationViewModel = PaginationViewModel(numberOfMovements, currentPage, movementsPerPage, "")
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
