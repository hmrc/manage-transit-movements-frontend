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

package utils

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class DeparturesP5MessageHelper(implicit messages: Messages) extends SummaryListRowHelper {

  def buildRowFromAnswer[T](
    answer: Option[T],
    formatAnswer: T => Content,
    prefix: String,
    id: Option[String],
    call: Option[Call],
    args: Any*
  ): Option[SummaryListRow] =
    answer.map(
      ans =>
        buildRow(
          prefix = prefix,
          answer = formatAnswer(ans),
          id = id,
          call = call,
          args = args*
        )
    )

}
