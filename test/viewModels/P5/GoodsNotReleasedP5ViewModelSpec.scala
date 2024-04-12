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
import generated.CC051CType
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import viewModels.P5.departure.GoodsNotReleasedP5ViewModel
import viewModels.P5.departure.GoodsNotReleasedP5ViewModel.GoodsNotReleasedP5ViewModelProvider

class GoodsNotReleasedP5ViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super.guiceApplicationBuilder()

  "GoodsNotReleasedP5ViewModelSpec" - {

    val lrn = "AB123"

    val message = arbitrary[CC051CType].sample.value

    val viewModelProvider = new GoodsNotReleasedP5ViewModelProvider()

    def viewModel: GoodsNotReleasedP5ViewModel =
      viewModelProvider.apply(message, lrn)

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
        "Customs have reviewed this declaration and decided not to release the goods for transit. This means the movement has now ended."
    }
  }
}
