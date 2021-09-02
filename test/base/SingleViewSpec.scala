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

abstract class SingleViewSpec(protected val viewUnderTest: String) extends SpecBase with ViewSpecAssertions with NunjucksSupport {

  require(viewUnderTest.endsWith(".njk"), "Expected view with file extension of `.njk`")

  override val messages: Messages = Helpers.stubMessages()

  private def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  private val renderer = {
    val env                   = Environment.simple()
    val nunjucksSetup         = new NunjucksSetup(env)
    val nunjucksConfiguration = new NunjucksConfigurationProvider(Configuration.load(env), nunjucksSetup).get()
    val nunjucksRoutesHelper  = new DevelopmentNunjucksRoutesHelper(env)

    val nunjucksRenderer = new NunjucksRenderer(
      nunjucksSetup,
      nunjucksConfiguration,
      env,
      nunjucksRoutesHelper,
      Helpers.stubMessagesApi()
    )

    new Renderer(FakeFrontendAppConfig(), nunjucksRenderer)
  }

  def renderDocument(json: JsObject = Json.obj()): Future[Document] = {
    import play.api.test.CSRFTokenHelper._

    implicit val fr: RequestHeader = fakeRequest.withCSRFToken

    renderer
      .render(viewUnderTest, json)
      .map(asDocument)
  }

  def pageWithHeading(doc: Document, messageKeyPrefix: String): Unit =
    "display page heading" in {
      doc.selectFirst("h1").text() mustBe s"$messageKeyPrefix.heading"
    }

  def pageWithLink(doc: Document, id: String, expectedText: String): Unit =
    s"display link with id $id" in {
      doc.selectFirst(s"a[id=$id]").text() mustBe expectedText
    }

}
