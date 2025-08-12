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

package base

import config.{FrontendAppConfig, PaginationAppConfig}
import controllers.actions.*
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.cache.AsyncCacheApi
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{bind, Injector}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest

trait AppWithDefaultMockFixtures extends GuiceOneAppPerSuite with BeforeAndAfterEach with MockitoSugar {
  self: TestSuite =>

  def injector: Injector = app.injector

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest: FakeRequest[AnyContent] = FakeRequest("", "")

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  implicit def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  def paginationAppConfig: PaginationAppConfig = injector.instanceOf[PaginationAppConfig]

  override def fakeApplication(): Application =
    guiceApplicationBuilder()
      .build()

  // Override to provide custom binding
  private def defaultApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].toInstance(FakeIdentifierAction()),
        bind[AsyncCacheApi].to[FakeAsyncCacheApi]
      )

  def guiceApplicationBuilder(): GuiceApplicationBuilder =
    defaultApplicationBuilder()

}
