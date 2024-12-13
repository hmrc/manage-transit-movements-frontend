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

package views.departureP5

import generators.Generators
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.Table
import viewModels.P5.departure.GuaranteeRejectedNotAmendableP5ViewModel
import views.behaviours.TableViewBehaviours
import views.html.departureP5.GuaranteeRejectedNotAmendableP5View

class GuaranteeRejectedNotAmendableP5ViewSpec extends TableViewBehaviours with Generators {

  private val viewModel: GuaranteeRejectedNotAmendableP5ViewModel =
    arbitraryGuaranteeRejectedNotAmendableP5ViewModel.arbitrary.sample.value

  override val tables: Seq[Table] = viewModel.tables.map(_.table)

  override val prefix: String = "guarantee.rejected.message.notAmendable"

  override def view: HtmlFormat.Appendable = injector
    .instanceOf[GuaranteeRejectedNotAmendableP5View]
    .apply(viewModel, departureIdP5, messageId)(fakeRequest, messages)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithContent("p", viewModel.paragraph1)

  behave like pageWithContent("p", s"Movement Reference Number (MRN): ${viewModel.mrn}")

  behave like pageWithContent("p", s"Declaration acceptance date: ${viewModel.declarationAcceptanceDate}")

  behave like pageWithTables()

  behave like pageWithoutSubmitButton()

  behave like pageWithLink(
    "helpdesk-link",
    viewModel.paragraph2,
    frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithLink(
    "makeNewDeparture",
    viewModel.link,
    frontendAppConfig.p5Departure
  )
}
