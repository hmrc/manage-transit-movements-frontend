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
import play.api.mvc.Call
import play.api.test.Helpers.GET
import viewModels.pagination.PaginationViewModel
import views.html.components.Pagination
import views.html.templates.MainTemplate

class PaginationSpec extends A11ySpecBase {

  "the 'pagination' component" must {
    val template  = app.injector.instanceOf[MainTemplate]
    val component = app.injector.instanceOf[Pagination]

    val title = nonEmptyString.sample.value

    class FakePaginationViewModel(
      override val items: Seq[String],
      override val currentPage: Int,
      override val numberOfItemsPerPage: Int
    ) extends PaginationViewModel[String] {
      override val href: Call                              = Call(GET, "href")
      override val heading: String                         = title
      override val additionalParams: Seq[(String, String)] = Seq("foo" -> "bar")
      override val searchParam: Option[String]             = None
    }

    "pass accessibility checks" when {

      "1 page" in {
        val paginationViewModel = new FakePaginationViewModel(
          items = Seq("foo"),
          currentPage = 1,
          numberOfItemsPerPage = 2
        )
        val content = template.apply(title) {
          component.apply(paginationViewModel).withHeading(title)
        }
        content.toString() must passAccessibilityChecks
      }

      "multiple pages" in {
        val paginationViewModel = new FakePaginationViewModel(
          items = Seq("foo", "bar", "baz"),
          currentPage = 1,
          numberOfItemsPerPage = 2
        )
        val content = template.apply(title) {
          component.apply(paginationViewModel).withHeading(title)
        }
        content.toString() must passAccessibilityChecks
      }
    }
  }

}
