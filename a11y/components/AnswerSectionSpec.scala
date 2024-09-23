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
import viewModels.sections.Section.StaticSection
import views.html.components.AnswerSection
import views.html.templates.MainTemplate

class AnswerSectionSpec extends A11ySpecBase {

  "the 'answer section' component" must {
    val template  = app.injector.instanceOf[MainTemplate]
    val component = app.injector.instanceOf[AnswerSection]

    val title   = nonEmptyString.sample.value
    val section = arbitrary[StaticSection].sample.value

    val content = template.apply(title) {
      component.apply(section).withHeading(title)
    }

    "pass accessibility checks" in {
      content.toString() must passAccessibilityChecks
    }
  }

}
