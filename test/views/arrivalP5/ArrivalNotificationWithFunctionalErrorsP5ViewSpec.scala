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

package views.arrivalP5

import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.{HeadCell, TableRow}
import viewModels.P5.arrival.ArrivalNotificationWithFunctionalErrorsP5ViewModel
import viewModels.pagination.ListPaginationViewModel
import viewModels.sections.Section
import views.behaviours.{PaginationViewBehaviours, TableViewBehaviours}
import views.html.arrivalP5.ArrivalNotificationWithFunctionalErrorsP5View

class ArrivalNotificationWithFunctionalErrorsP5ViewSpec extends PaginationViewBehaviours[ListPaginationViewModel] with TableViewBehaviours with Generators {

  override val headCells: Seq[HeadCell] =
    Seq(HeadCell(Text("Error")), HeadCell(Text("Business rule ID")), HeadCell(Text("Invalid data item")), HeadCell(Text("Invalid answer")))

  val tableRows: Seq[TableRow] = arbitrary[Seq[TableRow]].sample.value

  override val prefix: String        = "arrival.ie057.review.notification.message"
  override val movementsPerPage: Int = paginationAppConfig.arrivalsNumberOfErrorsPerPage

  override val buildViewModel: (Int, Int, Int, String) => ListPaginationViewModel =
    ListPaginationViewModel(_, _, _, _)

  private val sections: Seq[Section] = arbitrary[List[Section]].sample.value

  val paginationViewModel: ListPaginationViewModel = ListPaginationViewModel(
    totalNumberOfItems = sections.length,
    currentPage = 1,
    numberOfItemsPerPage = paginationAppConfig.departuresNumberOfErrorsPerPage,
    href = controllers.arrivalP5.routes.ArrivalNotificationWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalIdP5, messageId).url,
    additionalParams = Seq()
  )

  private val arrivalNotificationWithFunctionalErrorsP5ViewModel: ArrivalNotificationWithFunctionalErrorsP5ViewModel =
    new ArrivalNotificationWithFunctionalErrorsP5ViewModel(Seq(tableRows), mrn, false)

  override def view: HtmlFormat.Appendable = applyView(arrivalNotificationWithFunctionalErrorsP5ViewModel, paginationViewModel)

  override def viewWithSpecificPagination(paginationViewModel: ListPaginationViewModel): HtmlFormat.Appendable =
    applyView(arrivalNotificationWithFunctionalErrorsP5ViewModel, paginationViewModel)

  private def applyView(
    viewModel: ArrivalNotificationWithFunctionalErrorsP5ViewModel,
    paginationViewModel: ListPaginationViewModel
  ): HtmlFormat.Appendable =
    injector
      .instanceOf[ArrivalNotificationWithFunctionalErrorsP5View]
      .apply(viewModel, arrivalIdP5, paginationViewModel)(fakeRequest, messages, frontendAppConfig)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithCaption(s"MRN: $mrn")

  behave like pageWithPagination(controllers.arrivalP5.routes.ArrivalNotificationWithFunctionalErrorsP5Controller.onPageLoad(None, arrivalIdP5, messageId).url)

  behave like pageWithTable()

  behave like pageWithoutFormAction()

  behave like pageWithoutSubmitButton()

  "must render correct paragraph1 content" in {
    assertSpecificElementContainsText(
      "paragraph-1",
      s"There is a problem with this notification. Review the error and make a new notification with the right information."
    )
  }

  "must render correct paragraph2 content" in {
    assertSpecificElementContainsText(
      "paragraph-2",
      s"We will keep your previous answers for 30 days - so if you use the same MRN within this time, your answers will be pre-populated."
    )
  }

  "must render correct paragraph3 content" in {
    assertSpecificElementContainsText(
      "helpdesk",
      "Contact the New Computerised Transit System helpdesk for help understanding the error (opens in a new tab)."
    )
    assertSpecificElementContainsText(
      "helpdesk-link",
      "New Computerised Transit System helpdesk"
    )
  }

  behave like pageWithLink(
    "helpdesk-link",
    "New Computerised Transit System helpdesk",
    frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithLink(
    "arrival-link",
    "Make another arrival notification",
    frontendAppConfig.p5Arrival
  )

  private def assertSpecificElementContainsText(id: String, expectedText: String): Unit = {
    val element = doc.getElementById(id)
    assertElementContainsText(element, expectedText)
  }

}
