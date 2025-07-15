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

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import play.twirl.api.TwirlHelperImports.*
import views.base.ViewSpecAssertions

trait ViewBehaviours extends SpecBase with ViewSpecAssertions {

  private val path = "foo"

  override def fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", path)

  def view: HtmlFormat.Appendable

  def parseView(view: HtmlFormat.Appendable): Document = Jsoup.parse(view.toString())

  lazy val doc: Document = parseView(view)

  val prefix: String

  "must render service name link in header" in {
    val link = getElementByClass(doc, "govuk-header__service-name")
    assertElementContainsText(link, "Manage your transit movements")
    assertElementContainsHref(link, "/manage-transit-movements/what-do-you-want-to-do")
  }

  "must append service to feedback link" in {
    val link = getElementBySelector(doc, ".govuk-phase-banner__text > .govuk-link")
    getElementHref(link) mustEqual s"http://localhost:9250/contact/beta-feedback?service=CTCTraders&referrerUrl=$path"
  }

  "must render accessibility statement link" in {
    val link = doc
      .select(".govuk-footer__inline-list-item > .govuk-footer__link")
      .toList
      .find(_.text() == "Accessibility statement")
      .get

    getElementHref(link) mustEqual s"http://localhost:12346/accessibility-statement/manage-transit-movements?referrerUrl=$path"
  }

  "must not render language toggle" in {
    assertElementDoesNotExist(doc, "hmrc-language-select")
  }

  "must render 'page not working properly' link" in {
    val link = getElementByClass(doc, "hmrc-report-technical-issue")

    assertElementContainsText(link, "Is this page not working properly? (opens in new tab)")
    getElementHref(link) mustEqual s"http://localhost:9250/contact/report-technical-problem?service=CTCTraders&referrerUrl=$path"
  }

  def pageWithTitle(text: String): Unit =
    "must render title" in {
      checkTitle(text)
    }

  def pageWithTitle(args: Any*): Unit =
    "must render title" in {
      val messageKey = s"$prefix.title"
      checkTitle(messages(messageKey, args*))
      assert(messages.isDefinedAt(messageKey))
    }

  def pageWithTitle(doc: Document, prefix: String, args: Any*): Unit =
    "must render title" in {
      val title      = doc.title()
      val messageKey = s"$prefix.title"
      title mustEqual s"${messages(messageKey, args*)} - Arrival notifications - Manage your transit movements - GOV.UK"
      assert(messages.isDefinedAt(messageKey))
    }

  private def checkTitle(text: String): Assertion = {
    val title = doc.title()
    title mustEqual s"$text - Manage your transit movements - GOV.UK"
  }

  def pageWithHeading(text: String): Unit =
    "must render heading" in {
      checkHeading(text)
    }

  def pageWithHeading(args: Any*): Unit =
    "must render heading" in {
      val messageKey = s"$prefix.heading"
      checkHeading(messages(messageKey, args*))
      assert(messages.isDefinedAt(messageKey))
    }

  def pageWithHeading(doc: Document, prefix: String, args: Any*): Unit =
    "must render heading" in {
      val heading    = getElementByTag(doc, "h1")
      val messageKey = s"$prefix.heading"
      assertElementIncludesText(heading, messages(messageKey, args*))
      assert(messages.isDefinedAt(messageKey))
    }

  private def checkHeading(text: String): Assertion = {
    val heading = getElementByTag(doc, "h1")
    assertElementIncludesText(heading, text)
  }

  def pageWithCaption(expectedText: String): Unit =
    "must render caption" in {
      val caption = getElementByClass(doc, "govuk-caption-xl")
      assertElementContainsText(caption, expectedText)
    }

  def pageWithHint(expectedText: String): Unit =
    "must render hint" in {
      val hint = getElementByClass(doc, "govuk-hint")
      assertElementContainsText(hint, expectedText)
    }

  def pageWithoutHint(): Unit =
    "must not render hint" in {
      assertElementDoesNotExist(doc, "govuk-hint")
    }

  def pageWithSubmitButton(expectedText: String): Unit =
    pageWithButton(expectedText) {
      button => assertElementContainsId(button, "submit")
    }

  def pageWithSubmitButton(doc: Document, expectedText: String): Unit =
    pageWithButton(doc, expectedText) {
      button => assertElementContainsId(button, "submit")
    }

  def pageWithoutSubmitButton(): Unit =
    pageWithoutSubmitButton(doc)

