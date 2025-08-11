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

package viewModels.drafts

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.{DepartureUserAnswerSummary, DeparturesSummary}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class AllDraftDeparturesViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with Generators with ScalaCheckPropertyChecks {

  "AllDraftDeparturesViewModel" - {

    "isSearch" - {

      val userAnswerSummary: List[DepartureUserAnswerSummary] = Gen.listOfN(2, arbitrary[DepartureUserAnswerSummary]).sample.value
      val departuresSummary: DeparturesSummary                = DeparturesSummary(0, 0, userAnswerSummary)

      "must return true when LRN is defined" in {

        val viewModel = AllDraftDeparturesViewModel(
          departuresSummary,
          Some("AB123"),
          1,
          2
        )

        viewModel.isSearch mustEqual true
      }

      "must return false when LRN is not defined" in {

        val viewModel = AllDraftDeparturesViewModel(
          departuresSummary,
          None,
          1,
          2
        )

        viewModel.isSearch mustEqual false
      }

    }
  }
}
