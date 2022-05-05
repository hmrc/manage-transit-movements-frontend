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

import config.FrontendAppConfig
import models.{DepartureId, LocalReferenceNumber}
import org.scalatest._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.baseApplicationBuilder.injector
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}

import java.time.Clock

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with OptionValues
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with BeforeAndAfterEach {

  val configKey                 = "config"
  val lrn: LocalReferenceNumber = LocalReferenceNumber("ABCD1234567890123")

  val departureId: DepartureId = DepartureId(1)

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  def messagesApi: MessagesApi    = injector.instanceOf[MessagesApi]
  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  implicit val frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  implicit val clock: Clock = Clock.systemDefaultZone()

}
