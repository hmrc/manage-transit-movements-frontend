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
import controllers.departure.{routes => departureRoutes}
import generators.Generators
import models.{Departure, DraftDeparture}
import models.departure.DepartureStatus._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.drafts.AllDraftDeparturesViewModel.getRemainingDays

import java.time.LocalDate
import scala.annotation.unused

class AllDraftDeparturesViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "AllDraftDeparturesViewModelSpec" - {


    "When DraftDepartures are tabulated" in {


      val draftDepartures: Gen[List[DraftDeparture]] = Gen.listOfN(2, arbitrary[DraftDeparture])
      val today = LocalDate.now()

      forAll(draftDepartures) {
        draftDeparture =>
          val viewModel = AllDraftDeparturesViewModel(draftDeparture)

          viewModel.dataRows.length mustBe draftDeparture.length

          viewModel.dataRows(0).lrn mustBe draftDeparture(0).lrn
          viewModel.dataRows(1).lrn mustBe draftDeparture(1).lrn

          viewModel.dataRows(0).daysRemaining mustBe getRemainingDays(draftDeparture(0).createdAt,today)
          viewModel.dataRows(1).daysRemaining mustBe getRemainingDays(draftDeparture(1).createdAt,today)

      }




    }



  }
}
