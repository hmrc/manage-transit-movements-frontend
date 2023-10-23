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
import models.departureP5._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import viewModels.P5.departure.GoodsNotReleasedP5ViewModel
import viewModels.P5.departure.GoodsNotReleasedP5ViewModel.GoodsNotReleasedP5ViewModelProvider

import java.time.LocalDateTime

class GoodsNotReleasedP5ViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super.guiceApplicationBuilder()

  "GoodsNotReleasedP5ViewModelSpec" - {

    val lrn                              = "AB123"
    val declarationSubmissionDateAndTime = LocalDateTime.now()

    val iE051Data = IE051Data(
      IE051MessageData(
        TransitOperationIE051(
          "AB123",
          declarationSubmissionDateAndTime,
          "G1",
          "Guarantee not valid"
        )
      )
    )

    val viewModelProvider = new GoodsNotReleasedP5ViewModelProvider()

    def viewModel: GoodsNotReleasedP5ViewModel =
      viewModelProvider.apply(iE051Data.data, lrn)

    "must return correct section" in {
      viewModel.sections.head.sectionTitle mustBe None
      viewModel.sections.head.rows.size mustBe 4
    }

    "title and heading" - {
      "must return correct message" in {
        viewModel.titleAndHeading mustBe
          "Goods not released"
      }
    }

    "paragraph" in {
      viewModel.paragraph mustBe
        s"Customs have reviewed this declaration and decided not to release the goods for transit. This means the movement has now ended."
    }
  }
}
