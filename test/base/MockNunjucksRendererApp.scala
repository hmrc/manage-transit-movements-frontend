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

import controllers.actions._
import org.mockito.Mockito
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers
import uk.gov.hmrc.nunjucks.NunjucksRenderer

trait MockNunjucksRendererApp extends GuiceOneAppPerSuite with BeforeAndAfterEach with MockitoSugar {
  self: TestSuite =>

  val mockNunjucksRenderer: NunjucksRenderer = mock[NunjucksRenderer]

  override def beforeEach {
    Mockito.reset(
      mockNunjucksRenderer
    )
    super.beforeEach()
  }

  override def fakeApplication(): Application =
    guiceApplicationBuilder()
      .build()

  // Override to provide custom binding
  def guiceApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[NunjucksRenderer].toInstance(mockNunjucksRenderer),
        bind[MessagesApi].toInstance(Helpers.stubMessagesApi())
      )
}