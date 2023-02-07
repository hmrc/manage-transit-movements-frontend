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

import base.SpecBase
import generators.Generators
import models.{DepartureUserAnswerSummary, DeparturesSummary}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class AllDraftDeparturesViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "AllDraftDeparturesViewModelSpec" - {

    "When DraftDepartures are tabulated must display correct data and format" in {

      val userAnswerSummary: Gen[List[DepartureUserAnswerSummary]] = Gen.listOfN(2, arbitrary[DepartureUserAnswerSummary])

      forAll(userAnswerSummary) {
        userAnswerSummary =>
          val draftDeparture = DeparturesSummary(userAnswerSummary)

          val viewModel = AllDraftDeparturesViewModel(draftDeparture)

          viewModel.dataRows.length mustBe draftDeparture.userAnswers.length

          viewModel.dataRows.head.lrn mustBe draftDeparture.userAnswers.head.lrn.toString
          viewModel.dataRows(1).lrn mustBe draftDeparture.userAnswers(1).lrn.toString

          viewModel.dataRows.head.daysRemaining mustBe draftDeparture.userAnswers.head.expiresInDays
          viewModel.dataRows(1).daysRemaining mustBe draftDeparture.userAnswers(1).expiresInDays
      }
    }
  }
}
