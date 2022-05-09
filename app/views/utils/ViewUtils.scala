/*
 * Copyright 2022 HM Revenue & Customs
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

package views.utils

import models.FunctionalError
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow, Value}

object ViewUtils {

  def breadCrumbTitle(title: String, mainContent: Html)(implicit messages: Messages): String =
    (if (mainContent.body.contains("govuk-error-summary")) s"${messages("error.title.prefix")} " else "") +
      s"$title - ${messages("site.service_name")} - GOV.UK"

  implicit class RichFunctionalError(functionalError: FunctionalError) {

    def toSummaryList(implicit messages: Messages): SummaryList = SummaryList(
      rows = Seq(
        Some(
          SummaryListRow(
            key = messages("xmlNegativeAcknowledgement.errorType").toKey,
            value = Value(functionalError.errorType.toString.toText)
          )
        ),
        Some(
          SummaryListRow(
            key = messages("xmlNegativeAcknowledgement.errorPointer").toKey,
            value = Value(functionalError.pointer.value.toText)
          )
        ),
        functionalError.reason.map {
          reason =>
            SummaryListRow(
              key = messages("xmlNegativeAcknowledgement.errorReason").toKey,
              value = Value(reason.toText)
            )
        },
        functionalError.originalAttributeValue.map {
          originalAttributeValue =>
            SummaryListRow(
              key = messages("xmlNegativeAcknowledgement.originalAttributeValue").toKey,
              value = Value(originalAttributeValue.toText)
            )
        }
      ).flatten
    )
  }

}
