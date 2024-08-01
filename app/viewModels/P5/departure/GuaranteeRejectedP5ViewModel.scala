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

package viewModels.P5.departure

import generated.GuaranteeReferenceType08
import models.departureP5.GuaranteeReferenceTable
import play.api.i18n.Messages
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier
import utils.GuaranteeRejectedP5Helper

import java.text.SimpleDateFormat
import javax.inject.Inject
import javax.xml.datatype.XMLGregorianCalendar
import scala.concurrent.{ExecutionContext, Future}

case class GuaranteeRejectedP5ViewModel(
  tables: Seq[GuaranteeReferenceTable],
  lrn: String,
  mrn: String,
  acceptanceDate: XMLGregorianCalendar
) {

  // TODO - refactor
  def formatDateTime: String = {
    val date      = acceptanceDate.toGregorianCalendar.getTime
    val formatter = new SimpleDateFormat("dd/MM/yyyy")
    formatter.format(date)
  }

  def paragraph1(implicit messages: Messages): String =
    if (tables.length == 1 && tables.head.table.rows.length == 1) {
      messages("guarantee.rejected.message.paragraph1.singular")
    } else if (tables.length == 1 && tables.head.table.rows.length > 1) {
      messages("guarantee.rejected.message.paragraph1.singularGuaranteePluralReference")
    } else {
      messages("guarantee.rejected.message.paragraph1.pluralGuaranteePluralReference")
    }

  def paragraph2(implicit messages: Messages): String = if (tables.length == 1 && tables.head.table.rows.length == 1) {
    messages("guarantee.rejected.message.contact.singular")
  } else {
    messages("guarantee.rejected.message.contact.plural")
  }

  def buttonContent(implicit messages: Messages): String =
    messages("guarantee.rejected.message.amendErrors")

  def link(implicit messages: Messages): String =
    messages("guarantee.rejected.message.makeAnotherDeparture")
}

object GuaranteeRejectedP5ViewModel {

  class GuaranteeRejectedP5ViewModelProvider @Inject() (referenceDataService: ReferenceDataService) {

    def apply(
      guaranteeReferences: Seq[GuaranteeReferenceType08],
      lrn: String,
      mrn: String,
      acceptanceDate: XMLGregorianCalendar
    )(implicit messages: Messages, ec: ExecutionContext, hc: HeaderCarrier): Future[GuaranteeRejectedP5ViewModel] = {

      val helper = new GuaranteeRejectedP5Helper(guaranteeReferences, referenceDataService)

      helper.tables.map(GuaranteeRejectedP5ViewModel(_, lrn, mrn, acceptanceDate))
    }
  }
}
