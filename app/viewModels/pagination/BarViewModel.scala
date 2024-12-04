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

package viewModels.pagination

import models.{FunctionalError, FunctionalErrors}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table

trait BarViewModel[A <: FunctionalError, B <: FunctionalErrors[A]] extends FooViewModel[A] {

  val functionalErrors: B

  override val items: Seq[A] = functionalErrors.value

  def table(implicit messages: Messages): Table = functionalErrors.paginate(currentPage, numberOfItemsPerPage).toTable
}
