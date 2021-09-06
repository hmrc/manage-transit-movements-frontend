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
import org.jsoup.nodes.Document
import play.api.libs.json.JsObject
import play.api.mvc.Call
import viewModels.PaginationViewModel

abstract class ViewBehaviours(override protected val viewUnderTest: String) extends SingleViewSpec(viewUnderTest) {

  def pageWithHeading(doc: Document, messageKeyPrefix: String): Unit =
    "display page heading" in {
      assertPageHasHeading(doc, s"$messageKeyPrefix.heading")
    }

  def pageWithLink(doc: Document, id: String, expectedText: String, expectedHref: String): Unit =
    s"display link with id $id" in {
      assertPageHasLink(doc, id, expectedText, expectedHref)
    }

  def pageWithPagination(expectedCall: Option[Int] => Call): Unit =
    "Pagination" - {

      "must display pagination results when there is more than one page" in {

        val json: JsObject = PaginationViewModel(4, 2, 2, expectedCall)

        val doc: Document = renderDocument(json).futureValue

        doc.getElementById("paginated-results-count").text mustBe "Showing 3 to 4 of 4 results"
      }

      "must display results when there is only one page" in {

        val json: JsObject = PaginationViewModel(1, 1, 1, expectedCall)

        val doc: Document = renderDocument(json).futureValue

        doc.getElementById("results-count").text mustBe "Showing 1 results" //TODO account for singular or plural
      }

      "must display previous button when not on the first page" in {

        val json: JsObject = PaginationViewModel(4, 2, 2, expectedCall)

        val doc: Document = renderDocument(json).futureValue

        assertRenderedById(doc, "previous")
        doc.getElementById("previous").attr("href") mustBe expectedCall(Some(1)).url
      }

      "must not display previous button when on the first page" in {

        val json: JsObject = PaginationViewModel(1, 1, 1, expectedCall)

        val doc: Document = renderDocument(json).futureValue

        assertNotRenderedById(doc, "previous")
      }

      "must display next button when not on the last page" in {

        val json: JsObject = PaginationViewModel(2, 1, 1, expectedCall)

        val doc: Document = renderDocument(json).futureValue

        assertRenderedById(doc, "next")
        doc.getElementById("next").attr("href") mustBe expectedCall(Some(2)).url
      }

      "must not display next button when on the last page" in {

        val json: JsObject = PaginationViewModel(2, 2, 1, expectedCall)

        val doc: Document = renderDocument(json).futureValue

        assertNotRenderedById(doc, "next")
      }

      "must display correct amount of items" in {

        val json: JsObject = PaginationViewModel(60, 4, 5, expectedCall)

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
}
