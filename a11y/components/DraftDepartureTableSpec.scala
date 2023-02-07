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
import views.html.templates.MainTemplate
import views.html.components.DraftDeparturesTable

class DraftDepartureTableSpec extends A11ySpecBase {

  "the 'draft departure table' component" must {
    val template  = app.injector.instanceOf[MainTemplate]
    val component = app.injector.instanceOf[DraftDeparturesTable]

    val title                     = nonEmptyString.sample.value

    "pass accessibility checks" when {

      "draft departure table" in {
        val dataRows = arbitrary[AllDraftDeparturesViewModel].sample.value
        val content = template.apply(title) {
          component.apply(dataRows).withHeading(title)
        }
        content.toString() must passAccessibilityChecks
      }
    }
  }
}
