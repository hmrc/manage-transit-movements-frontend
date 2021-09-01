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
import play.api.libs.json.{JsObject, Json}
import play.twirl.api.Html
import renderer.Renderer
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.Environment
import play.api.Configuration
import play.api.test.Helpers
import uk.gov.hmrc.nunjucks.NunjucksSetup
import uk.gov.hmrc.nunjucks.NunjucksRenderer
import uk.gov.hmrc.nunjucks.DevelopmentNunjucksRoutesHelper
import uk.gov.hmrc.nunjucks.NunjucksConfigurationProvider
import play.api.i18n.Messages

abstract class SingleViewSpec(protected val viewUnderTest: String) extends SpecBase with ViewSpecAssertions with NunjucksSupport {

  require(viewUnderTest.endsWith(".njk"), "Expected view with file extension of `.njk`")

  override val messages: Messages = Helpers.stubMessages()

  private def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  private val renderer = {
    val env                   = Environment.simple()
    val nunjucksSetup         = new NunjucksSetup(env)
    val nunjucksConfiguration = (new NunjucksConfigurationProvider(Configuration.load(env), nunjucksSetup)).get()
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

    implicit val fr = fakeRequest.withCSRFToken

    renderer
      .render(viewUnderTest, json)
      .map(asDocument)
  }

}