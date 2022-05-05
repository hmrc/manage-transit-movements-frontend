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

import config.PaginationAppConfig
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json._
import play.twirl.api.HtmlFormat
import viewModels.ViewMovement
import viewModels.pagination.PaginationViewModel

import scala.collection.convert.ImplicitConversions._

trait MovementsTableViewBehaviours[T <: ViewMovement] extends ViewBehaviours with ScalaCheckPropertyChecks {

  def paginationAppConfig: PaginationAppConfig = injector.instanceOf[PaginationAppConfig]

  val viewMovements: Seq[T]

  val referenceNumberType: String

  val movementsPerPage: Int

  def viewWithSpecificPagination(paginationViewModel: PaginationViewModel): HtmlFormat.Appendable

  // scalastyle:off method.length
  // scalastyle:off magic.number
  def pageWithMovementsData()(implicit wts: Writes[T]): Unit =
    "page with a movements data table" - {

      "must generate a heading for each unique day" in {
        val elements: Elements = doc.getElementsByAttributeValue("data-testrole", "movements-list_group-heading")

        // regex for date formatted as dd MMMM yyyy
        val dateRegex = "^(([0-9])|([0-2][0-9])|([3][0-1])) (January|February|March|April|May|June|July|August|September|October|November|December) \\d{4}$"

        elements.forEach {
          element =>
            element.text().matches(dateRegex) mustBe true
        }
      }

      val rows: Elements = doc.select("tr[data-testrole^=movements-list_row]")

      "must generate a row for each movement" in {
        rows.size() mustEqual viewMovements.size
      }

      "must generate correct data in each row" - {
        rows.toList.zipWithIndex.forEach {
          case (row, rowIndex) =>
            s"when row ${rowIndex + 1}" - {

              def elementWithVisibleText(element: Element, text: String): Unit =
                element.ownText() mustBe text.trim

              def elementWithHiddenText(element: Element, text: String): Unit = {
                val heading = element.getElementsByClass("responsive-table__heading").head
                heading.attr("aria-hidden").toBoolean mustBe true
                heading.text() mustBe text
              }

              "must display correct time" in {
                val updated = row.selectFirst("td[data-testrole*=-updated]")
                val time    = Json.toJson(viewMovements(rowIndex)).transform((JsPath \ "updated").json.pick[JsString]).get.value

                behave like elementWithVisibleText(updated, time)
                behave like elementWithHiddenText(updated, messages(s"$prefix.table.updated"))
              }

              "must display correct reference number" in {
                val ref = row.selectFirst("td[data-testrole*=-ref]")

                behave like elementWithVisibleText(ref, viewMovements(rowIndex).referenceNumber)
                behave like elementWithHiddenText(ref, messages(s"$prefix.table.$referenceNumberType"))
              }

              "must display correct status" in {
                val status = row.selectFirst("td[data-testrole*=-status]")

                behave like elementWithVisibleText(status, viewMovements(rowIndex).status)
                behave like elementWithHiddenText(status, messages(s"$prefix.table.status"))
              }

              "must display actions" - {
                val actions = row.selectFirst("td[data-testrole*=-actions]")

                "must include hidden content" in {
                  behave like elementWithHiddenText(actions, messages(s"$prefix.table.action"))
                }

                val actionLinks = actions.getElementsByClass("govuk-link")
                actionLinks.zipWithIndex.forEach {
                  case (link, linkIndex) =>
                    s"when action ${linkIndex + 1}" - {

                      "must display correct text" in {
                        link.text() mustBe s"${viewMovements(rowIndex).actions(linkIndex).key} for ${viewMovements(rowIndex).referenceNumber}"

                        behave like elementWithVisibleText(link, s"${viewMovements(rowIndex).actions(linkIndex).key}")

                        val hiddenText = link.getElementsByClass("govuk-visually-hidden").head
                        hiddenText.text() mustBe s"for ${viewMovements(rowIndex).referenceNumber}"
                      }

                      "must have correct id" in {
                        link.attr("id") mustBe s"${viewMovements(rowIndex).actions(linkIndex).key}-${viewMovements(rowIndex).referenceNumber}"
                      }

                      "must have correct href" in {
                        link.attr("href") mustBe viewMovements(rowIndex).actions(linkIndex).href
                      }
                    }
                }
              }
            }
        }
      }
    }

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
    }

  def pageWithMovementSearch(): Unit =
    "page with a movements search box" - {
      s"must display a search box for $referenceNumberType" in {
        assertRenderedById(doc, referenceNumberType)
      }

      "must contain a label for the search" in {
        assertContainsLabel(doc, referenceNumberType, "Search by movement reference number")
      }

      behave like pageWithSubmitButton("Search")

      "must display correct count" - {
        "when not paginated" - {
          "when only one movement" in {
            val paginationViewModel = PaginationViewModel(1, 1, movementsPerPage, "")
            val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))
            val p                   = doc.getElementById("results-count")
            p.text() mustBe "Showing 1 result"
          }

          "when multiple movements" in {
            forAll(Gen.choose(2, movementsPerPage)) {
              numberOfMovements =>
                val paginationViewModel = PaginationViewModel(numberOfMovements, 1, movementsPerPage, "")
                val doc: Document       = parseView(viewWithSpecificPagination(paginationViewModel))
                val p                   = doc.getElementById("results-count")
                p.text() mustBe s"Showing $numberOfMovements results"
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
              }
          }
        }
      }
    }
  // scalastyle:on method.length
  // scalastyle:on magic.number

}
