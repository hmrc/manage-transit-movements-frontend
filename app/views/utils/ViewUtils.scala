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

package views.utils

import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.implicits.RichRadiosSupport
import uk.gov.hmrc.govukfrontend.views.viewmodels.FormGroup

object ViewUtils {

  def breadCrumbTitle(title: String, mainContent: Html)(implicit messages: Messages): String =
    (if (mainContent.body.contains("govuk-error-summary")) s"${messages("error.title.prefix")} " else "") +
      s"$title - ${messages("site.service_name")} - GOV.UK"

  def searchInput(form: Form[?], label: String)(implicit messages: Messages): Input = {
    val field = form("value")
    Input(
      label = Label(
        content = messages(label).toText
      ),
      errorMessage = field.error.map {
        e =>
          ErrorMessage.errorMessageWithDefaultStringsTranslated(content = Text(messages(e.message, e.args*)))
      },
      inputType = "search",
      classes = "govuk-!-width-one-half"
    ).withFormField(field)
  }

  implicit class RadiosImplicits(radios: Radios)(implicit messages: Messages) extends RichRadiosSupport {

    def withHeadingAndCaption(heading: String, caption: Option[String]): Radios =
      caption match {
        case Some(value) => radios.withHeadingAndSectionCaption(Text(heading), Text(value))
        case None        => radios.withHeading(Text(heading))
      }

    def withLegend(legend: String, legendIsVisible: Boolean = true): Radios = {
      val legendClass = if (legendIsVisible) "govuk-fieldset__legend--m" else "govuk-visually-hidden govuk-!-display-inline"
      radios.copy(
        fieldset = Some(Fieldset(legend = Some(Legend(content = Text(legend), classes = legendClass, isPageHeading = false))))
      )
    }

  }

}
