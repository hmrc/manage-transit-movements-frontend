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

package models

import base.SpecBase

class AvailabilitySpec extends SpecBase {

  "isAvailable" - {

    "must return true" - {
      "when Empty" in {
        Availability.Empty.isAvailable mustBe true
      }

      "when NonEmpty" in {
        Availability.NonEmpty.isAvailable mustBe true
      }
    }

    "must return false" - {
      "when Unavailable" in {
        Availability.Unavailable.isAvailable mustBe false
      }
    }
  }

  "isAvailableAndNonEmpty" - {

    "must return true" - {
      "when NonEmpty" in {
        Availability.NonEmpty.isAvailableAndNonEmpty mustBe true
      }
    }

    "must return false" - {
      "when Empty" in {
        Availability.Empty.isAvailableAndNonEmpty mustBe false
      }

      "when Unavailable" in {
        Availability.Unavailable.isAvailableAndNonEmpty mustBe false
      }
    }
  }

}