  def pageWithoutSubmitButton(doc: Document): Unit =
    "must not render submit" in {
      assertElementDoesNotExist(doc, "submit")
    }

  def pageWithButton(doc: Document, expectedText: String)(additionalAssertions: Element => Assertion*): Unit =
    s"must render $expectedText button" in {
      val button = doc.getElementsByClass("govuk-button").toList.find(_.text() == expectedText).value
      additionalAssertions.map(_(button))
    }

  def pageWithLink(id: String, expectedText: String, expectedHref: String): Unit =
    pageWithLink(doc, id, expectedText, expectedHref)

  def pageWithLink(doc: Document, id: String, expectedText: String, expectedHref: String): Unit =
    s"must render link with id $id" in {
      val link = getElementById(doc, id)
      assertElementContainsText(link, expectedText)
      assertElementContainsHref(link, expectedHref)
    }

  def pageWithoutLink(doc: Document, id: String): Unit =
    s"must not render link with id $id" in {
      assertNotRenderedById(doc, id)
    }

  def pageWithBackLink(): Unit =
    "must render back link" in {
      val link = getElementByClass(doc, "govuk-back-link")
      assertElementContainsText(link, "Back")
      assertElementContainsHref(link, "#")
    }

  def pageWithoutBackLink(): Unit =
    "must not render back link" in {
      assertElementDoesNotExist(doc, "govuk-back-link")
    }

  def pageWithContent(tag: String, expectedText: String): Unit =
    pageWithContent(doc, tag, expectedText)

  def pageWithContent(doc: Document, tag: String, expectedText: String): Unit =
    pageWithContent(doc, tag, expectedText, _ `equals` _)

  def pageWithPartialContent(tag: String, expectedText: String): Unit =
    pageWithContent(doc, tag, expectedText, _ `contains` _)

  private def pageWithContent(doc: Document, tag: String, expectedText: String, condition: (String, String) => Boolean): Unit =
    s"must render $tag with text $expectedText" in {
      val elements = getElementsByTag(doc, tag)
      assertElementExists(elements, element => condition(element.text, expectedText))
    }

  def pageWithoutContent(doc: Document, tag: String, expectedText: String): Unit =
    s"must not render $tag with text $expectedText" in {
      val elements = getElementsByTag(doc, tag)
      assertElementDoesNotExist(elements, _.text == expectedText)
    }

  def pageWithList(listClass: String, expectedListItems: String*): Unit =
    "must render list" in {
      val list      = getElementByClass(doc, listClass)
      val listItems = list.getElementsByTag("li")
      listItems.toList.map(_.text()) mustEqual expectedListItems
    }

  def pageWithFormAction(expectedUrl: String): Unit =
    "must render form with action" in {
      val formAction = getElementByTag(doc, "form").attr("action")
      formAction mustEqual expectedUrl
    }

  def pageWithoutFormAction(): Unit =
    "must render form with action" in {
      assertElementDoesNotExist(doc, "form")
    }

  def pageWithFullWidth(): Unit =
    "must have full width class" in {
      assert(doc.getElementsByClass("govuk-grid-column-full").size() == 1)
    }

  def pageWithInsetText(expectedText: String): Unit =
    pageWithInsetText(doc, expectedText)

  def pageWithInsetText(doc: Document, expectedText: String): Unit =
    "must render inset text" in {
      val insetText = getElementByClass(doc, "govuk-inset-text")
      assertElementContainsText(insetText, expectedText)
    }

  def boldWords(p: Element): Seq[String] = p.getElementsByTag("b").toList.map(_.text())

  def pageWithButton(expectedText: String)(additionalAssertions: Element => Assertion*): Unit =
    pageWithButton(doc, expectedText)(additionalAssertions*)

  def pageWithWarningText(doc: Document, expectedText: String): Unit =
    s"must render warning text" in {
      val warning = getElementByClass(doc, "govuk-warning-text__text")
      assertElementContainsText(warning, s"Warning $expectedText")
    }

  def pageWithoutWarningText(doc: Document): Unit =
    s"must render warning text" in {
      assertElementDoesNotExist(doc, "govuk-warning-text")
    }

  def pageWithSpecificContent(id: String, expectedText: String): Unit =
    s"must render correct $id content" in {
      val element = doc.getElementById(id)
      assertElementContainsText(element, expectedText)
    }

}
