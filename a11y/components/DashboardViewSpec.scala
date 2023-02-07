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

package components

import a11ySpecBase.A11ySpecBase
import org.scalacheck.Arbitrary.arbitrary
import viewModels.drafts.AllDraftDeparturesViewModel
import views.html.components.DraftDeparturesTable
import views.html.departure.drafts.DashboardView
import views.html.templates.MainTemplate

class DashboardViewSpec extends A11ySpecBase {

  "the 'dashboard view' component" must {
    val component = app.injector.instanceOf[DashboardView]

    "pass accessibility checks" when {

      "dashboard view spec" in {
        val viewModel = arbitrary[AllDraftDeparturesViewModel].sample.value
        val content   = component.apply(viewModel)
        content.toString() must passAccessibilityChecks
      }
    }
  }
}
