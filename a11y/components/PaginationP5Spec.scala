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
import org.scalacheck.Gen
import viewModels.pagination.{MetaData, PaginationViewModel}
import views.html.components.{Pagination, PaginationP5}
import views.html.templates.MainTemplate

class PaginationP5Spec extends A11ySpecBase {

  "the 'pagination' component" must {
    val template  = app.injector.instanceOf[MainTemplate]
    val component = app.injector.instanceOf[PaginationP5]

    val title = nonEmptyString.sample.value

    "pass accessibility checks" when {

      "0 pages" in {
        val metaData            = arbitrary[MetaData].sample.value.copy(totalPages = 0)
        val paginationViewModel = arbitrary[PaginationViewModel].sample.value.copy(results = metaData)
        val content = template.apply(title) {
          component.apply(paginationViewModel).withHeading(title)
        }
        content.toString() must passAccessibilityChecks
      }

      "1 page" in {
        val metaData            = arbitrary[MetaData].sample.value.copy(totalPages = 1)
        val paginationViewModel = arbitrary[PaginationViewModel].sample.value.copy(results = metaData)
        val content = template.apply(title) {
          component.apply(paginationViewModel).withHeading(title)
        }
        content.toString() must passAccessibilityChecks
      }

      "multiple pages" in {
        val totalPages          = Gen.choose(2, Int.MaxValue).sample.value
        val metaData            = arbitrary[MetaData].sample.value.copy(totalPages = totalPages)
        val paginationViewModel = arbitrary[PaginationViewModel].sample.value.copy(results = metaData)
        val content = template.apply(title) {
          component.apply(paginationViewModel).withHeading(title)
        }
        content.toString() must passAccessibilityChecks
      }
    }
  }
}
