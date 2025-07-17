/*
 * Copyright 2024 HM Revenue & Customs
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

package viewModels.components

import base.SpecBase
import play.twirl.api.Html

class InputYesNoViewModelSpec extends SpecBase {

  "InputYesNoViewModel" - {

    "create an OrdinaryYesNo with a heading and no caption" in {
      val heading   = "Heading"
      val viewModel = InputYesNoViewModel.OrdinaryYesNo(heading)

      viewModel.heading mustEqual heading
      viewModel.caption must be(None)
    }

    "create an OrdinaryYesNo with a heading and a caption" in {
      val heading   = "Heading"
      val caption   = Some("caption")
      val viewModel = InputYesNoViewModel.OrdinaryYesNo(heading, caption)

      viewModel.heading mustEqual heading
      viewModel.caption must be(caption)
    }

    "create a YesNoWithAdditionalHtml with a heading, caption, and additionalHtml" in {
      val heading        = "Heading"
      val caption        = Some("caption")
      val additionalHtml = Html("<p>Additional html</p>")
      val viewModel      = InputYesNoViewModel.YesNoWithAdditionalHtml(heading, caption, additionalHtml)

      viewModel.heading must be(heading)
      viewModel.caption must be(caption)
      viewModel.additionalHtml mustEqual additionalHtml
    }

    "create a YesNoWithLegend with a legend" in {
      val legend    = "Important Decision"
      val viewModel = InputYesNoViewModel.YesNoWithLegend(legend)

      viewModel.legend mustEqual legend
    }

    "create a YesNoWithAdditionalHtml without a caption" in {
      val heading        = "Proceed?"
      val additionalHtml = Html("<div>More info</div>")
      val viewModel      = InputYesNoViewModel.YesNoWithAdditionalHtml(heading, None, additionalHtml)

      viewModel.heading mustEqual heading
      viewModel.caption must be(None)
      viewModel.additionalHtml mustEqual additionalHtml
    }

  }
}
