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

package viewModels

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.departureP5._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.P5.departure.RecoveryNotificationViewModel.RecoveryNotificationViewModelProvider

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RecoveryNotificationViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "RecoveryNotificationViewModel" - {

    val message: IE035Data = IE035Data(
      IE035MessageData(
        TransitOperationIE035(mrn, LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME)),
        RecoveryNotification(LocalDateTime.parse("2014-06-09T16:15:04+01:00", DateTimeFormatter.ISO_DATE_TIME), "text", "1000", "EUR")
      )
    )

    val viewModelProvider = new RecoveryNotificationViewModelProvider()
    val result            = viewModelProvider.apply(message.data)

    "must return correct section length" in {
      result.sections.length mustBe 1
    }

    "must return correct title" in {
      result.title mustBe "Goods being recovered"
    }
    "must return correct heading" in {
      result.heading mustBe "Goods being recovered"
    }
    "must return correct paragraphs" in {
      result.paragraph1 mustBe "There was an issue with this movement during its transit. The goods are now being recovered to a customs office by a local authority."
      result.paragraph2 mustBe "Review the recovery information and wait for the customs office to contact you."
      result.paragraph3 mustBe "Customs will contact you to discuss the issue further."
    }
    "must return correct h2" in {
      result.whatHappensNext mustBe "What happens next"
    }
  }

}
