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

package models

import base.SpecBase
import generators.Generators
import models.arrivalP5.ArrivalMovement
import models.departureP5.DepartureMovement
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class AvailabilitySpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "apply" - {

    "must return NonEmpty" - {
      "when non-empty list of departures" in {
        forAll(listWithMaxLength[Seq[DepartureMovement]]()) {
          departures =>
            Availability.apply(Some(departures)) `mustBe` Availability.NonEmpty
        }
      }

      "when non-empty list of arrivals" in {
        forAll(listWithMaxLength[Seq[ArrivalMovement]]()) {
          arrivals =>
            Availability.apply(Some(arrivals)) `mustBe` Availability.NonEmpty
        }
      }
    }

    "must return Empty" - {
      "when empty list" in {
        Availability.apply(Some(Nil)) `mustBe` Availability.Empty
      }
    }

    "must return Unavailable" - {
      "when no list of movements" in {
        Availability.apply(None) `mustBe` Availability.Unavailable
      }
    }
  }

}
