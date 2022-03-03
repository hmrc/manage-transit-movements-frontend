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

package base

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.RequestHeader
import play.api.test.Helpers
import play.api.{Configuration, Environment}
import play.twirl.api.Html
import renderer.Renderer
import uk.gov.hmrc.nunjucks.{DevelopmentNunjucksRoutesHelper, NunjucksConfigurationProvider, NunjucksRenderer, NunjucksSetup}
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class SingleViewSpec(protected val viewUnderTest: String, hasSignOutLink: Boolean = true)
    extends SpecBase
    with ViewSpecAssertions
    with NunjucksSupport {

  require(viewUnderTest.endsWith(".njk"), "Expected view with file extension of `.njk`")

  override val messages: Messages = Helpers.stubMessages()

  private def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  private val renderer = {
    val env                   = Environment.simple()
    val nunjucksSetup         = new NunjucksSetup(env)
    val configuration         = Configuration.load(env)
    val nunjucksConfiguration = new NunjucksConfigurationProvider(configuration, nunjucksSetup).get()
    val nunjucksRoutesHelper  = new DevelopmentNunjucksRoutesHelper(env)

    val nunjucksRenderer = new NunjucksRenderer(
      nunjucksSetup,
      nunjucksConfiguration,
      configuration,
      env,
      nunjucksRoutesHelper,
      Helpers.stubMessagesApi()
    )

    new Renderer(frontendAppConfig, nunjucksRenderer)
  }

  def renderDocument(json: JsObject = Json.obj()): Future[Document] = {
    import play.api.test.CSRFTokenHelper._

    implicit val fr: RequestHeader = fakeRequest.withCSRFToken

    renderer
      .render(viewUnderTest, json)
      .map(asDocument)
  }

  if (hasSignOutLink) {
    "must render sign out link in header" in {
      val doc: Document = renderDocument().futureValue

      assertPageHasSignOutLink(
        doc = doc,
        expectedText = "Sign out",
        expectedHref = "http://localhost:9553/bas-gateway/sign-out-without-state?continue=http://localhost:9514/feedback/manage-transit-movements"
      )
    }
  } else {
    "must not render sign out link in header" in {
      val doc: Document = renderDocument(
        Json.obj("signInUrl" -> "/manage-transit-movements/what-do-you-want-to-do")
      ).futureValue

      assertPageHasNoSignOutLink(doc)
    }
  }

  "must append service to feedback link" in {
    val doc: Document = renderDocument().futureValue
    val link          = doc.getElementsByClass("govuk-phase-banner__text").first().getElementsByClass("govuk-link").first()
    link.attr("href") must include("?service=CTCTraders")
  }

}
