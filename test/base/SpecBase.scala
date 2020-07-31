/*
 * Copyright 2020 HM Revenue & Customs
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

import config.FrontendAppConfig
import controllers.actions._
import models.UserAnswers
import org.mockito.Mockito
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice._
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{bind, Binding, Injector}
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.nunjucks.NunjucksRenderer

import scala.reflect.ClassTag

trait SpecBase
    extends FreeSpec
    with MustMatchers
    with GuiceOneAppPerSuite
    with OptionValues
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with BeforeAndAfterEach {

  override def beforeEach {
    Mockito.reset(mockRenderer)
  }

  val configKey = "config"

  val userAnswersId = "id"

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId, Json.obj())

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig =
    injector.instanceOf[FrontendAppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  val mockRenderer: NunjucksRenderer = mock[NunjucksRenderer]

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  def bindingOverride[A: ClassTag](module: A): GuiceApplicationBuilder => GuiceApplicationBuilder =
    guiceApplicationBuilder => guiceApplicationBuilder.overrides(bind[A].toInstance(module))

  protected def applicationBuilder(
    userAnswers: Option[UserAnswers] = None
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[NunjucksRenderer].toInstance(mockRenderer)
      )
}
