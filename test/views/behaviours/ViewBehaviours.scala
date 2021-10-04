/*
 * Copyright 2021 HM Revenue & Customs
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

import base.SingleViewSpec
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.json.{JsObject, Json}
import play.twirl.api.Html
import viewModels.pagination.PaginationViewModel

abstract class ViewBehaviours(override protected val viewUnderTest: String) extends SingleViewSpec(viewUnderTest) {

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  def pageWithHeading(doc: Document, messageKeyPrefix: String): Unit =
    "display page heading" in {
      assertPageHasHeading(doc, s"$messageKeyPrefix.heading")
    }

  def pageWithLink(doc: Document, id: String, expectedText: String, expectedHref: String): Unit =
    s"display link with id $id" in {
      assertPageHasLink(doc, id, expectedText, expectedHref)
    }

  // scalastyle:off method.length
  def pageWithPagination(href: String): Unit =
    "Pagination" - {

      "must display pagination results when there is more than one page" in {

        val json: JsObject = Json.toJsObject(PaginationViewModel(4, 2, 2, href))

        val doc: Document = renderDocument(json).futureValue

        doc.getElementById("paginated-results-count").text mustBe "Showing 3 to 4 of 4 results"
      }

      "must display previous button when not on the first page" in {

        val json: JsObject = Json.toJsObject(PaginationViewModel(4, 2, 2, href))

        val doc: Document = renderDocument(json).futureValue

        assertRenderedById(doc, "previous")
        doc.getElementById("previous").attr("href") mustBe s"$href?page=1"
      }

      "must not display previous button when on the first page" in {

        val json: JsObject = Json.toJsObject(PaginationViewModel(1, 1, 1, href))

        val doc: Document = renderDocument(json).futureValue

        assertNotRenderedById(doc, "previous")
      }

      "must display next button when not on the last page" in {

        val json: JsObject = Json.toJsObject(PaginationViewModel(2, 1, 1, href))

        val doc: Document = renderDocument(json).futureValue

        assertRenderedById(doc, "next")
        doc.getElementById("next").attr("href") mustBe s"$href?page=2"
      }

      "must not display next button when on the last page" in {

        val json: JsObject = Json.toJsObject(PaginationViewModel(2, 2, 1, href))

        val doc: Document = renderDocument(json).futureValue

        assertNotRenderedById(doc, "next")
      }

      "must display correct amount of items" in {

        val json: JsObject = Json.toJsObject(PaginationViewModel(60, 4, 5, href))

        val doc: Document = renderDocument(json).futureValue

        assertRenderedById(doc, "pagination-item-1")
        assertNotRenderedById(doc, "pagination-item-2")
        assertRenderedById(doc, "pagination-item-3")
        assertRenderedById(doc, "pagination-item-4")
        assertRenderedById(doc, "pagination-item-5")
        assertNotRenderedById(doc, "pagination-item-6")
        assertRenderedById(doc, "pagination-item-12")
      }
    }
  // scalastyle:on method.length

  def pageWithMovementSearch(doc: Document, id: String, expectedText: String): Unit =
    "displays search box" - {
      s"display search box $id" in {
        assertRenderedById(doc, id)
      }

      "contain a label for the search" in {
        assertContainsLabel(doc, id, expectedText, None)
      }

      "have a submit button" in {
        assertRenderedById(doc, "submit")
      }
    }
}
