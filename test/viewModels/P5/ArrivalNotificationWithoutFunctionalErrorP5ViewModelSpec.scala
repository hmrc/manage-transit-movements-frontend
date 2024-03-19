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

package viewModels.P5

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.referenceData.FunctionalErrorWithDesc
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.P5.arrival.ArrivalNotificationWithoutFunctionalErrorP5ViewModel.ArrivalNotificationWithoutFunctionalErrorP5ViewModelProvider

import scala.concurrent.Future

class ArrivalNotificationWithoutFunctionalErrorP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(api.inject.bind[ReferenceDataService].toInstance(mockReferenceDataService))

  override def beforeEach(): Unit =
    reset(mockReferenceDataService)

  val mrnString = "MRNAB123"

  "ArrivalNotificationWithoutFunctionalErrorP5ViewModel" - {

    val functionalErrorReferenceData = FunctionalErrorWithDesc("12", "Codelist violation")

    "when there is no error" - {

      when(mockReferenceDataService.getFunctionalError(any())(any(), any())).thenReturn(Future.successful(functionalErrorReferenceData))

      val viewModelProvider = new ArrivalNotificationWithoutFunctionalErrorP5ViewModelProvider()
      val result            = viewModelProvider.apply(mrnString)

      "must return correct title" in {
        result.title mustBe "Notification errors"
      }
      "must return correct heading" in {
        result.heading mustBe "Notification errors"
      }
      "must return correct paragraph 1" in {
        result.paragraph1 mustBe s"There are one or more errors in this notification that cannot be amended. Make a new notification with the right information. We will keep your previous answers for 30 days - so if you use the same MRN within this time, your answers will be pre-populated."
      }
      "must return correct paragraph 2" in {
        result.paragraph2 mustBe s"Make a new arrival notification with the right information."
      }
      "must return correct paragraph 3 prefix, link and suffix" in {
        result.paragraph3Prefix mustBe "Contact the"
        result.paragraph3Link mustBe "New Computerised Transit System helpdesk"
        result.paragraph3Suffix mustBe "for help understanding the errors (opens in a new tab)."
      }
      "must return correct hyperlink text" in {
        result.hyperlink mustBe "Make another arrival notification"
      }
    }
  }
}
