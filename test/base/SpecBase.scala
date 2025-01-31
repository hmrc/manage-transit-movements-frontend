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
import models.departureP5.DepartureReferenceNumbers
import models.referenceData.CustomsOffice
import models.{DepartureId, Index, LocalReferenceNumber}
import org.scalatest.*
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Content, Key, Value}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse}

import java.time.Clock

trait SpecBase extends AnyFreeSpec with Matchers with OptionValues with TryValues with ScalaFutures with IntegrationPatience with AppWithDefaultMockFixtures {

  val configKey                 = "config"
  val lrn: LocalReferenceNumber = LocalReferenceNumber("ABCD1234567890123")
  val mrn: String               = "ABCD1234567890123"

  val departureId: DepartureId = DepartureId(1)
  val messageId: String        = "343ffafafaaf"
  val departureIdP5: String    = "643cffea2dca70b2"
  val arrivalIdP5: String      = "62f4ebbb765ba8c2"

  val index: Index                   = Index(0)
  val incidentIndex: Index           = Index(0)
  val transportEquipmentIndex: Index = Index(0)

  val fakeCustomsOffice: CustomsOffice = CustomsOffice("1234", "Customs Office", Some("01234567"))

  val departureReferenceNumbers: DepartureReferenceNumbers = DepartureReferenceNumbers(lrn.value, None)

  def injector: Injector                   = app.injector
  def fakeRequest: FakeRequest[AnyContent] = FakeRequest("", "")

  def messagesApi: MessagesApi    = injector.instanceOf[MessagesApi]
  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  implicit def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]
  def paginationAppConfig: PaginationAppConfig      = injector.instanceOf[PaginationAppConfig]

  implicit val clock: Clock = Clock.systemDefaultZone()

  implicit class RichContent(c: Content) {
    def value: String = c.asHtml.toString()
  }

  implicit class RichValue(v: Value) {
    def value: String = v.content.value
  }

  implicit class RichAction(ai: ActionItem) {
    def id: String = ai.attributes.get("id").value
  }

  implicit class RichKey(k: Key) {
    def value: String = k.content.value
  }

  def httpResponse(status: Int): HttpResponse = HttpResponse(status, "")
}
