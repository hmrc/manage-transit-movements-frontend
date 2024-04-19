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

import base.SpecBase
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.P5.departure.ViewAllDepartureMovementsP5ViewModel
import viewModels.pagination._

class ViewAllDepartureMovementsP5ViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val paginationViewModel: PaginationViewModel = ListPaginationViewModel(1, 1, 10, "")

  "Display correct title" - {

    "When searchParam provided" in {
      val viewModel = new ViewAllDepartureMovementsP5ViewModel(Seq.empty, paginationViewModel, Some("LRN123"))

      viewModel.pageTitle mustBe "Search results for ‘LRN123’ - Departure declarations"
    }

    "When searchParam not provided" in {
      val viewModel = new ViewAllDepartureMovementsP5ViewModel(Seq.empty, paginationViewModel, None)

      viewModel.pageTitle mustBe "Departure declarations"
    }

  }

  "Display correct heading" - {

    "When searchParam provided" in {
      val viewModel = new ViewAllDepartureMovementsP5ViewModel(Seq.empty, paginationViewModel, Some("LRN123"))

      viewModel.pageHeading mustBe "Search results for ‘LRN123’ - Departure declarations"
    }

    "When searchParam not provided" in {
      val viewModel = new ViewAllDepartureMovementsP5ViewModel(Seq.empty, paginationViewModel, None)

      viewModel.pageTitle mustBe "Departure declarations"
    }

  }
}
