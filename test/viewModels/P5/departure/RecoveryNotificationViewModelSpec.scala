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

package viewModels.P5.departure

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.CC035CType
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.P5.departure.RecoveryNotificationViewModel.RecoveryNotificationViewModelProvider

class RecoveryNotificationViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "RecoveryNotificationViewModel" - {

    val message = arbitrary[CC035CType].sample.value

    val viewModelProvider = new RecoveryNotificationViewModelProvider()
    val result            = viewModelProvider.apply(message)

    "must return correct section length" in {
      result.sections.length `mustBe` 1
    }

    "must return correct title" in {
      result.title `mustBe` "Goods being recovered"
    }
    "must return correct heading" in {
      result.heading `mustBe` "Goods being recovered"
    }
    "must return correct paragraphs" in {
      result.paragraph1 `mustBe` "There was an issue with this movement during its transit. The goods are now being recovered to a customs office by a local authority."
      result.paragraph2 `mustBe` "Review the recovery information and wait for the customs office to contact you."
      result.paragraph3 `mustBe` "Customs will contact you to discuss the issue further."
    }
    "must return correct h2" in {
      result.whatHappensNext `mustBe` "What happens next"
    }
  }

}
