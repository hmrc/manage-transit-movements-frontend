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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ViewAllDepartureMovementsP5ViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks {

  "Display correct title" - {

    "When searchParam provided" in {
      val viewModel = ViewAllDepartureMovementsP5ViewModel(Seq.empty, Some("LRN123"), 1, 20, 0)

      viewModel.title mustEqual "Search results for ‘LRN123’ - Departure declarations"
    }

    "When searchParam not provided" in {
      val viewModel = ViewAllDepartureMovementsP5ViewModel(Seq.empty, None, 1, 20, 0)

      viewModel.title mustEqual "Departure declarations"
    }

  }

  "Display correct heading" - {

    "When searchParam provided" in {
      val viewModel = ViewAllDepartureMovementsP5ViewModel(Seq.empty, Some("LRN123"), 1, 20, 0)

      viewModel.heading mustEqual "Search results for ‘LRN123’ - Departure declarations"
    }

    "When searchParam not provided" in {
      val viewModel = ViewAllDepartureMovementsP5ViewModel(Seq.empty, None, 1, 20, 0)

      viewModel.title mustEqual "Departure declarations"
    }
  }
}
