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

import base.SpecBase
import org.jsoup.nodes.Document

trait ViewBehaviours extends SpecBase {

  def pageWithHeading(doc: Document, messageKeyPrefix: String): Unit =
    "display page heading" in {
      doc.selectFirst("h1").text() mustBe s"$messageKeyPrefix.heading"
    }

  def pageWithLink(doc: Document, id: String, expectedText: String, expectedHref: String): Unit =
    s"display link with id $id" in {
      val link = doc.selectFirst(s"a[id=$id]")
      link.text() mustBe expectedText
      link.attr("href") mustBe expectedHref
    }

}
