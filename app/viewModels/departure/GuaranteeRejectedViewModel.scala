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

package viewModels.departure

import models.GuaranteeReference
import models.departure.GuaranteeReferenceTable
import play.api.i18n.Messages
import utils.Format

import javax.inject.Inject
import javax.xml.datatype.XMLGregorianCalendar

case class GuaranteeRejectedViewModel(
  guaranteeReferences: Seq[GuaranteeReference],
  lrn: String,
  mrn: String,
  declarationAcceptanceDate: String,
  paragraph1: String,
  paragraph2: String,
  link: String
) {

  def tables(implicit messages: Messages): Seq[GuaranteeReferenceTable] =
    guaranteeReferences.zipWithIndex.map {
      case (guaranteeReference, index) =>
        GuaranteeReferenceTable(
          title = messages("guarantee.rejected.message.guaranteeReference", index + 1),
          grn = guaranteeReference.grn,
          table = guaranteeReference.toTable
        )
    }
}

object GuaranteeRejectedViewModel {

  class GuaranteeRejectedViewModelProvider @Inject() {

    def apply(
      guaranteeReferences: Seq[GuaranteeReference],
      lrn: String,
      mrn: String,
      declarationAcceptanceDate: XMLGregorianCalendar
    )(implicit messages: Messages): GuaranteeRejectedViewModel = {
      val paragraph1: String =
        if (guaranteeReferences.length == 1 && guaranteeReferences.head.invalidGuarantees.length == 1) {
          messages("guarantee.rejected.message.paragraph1.singular")
        } else if (guaranteeReferences.length == 1 && guaranteeReferences.head.invalidGuarantees.length > 1) {
          messages("guarantee.rejected.message.paragraph1.singularGuaranteePluralReference")
        } else {
          messages("guarantee.rejected.message.paragraph1.pluralGuaranteePluralReference")
        }

      val paragraph2: String =
        if (guaranteeReferences.length == 1 && guaranteeReferences.head.invalidGuarantees.length == 1) {
          messages("guarantee.rejected.message.contact.singular")
        } else {
          messages("guarantee.rejected.message.contact.plural")
        }

      new GuaranteeRejectedViewModel(
        guaranteeReferences = guaranteeReferences,
        lrn = lrn,
        mrn = mrn,
        declarationAcceptanceDate = Format.formatDeclarationAcceptanceDate(declarationAcceptanceDate),
        paragraph1 = paragraph1,
        paragraph2 = paragraph2,
        link = messages("guarantee.rejected.message.makeAnotherDeparture")
      )
    }
  }
}
