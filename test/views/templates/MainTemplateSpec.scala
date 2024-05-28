/*
 * Copyright 2024 HM Revenue & Customs
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

package views.templates

import base.SpecBase
import config.FrontendAppConfig
import generators.Generators
import org.jsoup.Jsoup
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.base.ViewSpecAssertions
import views.html.templates.MainTemplate

class MainTemplateSpec extends SpecBase with ViewSpecAssertions with ScalaCheckPropertyChecks with Generators {

  private val path                                           = "foo"
  implicit private lazy val request: FakeRequest[AnyContent] = FakeRequest("GET", path)

  "when not in trader test" - {
    val app = new GuiceApplicationBuilder()
      .configure("trader-test.enabled" -> false)
      .build()

    "must point feedback at feedback form" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (content, title) =>
          val view = app.injector
            .instanceOf[MainTemplate]
            .apply(title) {
              Html.apply(content)
            }

          val doc = Jsoup.parse(view.toString())

          val link = getElementBySelector(doc, ".govuk-phase-banner__text > .govuk-link")
          getElementHref(link) mustBe s"http://localhost:9250/contact/beta-feedback?service=CTCTraders&referrerUrl=$path"
      }
    }

    "must use HMRC 'report technical issue' helper" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (content, title) =>
          val view = app.injector
            .instanceOf[MainTemplate]
            .apply(title) {
              Html.apply(content)
            }

          val doc = Jsoup.parse(view.toString())

          val link = getElementBySelector(doc, ".hmrc-report-technical-issue")
          getElementHref(link) mustBe s"http://localhost:9250/contact/report-technical-problem?newTab=true&service=CTCTraders&referrerUrl=$path"
          link.text() mustBe "Is this page not working properly? (opens in new tab)"
      }
    }
  }

  "when in trader test" - {
    val app = new GuiceApplicationBuilder()
      .configure("trader-test.enabled" -> true)
      .build()

    val config = app.injector.instanceOf[FrontendAppConfig]

    "must point feedback at google form" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (content, title) =>
          val view = app.injector
            .instanceOf[MainTemplate]
            .apply(title) {
              Html.apply(content)
            }

          val doc = Jsoup.parse(view.toString())

          val link = getElementBySelector(doc, ".govuk-phase-banner__text > .govuk-link")
          getElementHref(link) mustBe config.feedbackForm
      }
    }

    "must use custom link for reporting issues" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (content, title) =>
          val view = app.injector
            .instanceOf[MainTemplate]
            .apply(title) {
              Html.apply(content)
            }

          val doc = Jsoup.parse(view.toString())

          val link = getElementBySelector(doc, ".hmrc-report-technical-issue")
          getElementHref(link) must startWith(s"mailto:${config.feedbackEmail}")
          link.text() mustBe s"If you have any questions or issues, email us at ${config.feedbackEmail}"
      }
    }
  }
}
